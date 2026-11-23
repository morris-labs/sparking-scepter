package com.southcentralpositronics.reign_gdx.game.entity.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.southcentralpositronics.reign_gdx.engine.entity.component.PositionComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.system.SystemPriority;
import com.southcentralpositronics.reign_gdx.engine.level.Level;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;
import com.southcentralpositronics.reign_gdx.game.entity.Families;
import com.southcentralpositronics.reign_gdx.game.entity.MobFactory;
import com.southcentralpositronics.reign_gdx.game.sim.EntityKind;
import com.southcentralpositronics.reign_gdx.game.sim.EventLog;
import com.southcentralpositronics.reign_gdx.game.sim.SimulationEvent;

import java.util.Random;

/**
 * Starts a new wave of enemies whenever every enemy is dead, as the Java2D
 * {@code SpawnLevel} did. Enemies appear on random open tiles away from the
 * player. Clear Mobs, or a wave that finds no room, pauses new waves until the
 * next Add Mobs.
 */
public final class WaveSystem extends EntitySystem {
    /** The number of Shooters in each wave. */
    private static final int    SHOOTER_COUNT      = 10;
    /** The number of Chasers in each wave. */
    private static final int    CHASER_COUNT       = 5;
    /** The number of A* Chasers in each wave. */
    private static final int    ASTAR_CHASER_COUNT = 5;
    /** The closest that a spawned enemy can be to the player, in pixels. */
    private static final double MIN_SPAWN_DISTANCE = 6 * 16;

    private final Level      level;
    private final MobFactory mobs;
    private final EventLog   events;

    private ImmutableArray<Entity> enemies;
    private ImmutableArray<Entity> players;
    /** True after Clear Mobs, or after a wave found no room, so an empty level doesn't try a wave every tick. */
    private boolean                wavesPaused = false;
    /** The number of waves that have started. */
    private int                    wave        = 0;

    /**
     * Creates the system.
     *
     * @param level  The level whose open tiles and random source pick where enemies spawn.
     * @param mobs   The factory that adds enemies.
     * @param events The log that receives wave events.
     */
    public WaveSystem(Level level, MobFactory mobs, EventLog events) {
        super(SystemPriority.WAVES);
        this.level  = level;
        this.mobs   = mobs;
        this.events = events;
    }

    @Override
    public void addedToEngine(Engine engine) {
        enemies = engine.getEntitiesFor(Families.ENEMIES);
        players = engine.getEntitiesFor(Families.PLAYERS);
    }

    @Override
    public void update(float deltaTime) {
        if (enemies.size() == 0 && !wavesPaused && players.size() > 0) startWave();
    }

    /**
     * Starts the next wave: spawns a wave of enemies and raises
     * {@link SimulationEvent.Type#WAVE_START}. If no enemy finds room, the wave
     * doesn't count and new waves pause.
     */
    public void startWave() {
        if (spawnEnemies() == 0) {
            wavesPaused = true;
            return;
        }
        wave++;
        PositionComponent player = players.size() > 0 ? PositionComponent.MAPPER.get(players.first()) : null;
        events.raise(SimulationEvent.Type.WAVE_START, EntityKind.PLAYER, player != null ? player.x : 0,
                     player != null ? player.y : 0);
    }

    /**
     * Spawns one wave's enemies, as the Java2D {@code Level.generateMobs()}
     * did, and resumes new waves. This is the Add Mobs button.
     *
     * @return The number of enemies spawned.
     */
    public int spawnEnemies() {
        wavesPaused = false;
        return spawn(EntityKind.SHOOTER, SHOOTER_COUNT)
            + spawn(EntityKind.CHASER, CHASER_COUNT)
            + spawn(EntityKind.ASTAR_CHASER, ASTAR_CHASER_COUNT);
    }

    /** Removes every enemy, with no particles or events, and pauses new waves. This is the Clear Mobs button. */
    public void clearEnemies() {
        wavesPaused = true;
        Engine engine = getEngine();
        for (Entity enemy : enemies.toArray(Entity.class)) engine.removeEntity(enemy);
    }

    /** Gets the number of waves that have started. */
    public int getWave() {
        return wave;
    }

    /**
     * Spawns enemies of one kind on random open tiles at least
     * {@link #MIN_SPAWN_DISTANCE} from the player's tile. The method gives up on
     * an enemy after {@code width * height} tries, so a small arena spawns
     * fewer than asked.
     *
     * @return The number of enemies spawned.
     */
    private int spawn(EntityKind kind, int count) {
        PositionComponent player  = players.size() > 0 ? PositionComponent.MAPPER.get(players.first()) : null;
        int               playerX = player != null ? (int) Math.floor(player.x / Tile.SIZE) : 0;
        int               playerY = player != null ? (int) Math.floor(player.y / Tile.SIZE) : 0;
        int               width   = level.getWidth();
        int               height  = level.getHeight();
        Random            random  = level.getRandom();
        int               spawned = 0;

        for (int i = 0; i < count; i++) {
            for (int tries = 0; tries < width * height; tries++) {
                int tx = random.nextInt(width);
                int ty = random.nextInt(height);
                if (!level.isWalkable(tx, ty)) continue;
                if (player != null && Math.hypot(playerX - tx, playerY - ty) * Tile.SIZE < MIN_SPAWN_DISTANCE) {
                    continue;
                }
                mobs.createEnemy(kind, tx, ty);
                spawned++;
                break;
            }
        }
        return spawned;
    }
}
