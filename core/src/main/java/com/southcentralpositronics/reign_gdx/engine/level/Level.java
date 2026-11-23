package com.southcentralpositronics.reign_gdx.engine.level;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.files.FileHandle;
import com.southcentralpositronics.reign_gdx.engine.entity.Entity;
import com.southcentralpositronics.reign_gdx.engine.entity.ParticleEmitter;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Node;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;
import com.southcentralpositronics.reign_gdx.engine.util.Vector2i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Owns a level's terrain, collision, paths, and generic entities. A level
 * holds no textures and needs no window or OpenGL context; a renderer draws
 * it from the outside.
 */
public class Level {
    protected final ArrayList<Entity> entities = new ArrayList<>();
    protected final Random random;
    protected final ParticleEmitter particleEmitter;
    protected int width, height;
    public Tile[][] tiles;

    /**
     * Builds a level from an arena's tiles.
     *
     * @param arena The arena to play.
     * @param seed   The seed for every random choice that the level's rules
     *               make. Particles draw from a separate source with the same
     *               seed, so they don't change the rules' choices.
     * @param engine The Ashley engine that holds the level's Ashley entities,
     *               such as particles.
     */
    public Level(Arena arena, long seed, Engine engine) {
        this.tiles           = arena.tiles();
        this.width           = arena.getWidth();
        this.height          = arena.getHeight();
        this.random          = new Random(seed);
        this.particleEmitter = new ParticleEmitter(engine, new Random(seed));
    }

    /**
     * Loads terrain from a level PNG file, without creating textures or a
     * window. Its particles go to an engine that nothing updates.
     */
    public Level(FileHandle image) {
        this(Arena.fromPng(image), 0L, new Engine());
    }

    /** Adds a generic entity to the level. */
    public void add(Entity entity) {
        entity.init(this);
        entities.add(entity);
    }

    /** Gets the emitter that bursts particles into the level's Ashley engine. */
    public ParticleEmitter getParticleEmitter() {
        return particleEmitter;
    }

    /**
     * Advances generic entities by one tick.
     *
     * @param delta The length of a tick, in seconds.
     */
    public void update(float delta) {
        for (int i = 0, n = entities.size(); i < n; i++) entities.get(i).update();
        remove();
    }

    /** Removes expired generic entities. */
    protected void remove() {
        entities.removeIf(Entity::isRemoved);
    }

    public Tile getTileAt(int x, int y) {
        if (x < 0 || y < 0 || x >= tiles.length || y >= tiles[0].length) {
            return null;
        }
        return tiles[x][y];
    }

    /**
     * Checks whether a box, shrunk by its paddings, overlaps a solid tile or
     * leaves the map.
     */
    public boolean tileCollision(double x, double y, int width, int height, int padLeft, int padRight, int padTop, int padBottom) {
        int tileSize = Tile.SIZE;

        double hitboxX      = x + padLeft;
        double hitboxY      = y + padBottom;
        int    hitboxWidth  = width - padLeft - padRight;
        int    hitboxHeight = height - padTop - padBottom;

        int left   = (int) Math.floor(hitboxX / tileSize);
        int right  = (int) Math.floor((hitboxX + hitboxWidth - 1) / tileSize);
        int top    = (int) Math.floor(hitboxY / tileSize);
        int bottom = (int) Math.floor((hitboxY + hitboxHeight - 1) / tileSize);

        for (int tx = left; tx <= right; tx++) {
            for (int ty = top; ty <= bottom; ty++) {
                if (tx < 0 || ty < 0 || tx >= tiles.length || ty >= tiles[0].length) {
                    return true; // Out of bounds counts as solid.
                }

                Tile tile = tiles[tx][ty];
                if (tile != null && tile.isSolid()) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean tileCollision(double x, double y, int width, int height) {
        return tileCollision(x, y, width, height, 0, 0, 0, 0);
    }

    /**
     * Finds a path between two tiles with A*, ported from the Java2D
     * {@code Level.findPath()}.
     *
     * <p>A mob's collision box fits in one tile, so any open tile is a valid
     * step. Diagonal steps cost the square root of two and can't cut a wall
     * corner. The Java2D version costed diagonals at 0.95, which made paths
     * zigzag, and let them cut corners.
     *
     * @param startVec The tile to start from.
     * @param destVec  The tile to reach.
     * @return The path from the goal back to the first step, so the next tile
     *     to move to is the last element. The list is empty when no path exists
     *     or when the mob is already on the goal tile.
     */
    public List<Node> findPath(Vector2i startVec, Vector2i destVec) {
        List<Node> path = new ArrayList<>();
        if (!inBounds(startVec.x, startVec.y) || !isTileOpen(destVec.x, destVec.y)) return path;

        double[] bestCost = new double[width * height];
        Arrays.fill(bestCost, Double.POSITIVE_INFINITY);
        boolean[]           closed = new boolean[width * height];
        PriorityQueue<Node> open   = new PriorityQueue<>(Comparator.comparingDouble((Node n) -> n.fCost));

        open.add(new Node(startVec, null, 0, Vector2i.getDistance(startVec, destVec)));
        bestCost[tileIndex(startVec.x, startVec.y)] = 0;

        while (!open.isEmpty()) {
            Node current = open.poll();
            int  index   = tileIndex(current.tile.x, current.tile.y);
            if (closed[index]) continue;    // A cheaper copy of this tile was expanded already.
            closed[index] = true;

            if (current.tile.equals(destVec)) {
                while (current.parent != null) {
                    path.add(current);
                    current = current.parent;
                }
                return path;
            }

            for (int i = 0; i < 9; i++) {
                if (i == 4) continue;       // The current tile.
                int xDir = (i % 3) - 1;
                int yDir = (i / 3) - 1;
                int nx   = current.tile.x + xDir;
                int ny   = current.tile.y + yDir;

                if (!isTileOpen(nx, ny)) continue;
                if (xDir != 0 && yDir != 0) {
                    // Don't cut corners: both orthogonal neighbours must be open too.
                    if (!isTileOpen(current.tile.x + xDir, current.tile.y)) continue;
                    if (!isTileOpen(current.tile.x, current.tile.y + yDir)) continue;
                }

                int nIndex = tileIndex(nx, ny);
                if (closed[nIndex]) continue;
                double gCost = current.gCost + (xDir != 0 && yDir != 0 ? Math.sqrt(2) : 1);
                if (gCost >= bestCost[nIndex]) continue;
                bestCost[nIndex] = gCost;
                Vector2i next = new Vector2i(nx, ny);
                open.add(new Node(next, current, gCost, Vector2i.getDistance(next, destVec)));
            }
        }
        return path;
    }

    private boolean inBounds(int tx, int ty) {
        return tx >= 0 && ty >= 0 && tx < width && ty < height;
    }

    private int tileIndex(int tx, int ty) {
        return ty * width + tx;
    }

    /** Checks whether the tile is inside the map and not solid. */
    private boolean isTileOpen(int tx, int ty) {
        if (!inBounds(tx, ty)) return false;
        Tile tile = tiles[tx][ty];
        return tile != null && !tile.isSolid();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
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
            if (t != null && !t.isSolid()) return new Vector2i(x, y);
        }
        return new Vector2i(0, 0);
    }
}
