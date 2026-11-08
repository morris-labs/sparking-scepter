package com.southcentralpositronics.reign_gdx.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.southcentralpositronics.reign_gdx.Game;
import com.southcentralpositronics.reign_gdx.entity.Entity;
import com.southcentralpositronics.reign_gdx.entity.Particle;
import com.southcentralpositronics.reign_gdx.entity.mob.Mob;
import com.southcentralpositronics.reign_gdx.entity.mob.PlayerMob;
import com.southcentralpositronics.reign_gdx.entity.projectile.Projectile;
import com.southcentralpositronics.reign_gdx.entity.projectile.SpellProjectile;
import com.southcentralpositronics.reign_gdx.graphics.TileLoader;
import com.southcentralpositronics.reign_gdx.graphics.TileMapBuilder;
import com.southcentralpositronics.reign_gdx.level.tile.Node;
import com.southcentralpositronics.reign_gdx.level.tile.Tile;
import com.southcentralpositronics.reign_gdx.level.tile.TileCoordinates;
import com.southcentralpositronics.reign_gdx.util.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Level {
    private final ArrayList<Entity>     entities    = new ArrayList<>();
    private final ArrayList<Projectile> projectiles = new ArrayList<>();
    private final ArrayList<Particle>   particles   = new ArrayList<>();
    private final ArrayList<PlayerMob>  players     = new ArrayList<>();
    private final ArrayList<Mob>        mobs        = new ArrayList<>();
    private       PlayerMob             player      = getClientPlayer();

    protected int width, height;
    public    Tile[][] tiles;
    protected Viewport viewport;
    protected Game     game;

    private int xScroll, yScroll;

    public Level(Game game) {
        this.game = game;
        loadLevel(game.getTileAtlas(), game.getMapPath());
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
        remove();
    }

    private void remove() {
        entities.removeIf(Entity::isRemoved);
        projectiles.removeIf(Entity::isRemoved);
        particles.removeIf(Entity::isRemoved);
        mobs.removeIf(Entity::isRemoved);
        players.removeIf(Entity::isRemoved);
    }

    public void render(SpriteBatch batch) {
        int tileSize     = 16; // Assuming tiles are 16x16 pixels
        int screenWidth  = Game.VIEWPORT_WIDTH;
        int screenHeight = Game.VIEWPORT_HEIGHT;
        setScroll((int) (player.getX() - (double) screenWidth / 2), (int) (player.getY() - (double) screenHeight / 2));

        xScroll = 0;
        yScroll = 0;

        int x0 = xScroll / tileSize;
        int x1 = (xScroll + screenWidth + tileSize) / tileSize;
        int y0 = yScroll / tileSize;
        int y1 = (yScroll + screenHeight + tileSize) / tileSize;

        renderTiles(batch, tiles, xScroll, yScroll, tileSize);

        // Render entities
        for (Entity e : entities) e.render(batch, xScroll, yScroll);
        for (Particle p : particles) p.render(batch);
        for (Projectile p : projectiles) p.render(batch);
        for (Mob m : mobs) m.render(batch);
        for (PlayerMob p : players) p.render(batch);
    }

    public void renderTiles(SpriteBatch batch, Tile[][] tileGrid, int xScroll, int yScroll, int tileSize) {
        width  = tileGrid.length;
        height = tileGrid[0].length;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Tile tile = tileGrid[x][y];
                if (tile == null || tile.getTexture() == null) continue;

                float renderX = x * tileSize - xScroll;
                float renderY = y * tileSize - yScroll;

                batch.draw(tile.getTexture(), renderX, renderY, tileSize, tileSize);
            }
        }
    }


    public Tile getTileAt(int x, int y) {
        if (x < 0 || y < 0 || x >= tiles.length || y >= tiles[0].length) {
            return null; // Or optionally return a default void_tile
        }
        return tiles[x][y];
    }

    public Tile.tileType getTile(int x, int y) {
        return getTileAt(x, y).tile_Type;
    }

    public void add(Entity e) {
        e.init(this);
        if (e instanceof Particle) {
            particles.add((Particle) e);
        } else if (e instanceof SpellProjectile) {
            projectiles.add((Projectile) e);
        } else if (e instanceof PlayerMob) {
            players.add((PlayerMob) e);
        } else if (e instanceof Mob) {
            mobs.add((Mob) e);
        } else {
            entities.add(e);
        }
    }

    public void setScroll(int xScroll, int yScroll) {
        this.xScroll = xScroll;
        this.yScroll = yScroll;
    }

    public PlayerMob getClientPlayer() {
        return !players.isEmpty() ? players.getFirst() : null;
    }

    public List<Entity> getEntitiesInRadius(Entity e, int radius) {
        List<Entity> result = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity.equals(e)) continue;
            if (e.getPosition().dst(entity.getPosition()) <= radius) {
                result.add(entity);
            }
        }
        return result;
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

