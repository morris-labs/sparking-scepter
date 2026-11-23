package com.southcentralpositronics.reign_gdx.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Disposable;
import com.southcentralpositronics.reign_gdx.engine.graphics.TileLoader;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;
import com.southcentralpositronics.reign_gdx.game.sim.EntityState;
import com.southcentralpositronics.reign_gdx.game.sim.Simulation;

import java.util.List;

/**
 * A map of the whole level, ported from the Java2D {@code UIMiniMap}. Each
 * tile is a square of its level-file color, the player is blue, and enemies
 * are red. Void is black.
 */
public class Minimap extends Widget implements Disposable {
    /** The size of one tile on the minimap, in screen pixels, as in the Java2D version. */
    public static final int TILE_PIXELS = 3;

    private static final int PLAYER_COLOR = Color.rgba8888(Color.BLUE);
    private static final int ENEMY_COLOR  = Color.rgba8888(Color.RED);
    private static final int VOID_COLOR   = Color.rgba8888(Color.BLACK);

    private final Simulation simulation;
    private final int     tilesWide;
    private final int     tilesHigh;
    private final Pixmap  terrain;
    private final Pixmap  frame;
    private final Texture texture;

    /**
     * Builds the minimap for a simulation's arena. The terrain is drawn once;
     * mobs are drawn over it each frame.
     *
     * @param simulation The simulation to show.
     */
    public Minimap(Simulation simulation) {
        this.simulation = simulation;
        tilesWide       = simulation.getWidth();
        tilesHigh       = simulation.getHeight();
        terrain    = new Pixmap(tilesWide, tilesHigh, Pixmap.Format.RGBA8888);
        frame      = new Pixmap(tilesWide, tilesHigh, Pixmap.Format.RGBA8888);
        frame.setBlending(Pixmap.Blending.None);
        // Pixmap rows run top to bottom and tile rows bottom to top.
        for (int tx = 0; tx < tilesWide; tx++) {
            for (int ty = 0; ty < tilesHigh; ty++) {
                terrain.drawPixel(tx, tilesHigh - 1 - ty, tileColor(simulation.getTileAt(tx, ty)));
            }
        }
        texture = new Texture(frame);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        setSize(getPrefWidth(), getPrefHeight());
    }

    /** Gets the RGBA8888 color for a tile, matching the level-file color. */
    private static int tileColor(Tile tile) {
        if (tile == null) return VOID_COLOR;
        int argb = switch (tile.tile_Type) {
            case grass -> TileLoader.COLOR_GRASS;
            case hedge -> TileLoader.COLOR_HEDGE;
            case water -> TileLoader.COLOR_WATER;
            case wall -> TileLoader.COLOR_WALL;
            case wall_alt -> TileLoader.COLOR_WALLALT;
            case floor -> TileLoader.COLOR_FLOOR;
            case void_tile -> 0xFF000000;
        };
        return (argb << 8) | (argb >>> 24);     // ARGB to RGBA
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        frame.drawPixmap(terrain, 0, 0);
        List<EntityState> entities = simulation.getEntities();
        for (int i = 0, n = entities.size(); i < n; i++) {
            EntityState entity = entities.get(i);
            if (entity.kind().isEnemy()) plot(entity, ENEMY_COLOR);
        }
        plot(simulation.getPlayer(), PLAYER_COLOR);
        texture.draw(frame, 0, 0);
    }

    private void plot(EntityState mob, int color) {
        int tx = (int) Math.floor(mob.x() / Tile.SIZE);
        int ty = (int) Math.floor(mob.y() / Tile.SIZE);
        if (tx < 0 || ty < 0 || tx >= tilesWide || ty >= tilesHigh) return;
        frame.drawPixel(tx, tilesHigh - 1 - ty, color);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.setColor(1f, 1f, 1f, parentAlpha);
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public float getPrefWidth() {
        return tilesWide * TILE_PIXELS;
    }

    @Override
    public float getPrefHeight() {
        return tilesHigh * TILE_PIXELS;
    }

    @Override
    public void dispose() {
        terrain.dispose();
        frame.dispose();
        texture.dispose();
    }
}
