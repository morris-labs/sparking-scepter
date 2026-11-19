package com.southcentralpositronics.reign_gdx.game.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.southcentralpositronics.reign_gdx.game.Game;
import com.southcentralpositronics.reign_gdx.engine.entity.Entity;
import com.southcentralpositronics.reign_gdx.engine.entity.Particle;
import com.southcentralpositronics.reign_gdx.game.entity.mob.AStarChaserMob;
import com.southcentralpositronics.reign_gdx.game.entity.mob.ChaserMob;
import com.southcentralpositronics.reign_gdx.game.entity.mob.Mob;
import com.southcentralpositronics.reign_gdx.game.entity.mob.PlayerMob;
import com.southcentralpositronics.reign_gdx.game.entity.mob.ShooterMob;
import com.southcentralpositronics.reign_gdx.game.entity.projectile.Projectile;
import com.southcentralpositronics.reign_gdx.engine.level.Level;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/** Applies Reign's mob, spell, and wave rules to a level. */
public class ReignLevel extends Level {
    private final ArrayList<Projectile> projectiles = new ArrayList<>();
    private final ArrayList<PlayerMob>  players     = new ArrayList<>();
    private final ArrayList<Mob>        mobs        = new ArrayList<>();

    protected Game     game;

    /** True after Clear Mobs, so the empty level doesn't spawn a new wave at once. */
    private boolean wavesPaused = false;

    /** The number of shooter enemies that {@link #generateMobs()} spawns. */
    private static final int    SHOOTER_COUNT      = 10;
    /** The number of chaser enemies that {@link #generateMobs()} spawns. */
    private static final int    CHASER_COUNT       = 5;
    /** The number of A* chaser enemies that {@link #generateMobs()} spawns. */
    private static final int    ASTAR_CHASER_COUNT = 5;
    /** The closest that a spawned mob can be to the player, in pixels. */
    private static final double MIN_SPAWN_DISTANCE = 6 * 16;

    public ReignLevel(Game game) {
        super(Gdx.files.internal(game.getMapPath()), game.getTileAtlas());
        this.game = game;
    }

    /** Gets the enemies in the level. */
    public List<Mob> getMobs() {
        return mobs;
    }

    public ArrayList<PlayerMob> getPlayers() {
        return players;
    }

    /**
     * Updates every entity in the level, then drops the removed ones.
     *
     * <p>The loops index the lists instead of using iterators, because an
     * update can add entities (for example, a mob firing a projectile).
     * Entities added during this pass update on the next frame.
     *
     * @param delta The time since the last frame, in seconds.
     */
    @Override
    public void update(float delta) {
        for (int i = 0, n = entities.size(); i < n; i++) entities.get(i).update();
        for (int i = 0, n = projectiles.size(); i < n; i++) projectiles.get(i).update();
        for (int i = 0, n = particles.size(); i < n; i++) particles.get(i).update();
        for (int i = 0, n = mobs.size(); i < n; i++) mobs.get(i).update(delta);
        for (int i = 0, n = players.size(); i < n; i++) players.get(i).update(delta);
        for (int i = 0, n = mobs.size(); i < n; i++) mobs.get(i).checkForHits();
        for (int i = 0, n = players.size(); i < n; i++) players.get(i).checkForHits();
        remove();

        // As in the Java2D SpawnLevel, a new wave spawns once every enemy is dead.
        // Clear Mobs pauses this until the next generateMobs() call.
        if (mobs.isEmpty() && !wavesPaused && getClientPlayer() != null) generateMobs();
    }

    @Override
    protected void remove() {
        entities.removeIf(Entity::isRemoved);
        projectiles.removeIf(Entity::isRemoved);
        particles.removeIf(Entity::isRemoved);
        mobs.removeIf(Entity::isRemoved);
        players.removeIf(Entity::isRemoved);
    }

    /** Draws the tiles, then particles, projectiles, enemies, and players. The camera handles scrolling. */
    @Override
    public void render(SpriteBatch batch) {
        renderTiles(batch);
        for (Particle p : particles) p.render(batch);
        for (Projectile p : projectiles) p.render(batch);
        for (Mob m : mobs) m.render(batch);
        for (PlayerMob p : players) p.render(batch);
    }

    @Override
    public void add(Entity e) {
        e.init(this);
        if (e instanceof Particle) {
            particles.add((Particle) e);
        } else if (e instanceof Projectile) {
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
     * Spawns the starting enemies for this level, as the Java2D
     * {@code Level.generateMobs()} did.
     */
    public void generateMobs() {
        wavesPaused = false;
        TextureAtlas atlas = game.getMobAtlas();
        spawnMobs(SHOOTER_COUNT, (tx, ty) -> new ShooterMob(tx, ty, atlas));
        spawnMobs(CHASER_COUNT, (tx, ty) -> new ChaserMob(tx, ty, atlas));
        spawnMobs(ASTAR_CHASER_COUNT, (tx, ty) -> new AStarChaserMob(tx, ty, atlas));
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
        PlayerMob player   = getClientPlayer();
        int       spawned  = 0;

        for (int i = 0; i < count; i++) {
            for (int tries = 0; tries < width * height; tries++) {
                int    tx = MathUtils.random(0, width - 1);
                int    ty = MathUtils.random(0, height - 1);
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

    /** Removes every enemy and pauses new waves until the next {@link #generateMobs()}. */
    public void clearMobs() {
        wavesPaused = true;
        mobs.clear();
    }

    public ArrayList<Projectile> getProjectiles() {
        return projectiles;
    }

    public Game getGame() {
        return game;
    }

}
