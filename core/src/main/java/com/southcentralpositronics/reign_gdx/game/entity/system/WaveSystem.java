package com.southcentralpositronics.reign_gdx.game.entity.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.southcentralpositronics.reign_gdx.engine.entity.component.PositionComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.system.SystemPriority;
import com.southcentralpositronics.reign_gdx.engine.level.Level;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;
import com.southcentralpositronics.reign_gdx.engine.time.FixedTimestep;
import com.southcentralpositronics.reign_gdx.engine.util.Vector2i;
import com.southcentralpositronics.reign_gdx.game.entity.Families;
import com.southcentralpositronics.reign_gdx.game.entity.MobFactory;
import com.southcentralpositronics.reign_gdx.game.sim.EntityKind;
import com.southcentralpositronics.reign_gdx.game.sim.EventLog;
import com.southcentralpositronics.reign_gdx.game.sim.RunState;
import com.southcentralpositronics.reign_gdx.game.sim.SimulationEvent;
import com.southcentralpositronics.reign_gdx.game.upgrade.UpgradeChoice;
import com.southcentralpositronics.reign_gdx.game.wave.WavePlan;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs the survival loop of waves and breaks. A run opens with a break of
 * {@link WavePlan#FIRST_BREAK_SECONDS}; when a break runs out, the next wave
 * from the {@link WavePlan} enters at the arena's spawn points; when the last
 * enemy of a wave dies, a break of {@link WavePlan#BREAK_SECONDS} starts.
 *
 * <p>Each break after a cleared wave offers upgrades, and its countdown waits
 * until the player chooses one. The break before the first wave offers none.
 *
 * <p>Each enemy enters at a random spawn point at least
 * {@link WavePlan#MIN_SPAWN_DISTANCE} from the player, or at the farthest spawn
 * point if none is that far. In an arena with no spawn points, no wave starts:
 * the break runs out and the run waits.
 *
 * <p>Clear Mobs pauses the loop: the break stops counting down and a wave with
 * no enemies left doesn't end, until the next Add Mobs.
 */
public final class WaveSystem extends EntitySystem {
    private final Level      level;
    private final MobFactory mobs;
    private final EventLog   events;
    private final RunState      run;
    private final UpgradeChoice upgrades;

    private ImmutableArray<Entity> enemies;
    private ImmutableArray<Entity> players;
    /** True after Clear Mobs, until the next Add Mobs. */
    private boolean                paused = false;

    /**
     * Creates the system.
     *
     * @param level  The level whose spawn points and random source pick where enemies enter.
     * @param mobs   The factory that adds enemies.
     * @param events The log that receives wave events.
     * @param run      The run's state, which the system moves between waves and breaks.
     * @param upgrades The run's upgrades, offered at the start of each break after a cleared wave.
     */
    public WaveSystem(Level level, MobFactory mobs, EventLog events, RunState run, UpgradeChoice upgrades) {
        super(SystemPriority.WAVES);
        this.level  = level;
        this.mobs   = mobs;
        this.events = events;
        this.run    = run;
        this.upgrades = upgrades;
    }

    @Override
    public void addedToEngine(Engine engine) {
        enemies = engine.getEntitiesFor(Families.ENEMIES);
        players = engine.getEntitiesFor(Families.PLAYERS);
    }

    /** Opens the run with the break before the first wave. */
    public void startRun() {
        run.startBreak(ticks(WavePlan.FIRST_BREAK_SECONDS));
    }

    @Override
    public void update(float deltaTime) {
        if (paused || run.isOver() || players.size() == 0) return;
        if (run.isWave()) {
            if (enemies.size() == 0) {
                run.startBreak(ticks(WavePlan.BREAK_SECONDS));
                upgrades.offer();
            }
        } else if (!upgrades.isPending() && run.countDownBreak()) {
            startWave();
        }
    }

    /**
     * Starts the next wave: spawns its enemies and raises
     * {@link SimulationEvent.Type#WAVE_START}. With no spawn points, it does
     * nothing.
     */
    private void startWave() {
        if (level.getSpawnPoints().isEmpty()) return;
        run.startWave();
        spawn(WavePlan.enemies(run.getWave()));
        PositionComponent player = PositionComponent.MAPPER.get(players.first());
        events.raise(SimulationEvent.Type.WAVE_START, EntityKind.PLAYER, player.x, player.y);
    }

    /**
     * Spawns the enemies of the current wave, or of wave 1 before the first
     * wave, without starting a wave, and resumes the loop. This is the Add Mobs
     * button.
     */
    public void addEnemies() {
        paused = false;
        spawn(WavePlan.enemies(run.getWave()));
    }

    /** Removes every enemy, with no particles, score, or events, and pauses the loop. This is the Clear Mobs button. */
    public void clearEnemies() {
        paused = true;
        Engine engine = getEngine();
        for (Entity enemy : enemies.toArray(Entity.class)) engine.removeEntity(enemy);
    }

    /** Adds each enemy at a spawn point that {@link #pickSpawnPoint()} chooses. */
    private void spawn(List<EntityKind> kinds) {
        if (level.getSpawnPoints().isEmpty()) return;
        for (EntityKind kind : kinds) {
            Vector2i point = pickSpawnPoint();
            mobs.createEnemy(kind, point.x, point.y);
        }
    }

    /**
     * Picks a random spawn point at least {@link WavePlan#MIN_SPAWN_DISTANCE}
     * from the player, or the farthest one if none is. Distance runs from the
     * player's position to where an enemy on the spawn point would stand.
     */
    private Vector2i pickSpawnPoint() {
        List<Vector2i>    points   = level.getSpawnPoints();
        PositionComponent player   = players.size() > 0 ? PositionComponent.MAPPER.get(players.first()) : null;
        if (player == null) return points.get(level.getRandom().nextInt(points.size()));

        List<Vector2i> farEnough = new ArrayList<>();
        Vector2i       farthest  = points.getFirst();
        double         farthestDistance = -1;
        for (Vector2i point : points) {
            double distance = Math.hypot(point.x * Tile.SIZE + Tile.SIZE / 2.0 - player.x,
                                         point.y * Tile.SIZE + MobFactory.BODY_HEIGHT / 2.0 - player.y);
            if (distance >= WavePlan.MIN_SPAWN_DISTANCE) farEnough.add(point);
            if (distance > farthestDistance) {
                farthest         = point;
                farthestDistance = distance;
            }
        }
        return farEnough.isEmpty() ? farthest : farEnough.get(level.getRandom().nextInt(farEnough.size()));
    }

    private static int ticks(float seconds) {
        return Math.round(seconds * FixedTimestep.TICKS_PER_SECOND);
    }
}
