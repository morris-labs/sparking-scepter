package com.southcentralpositronics.reign_gdx.engine.level;

import com.badlogic.ashley.core.Engine;
import com.southcentralpositronics.reign_gdx.engine.entity.ParticleEmitter;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;
import com.southcentralpositronics.reign_gdx.engine.util.Vector2i;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Owns a level's terrain and tile collision, and the random source of its
 * rules. The level's entities live in an Ashley engine. A level holds no
 * textures and needs no window or OpenGL context; a renderer draws it from the
 * outside.
 */
public class Level {
    protected final Random random;
    protected final ParticleEmitter particleEmitter;
    protected final List<Vector2i> spawnPoints;
    protected int width, height;
    public Tile[][] tiles;

    /**
     * Builds a level from an arena's tiles.
     *
     * @param arena The arena to play.
     * @param seed   The seed for every random choice that the level's rules
     *               make. Particles draw from a separate source with the same
     *               seed, so they don't change the rules' choices.
     * @param engine The Ashley engine that holds the level's entities. The
     *               level adds its {@link ParticleEmitter} to it as a system.
     */
    public Level(Arena arena, long seed, Engine engine) {
        this.tiles           = arena.tiles();
        this.width           = arena.getWidth();
        this.height          = arena.getHeight();
        this.spawnPoints     = arena.getSpawnPoints();
        this.random          = new Random(seed);
        this.particleEmitter = new ParticleEmitter(engine, new Random(seed));
        engine.addSystem(particleEmitter);
    }

    /** Gets the emitter that bursts particles into the level's Ashley engine. */
    public ParticleEmitter getParticleEmitter() {
        return particleEmitter;
    }

    public Tile getTileAt(int x, int y) {
        if (x < 0 || y < 0 || x >= tiles.length || y >= tiles[0].length) {
            return null;
        }
        return tiles[x][y];
    }

    /**
     * Checks whether a box overlaps a tile that blocks movement, or leaves the
     * map.
     *
     * @param left   The x coordinate of the box's left edge, in pixels.
     * @param bottom The y coordinate of the box's bottom edge, in pixels.
     * @param width  The box's width, in pixels.
     * @param height The box's height, in pixels.
     * @return Whether the box is blocked.
     */
    public boolean blocksMovement(double left, double bottom, int width, int height) {
        return boxBlocked(left, bottom, width, height, Tile::blocksMovement);
    }

    /**
     * Checks whether a box overlaps a tile that stops spells, or leaves the map.
     *
     * @param left   The x coordinate of the box's left edge, in pixels.
     * @param bottom The y coordinate of the box's bottom edge, in pixels.
     * @param width  The box's width, in pixels.
     * @param height The box's height, in pixels.
     * @return Whether the box is blocked.
     */
    public boolean blocksSpells(double left, double bottom, int width, int height) {
        return boxBlocked(left, bottom, width, height, Tile::blocksSpells);
    }

    private boolean boxBlocked(double left, double bottom, int boxWidth, int boxHeight, Predicate<Tile> blocks) {
        int tileSize = Tile.SIZE;

        int firstColumn = (int) Math.floor(left / tileSize);
        int lastColumn  = (int) Math.floor((left + boxWidth - 1) / tileSize);
        int firstRow    = (int) Math.floor(bottom / tileSize);
        int lastRow     = (int) Math.floor((bottom + boxHeight - 1) / tileSize);

        for (int tx = firstColumn; tx <= lastColumn; tx++) {
            for (int ty = firstRow; ty <= lastRow; ty++) {
                if (!inBounds(tx, ty)) return true; // Out of bounds counts as blocked.
                Tile tile = tiles[tx][ty];
                if (tile != null && blocks.test(tile)) return true;
            }
        }
        return false;
    }

    /** Checks whether a tile is inside the map and doesn't block movement. */
    public boolean isWalkable(int tx, int ty) {
        if (!inBounds(tx, ty)) return false;
        Tile tile = tiles[tx][ty];
        return tile != null && !tile.blocksMovement();
    }

    private boolean inBounds(int tx, int ty) {
        return tx >= 0 && ty >= 0 && tx < width && ty < height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Gets the tiles where enemies enter, in the order the arena lists them.
     * The list can't change; don't change its tiles.
     */
    public List<Vector2i> getSpawnPoints() {
        return spawnPoints;
    }

    /** Gets the random number source that the level's rules draw from. */
    public Random getRandom() {
        return random;
    }

    /**
     * Picks a random open tile away from the map edge. After {@code width *
     * height} misses, it gives up and returns tile (0, 0).
     *
     * @return The tile's column and row.
     */
    public Vector2i getRandomNonSolidTile() {
        int maxTries = width * height;
        for (int tries = 0; tries < maxTries && width > 2 && height > 2; tries++) {
            int  x = 1 + random.nextInt(width - 2);
            int  y = 1 + random.nextInt(height - 2);
            Tile t = tiles[x][y];
            if (t != null && !t.blocksMovement()) return new Vector2i(x, y);
        }
        return new Vector2i(0, 0);
    }
}