//    public void onEvent(Event event) {
//        PlayerMob clientPlayer = getClientPlayer();
//        if (clientPlayer != null) {
//            clientPlayer.onEvent(event);
//        }
//    }

    public void clearMobs() {
        mobs.clear();
    }

    public ArrayList<Projectile> getProjectiles() {
        return projectiles;
    }

    public boolean tileCollision(double x, double y, int width, int height, int padLeft, int padRight, int padTop, int padBottom) {
        int tileSize = 16;

        // Shrink the hitbox by the given paddings
        double hitboxX      = x + padLeft;
        double hitboxY      = y + padBottom;
        int    hitboxWidth  = width - padLeft - padRight;
        int    hitboxHeight = height - padTop - padBottom;

        // Compute tile coordinates
        int left   = (int) (hitboxX / tileSize);
        int right  = (int) ((hitboxX + hitboxWidth - 1) / tileSize);
        int top    = (int) (hitboxY / tileSize);
        int bottom = (int) ((hitboxY + hitboxHeight - 1) / tileSize);

        for (int tx = left; tx <= right; tx++) {
            for (int ty = top; ty <= bottom; ty++) {
                if (tx < 0 || ty < 0 || tx >= tiles.length || ty >= tiles[0].length) {
                    return true; // Out of bounds = solid
                }

                Tile tile = tiles[tx][ty];
                if (tile != null && tile.isSolid()) {
                    return true;
                }
            }
        }

        return false; // All tiles pass
    }

    public boolean tileCollision(double x, double y, int width, int height) {
        return tileCollision(x, y, width, height, 0, 0, 0, 0);
    }

//    public boolean tileCollision(double x, double y, int width, int height) {
//        int tileSize = 16;
//
//        // Check all 4 corners
//        int left   = (int) (x / tileSize);
//        int right  = (int) ((x + width - 1) / tileSize);
//        int top    = (int) (y / tileSize);
//        int bottom = (int) ((y + height - 1) / tileSize);
//
//        // Check each tile the bounding box touches
//        for (int tx = left; tx <= right; tx++) {
//            for (int ty = top; ty <= bottom; ty++) {
//                if (tx < 0 || ty < 0 || tx >= tiles.length || ty >= tiles[0].length) {
//                    return true; // Out-of-bounds = solid
//                }
//
//                Tile tile = tiles[tx][ty];
//                if (tile != null && tile.isSolid()) {
//                    return true;
//                }
//            }
//        }
//
//        return false; // All tiles pass
//    }


    public void setPlayer(PlayerMob player) {
        this.player = player;
    }

    public List<Node> findPath(Vector2i startVec, Vector2i destVec) {
        return List.of();
    }

    public ArrayList<Entity> getEntities() {
        return entities;
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

    public TileCoordinates getRandomNonSolidTile() {
        // Fail-early safety in case the map is all solid
        if (tiles == null || tiles.length == 0) throw new IllegalStateException("Level not loaded");

        int tries    = 0;                 // avoids an infinite loop on maps with few open spots
        int maxTries = width * height; // reasonable upper bound

        while (tries++ < maxTries) {
            int  x = MathUtils.random(1, width - 2); // 1..width-2 keeps you off the outer wall
            int  y = MathUtils.random(1, height - 2); // ditto for Y
            Tile t = tiles[x][y];
            if (t != null && !t.isSolid()) {
                return new TileCoordinates(x, y);    // found a walkable tile
            }
        }
        return new TileCoordinates(0, 0);
    }

    // Additional methods like generateMobs, findPath, tileCollision can be implemented similarly
}
