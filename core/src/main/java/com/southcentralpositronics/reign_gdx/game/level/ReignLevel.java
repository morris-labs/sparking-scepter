package com.southcentralpositronics.reign_gdx.game.level;

import com.badlogic.ashley.core.Engine;
import com.southcentralpositronics.reign_gdx.engine.entity.Entity;
import com.southcentralpositronics.reign_gdx.engine.level.Arena;
import com.southcentralpositronics.reign_gdx.engine.level.Level;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;
import com.southcentralpositronics.reign_gdx.game.entity.mob.AStarChaserMob;
import com.southcentralpositronics.reign_gdx.game.entity.mob.ChaserMob;
import com.southcentralpositronics.reign_gdx.game.entity.mob.DummyMob;
import com.southcentralpositronics.reign_gdx.game.entity.mob.Mob;
import com.southcentralpositronics.reign_gdx.game.entity.mob.PlayerMob;
import com.southcentralpositronics.reign_gdx.game.entity.mob.ShooterMob;
import com.southcentralpositronics.reign_gdx.game.entity.projectile.Projectile;
import com.southcentralpositronics.reign_gdx.game.sim.EntityKind;
import com.southcentralpositronics.reign_gdx.game.sim.SimulationEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/** Applies Reign's mob, spell, and wave rules to a level. */
public class ReignLevel extends Level {
    private final ArrayList<Projectile>      projectiles = new ArrayList<>();
    private final ArrayList<PlayerMob>       players     = new ArrayList<>();
    private final ArrayList<Mob>             mobs        = new ArrayList<>();
    private final ArrayList<SimulationEvent> events      = new ArrayList<>();

    /**
     * True after Clear Mobs, or after a wave found no room to spawn, so the
     * empty level doesn't try a new wave on every tick.
     */
    private boolean wavesPaused = false;
    /** The number of waves that have started. */
    private int     wave        = 0;

    /** The number of shooter enemies in each wave. */
    private static final int    SHOOTER_COUNT      = 10;
    /** The number of chaser enemies in each wave. */
    private static final int    CHASER_COUNT       = 5;
    /** The number of A* chaser enemies in each wave. */
    private static final int    ASTAR_CHASER_COUNT = 5;
    /** The closest that a spawned mob can be to the player, in pixels. */
    private static final double MIN_SPAWN_DISTANCE = 6 * 16;

    /**
     * Builds the level for an arena.
     *
     * @param arena The arena to play.
     * @param seed   The seed for spawn positions and enemy behavior.
     * @param engine The Ashley engine that holds particles.
     */
    public ReignLevel(Arena arena, long seed, Engine engine) {
        super(arena, seed, engine);
    }

    /** Gets the enemies in the level. */
    public List<Mob> getMobs() {
        return mobs;
    }

    public ArrayList<PlayerMob> getPlayers() {
        return players;
    }

    /**
     * Updates every entity in the level by one tick, then drops the removed
     * ones.
     *
     * <p>The loops index the lists instead of using iterators, because an
     * update can add entities (for example, a mob firing a projectile).
     * Entities added during this pass update on the next tick.
     *
     * @param delta The length of a tick, in seconds.
     */
    @Override
    public void update(float delta) {
        for (int i = 0, n = entities.size(); i < n; i++) entities.get(i).update();
        for (int i = 0, n = projectiles.size(); i < n; i++) projectiles.get(i).update();
        for (int i = 0, n = mobs.size(); i < n; i++) mobs.get(i).update(delta);
        for (int i = 0, n = players.size(); i < n; i++) players.get(i).update(delta);
        for (int i = 0, n = mobs.size(); i < n; i++) mobs.get(i).checkForHits();
        for (int i = 0, n = players.size(); i < n; i++) players.get(i).checkForHits();
        remove();

        // As in the Java2D SpawnLevel, a new wave spawns once every enemy is dead.
        // Clear Mobs pauses this until the next Add Mobs.
        if (mobs.isEmpty() && !wavesPaused && getClientPlayer() != null) startWave();
    }

    @Override
    protected void remove() {
        entities.removeIf(Entity::isRemoved);
        projectiles.removeIf(Entity::isRemoved);
        mobs.removeIf(Entity::isRemoved);
        players.removeIf(Entity::isRemoved);
    }

