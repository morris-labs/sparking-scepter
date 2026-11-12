package com.southcentralpositronics.reign_gdx.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.southcentralpositronics.reign_gdx.Game;
import com.southcentralpositronics.reign_gdx.entity.Entity;
import com.southcentralpositronics.reign_gdx.entity.Particle;
import com.southcentralpositronics.reign_gdx.entity.mob.AStarChaserMob;
import com.southcentralpositronics.reign_gdx.entity.mob.ChaserMob;
import com.southcentralpositronics.reign_gdx.entity.mob.Mob;
import com.southcentralpositronics.reign_gdx.entity.mob.PlayerMob;
import com.southcentralpositronics.reign_gdx.entity.mob.ShooterMob;
import com.southcentralpositronics.reign_gdx.entity.projectile.Projectile;
import com.southcentralpositronics.reign_gdx.graphics.TileLoader;
import com.southcentralpositronics.reign_gdx.graphics.TileMapBuilder;
import com.southcentralpositronics.reign_gdx.level.tile.Node;
import com.southcentralpositronics.reign_gdx.level.tile.Tile;
import com.southcentralpositronics.reign_gdx.level.tile.TileCoordinates;
import com.southcentralpositronics.reign_gdx.util.Vector2i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.BiFunction;

public class Level {
    private final ArrayList<Entity>     entities    = new ArrayList<>();
    private final ArrayList<Projectile> projectiles = new ArrayList<>();
    private final ArrayList<Particle>   particles   = new ArrayList<>();
    private final ArrayList<PlayerMob>  players     = new ArrayList<>();
    private final ArrayList<Mob>        mobs        = new ArrayList<>();

    protected int width, height;
    public    Tile[][] tiles;
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

    public Level(Game game) {
        this.game = game;
        loadLevel(game.getTileAtlas(), game.getMapPath());
    }

    /** Gets the enemies in the level. */
    public List<Mob> getMobs() {
        return mobs;
    }

    public ArrayList<PlayerMob> getPlayers() {
        return players;
    }

    protected void generateLevel(Pixmap colorMap, Map<Integer, Tile> tileMap) {
        tiles = TileMapBuilder.buildTileGrid(colorMap, tileMap);
    }

    protected void loadLevel(TextureAtlas tileAtlas, String mapPath) {
        FileHandle PixelMap = Gdx.files.internal(mapPath);
        Pixmap     colorMap = new Pixmap(PixelMap);
        width  = colorMap.getWidth();
        height = colorMap.getHeight();
        Map<Integer, Tile> tileMap = TileLoader.loadTiles(tileAtlas);
        generateLevel(colorMap, tileMap);
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

    private void remove() {
        entities.removeIf(Entity::isRemoved);
        projectiles.removeIf(Entity::isRemoved);
        particles.removeIf(Entity::isRemoved);
        mobs.removeIf(Entity::isRemoved);
        players.removeIf(Entity::isRemoved);
    }

    /** Draws the tiles, then particles, projectiles, enemies, and players. The camera handles scrolling. */
    public void render(SpriteBatch batch) {
        renderTiles(batch);
        for (Particle p : particles) p.render(batch);
        for (Projectile p : projectiles) p.render(batch);
        for (Mob m : mobs) m.render(batch);
        for (PlayerMob p : players) p.render(batch);
    }

    private void renderTiles(SpriteBatch batch) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Tile tile = tiles[x][y];
                if (tile == null || tile.getTexture() == null) continue;
                batch.draw(tile.getTexture(), x * Tile.SIZE, y * Tile.SIZE, Tile.SIZE, Tile.SIZE);
            }
        }
    }

    public Tile getTileAt(int x, int y) {
        if (x < 0 || y < 0 || x >= tiles.length || y >= tiles[0].length) {
            return null;
        }
        return tiles[x][y];
    }

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

        int left   = (int) (hitboxX / tileSize);
        int right  = (int) ((hitboxX + hitboxWidth - 1) / tileSize);
        int top    = (int) (hitboxY / tileSize);
        int bottom = (int) ((hitboxY + hitboxHeight - 1) / tileSize);

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

    public Game getGame() {
        return game;
    }

    /**
     * Picks a random open tile away from the map edge. After {@code width *
     * height} misses, it gives up and returns tile (0, 0).
     */
    public TileCoordinates getRandomNonSolidTile() {
        if (tiles == null || tiles.length == 0) throw new IllegalStateException("Level not loaded");

        int maxTries = width * height;
        for (int tries = 0; tries < maxTries; tries++) {
            int  x = MathUtils.random(1, width - 2);
            int  y = MathUtils.random(1, height - 2);
            Tile t = tiles[x][y];
            if (t != null && !t.isSolid()) return new TileCoordinates(x, y);
        }
        return new TileCoordinates(0, 0);
    }
}