    @Override
    public void add(Entity e) {
        e.init(this);
        if (e instanceof Projectile) {
            projectiles.add((Projectile) e);
        } else if (e instanceof PlayerMob) {
            players.add((PlayerMob) e);
        } else if (e instanceof Mob) {
            mobs.add((Mob) e);
        } else {
            entities.add(e);
        }
    }

    public PlayerMob getClientPlayer() {
        return !players.isEmpty() ? players.getFirst() : null;
    }

    public List<PlayerMob> getPlayersInRadius(Entity e, float radius) {
        List<PlayerMob> result = new ArrayList<>();
        for (PlayerMob player : players) {
            if (e.distanceTo(player) <= radius) {
                result.add(player);
            }
        }
        return result;
    }

    /**
     * Starts the next wave: spawns a wave of enemies and raises
     * {@link SimulationEvent.Type#WAVE_START}. If no enemy finds room, the wave
     * doesn't count and automatic waves pause.
     */
    public void startWave() {
        if (spawnEnemies() == 0) {
            wavesPaused = true;
            return;
        }
        wave++;
        PlayerMob player = getClientPlayer();
        double    x      = player != null ? player.getBoxCenterX() : 0;
        double    y      = player != null ? player.getBoxCenterY() : 0;
        raise(SimulationEvent.Type.WAVE_START, EntityKind.PLAYER, x, y);
    }

    /**
     * Spawns one wave's enemies, as the Java2D {@code Level.generateMobs()}
     * did, and resumes automatic waves.
     *
     * @return The number of enemies spawned.
     */
    public int spawnEnemies() {
        wavesPaused = false;
        return spawnMobs(SHOOTER_COUNT, ShooterMob::new)
            + spawnMobs(CHASER_COUNT, ChaserMob::new)
            + spawnMobs(ASTAR_CHASER_COUNT, AStarChaserMob::new);
    }

    /**
     * Spawns mobs on random open tiles, away from the player. Each mob's
     * collision box fills the bottom half of its tile.
     *
     * <p>The method gives up on a mob after {@code width * height} tries, so a
     * map with no free space spawns fewer mobs than requested.
     *
     * @param count   The number of mobs to spawn.
     * @param factory Creates a mob at the given tile coordinates.
     * @return The number of mobs spawned.
     */
    public int spawnMobs(int count, BiFunction<Integer, Integer, Mob> factory) {
        PlayerMob player  = getClientPlayer();
        int       spawned = 0;

        for (int i = 0; i < count; i++) {
            for (int tries = 0; tries < width * height; tries++) {
                int  tx   = random.nextInt(width);
                int  ty   = random.nextInt(height);
                Tile tile = tiles[tx][ty];
                if (tile == null || tile.isSolid()) continue;
                if (player != null && Math.hypot(player.getTileX() - tx, player.getTileY() - ty) * Tile.SIZE
                    < MIN_SPAWN_DISTANCE) {
                    continue;
                }
                add(factory.apply(tx, ty));
                spawned++;
                break;
            }
        }
        return spawned;
    }

    /**
     * Adds one enemy of a kind with its collision box on a tile.
     *
     * @param kind The kind of enemy.
     * @param tx   The tile column.
     * @param ty   The tile row.
     */
    public void spawnEnemy(EntityKind kind, int tx, int ty) {
        Mob mob = switch (kind) {
            case DUMMY -> new DummyMob(tx, ty);
            case CHASER -> new ChaserMob(tx, ty);
            case ASTAR_CHASER -> new AStarChaserMob(tx, ty);
            case SHOOTER -> new ShooterMob(tx, ty);
            default -> throw new IllegalArgumentException(kind + " isn't an enemy");
        };
        add(mob);
    }

    /** Removes every enemy and pauses new waves until the next {@link #spawnEnemies()}. */
    public void clearMobs() {
        wavesPaused = true;
        mobs.clear();
    }

    /** Records an event for the simulation to report. */
    public void raise(SimulationEvent.Type type, EntityKind kind, double x, double y) {
        events.add(new SimulationEvent(type, kind, x, y));
    }

    /** Returns the events raised since the last call, and forgets them. */
    public List<SimulationEvent> drainEvents() {
        List<SimulationEvent> drained = List.copyOf(events);
        events.clear();
        return drained;
    }

    /** Gets the number of waves that have started. */
    public int getWave() {
        return wave;
    }

    public ArrayList<Projectile> getProjectiles() {
        return projectiles;
    }
}
