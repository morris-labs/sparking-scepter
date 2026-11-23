package com.southcentralpositronics.reign_gdx.engine.level;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.southcentralpositronics.reign_gdx.engine.graphics.TileLoader;
import com.southcentralpositronics.reign_gdx.engine.graphics.TileMapBuilder;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;
import com.southcentralpositronics.reign_gdx.engine.util.Vector2i;

import java.util.Optional;

/**
 * The layout of a level before play starts: its tiles and, if it has one, the
 * player start. A simulation builds its level from an arena, so an arena holds
 * no entities and no textures.
 */
public final class Arena {
    private final Tile[][] tiles;
    private final Vector2i playerStart;

    /**
     * Creates an arena from a tile grid.
     *
     * @param tiles       The tiles, indexed {@code [x][y]} with y up.
     * @param playerStart The tile where the player appears, or null to let the
     *                    simulation pick a random open tile.
     */
    public Arena(Tile[][] tiles, Vector2i playerStart) {
        if (tiles.length == 0 || tiles[0].length == 0) throw new IllegalArgumentException("An arena needs at least one tile");
        this.tiles       = tiles;
        this.playerStart = playerStart;
    }

    /**
     * Loads an arena from a level PNG file, one pixel per tile. A PNG file has no
     * player start.
     *
     * <p>This method needs the libGDX native library but no window or OpenGL
     * context.
     *
     * @param image The level PNG file.
     * @return The arena.
     */
    public static Arena fromPng(FileHandle image) {
        Pixmap colorMap = new Pixmap(image);
        try {
            return new Arena(TileMapBuilder.buildTileGrid(colorMap, TileLoader.loadTiles()), null);
        } finally {
            colorMap.dispose();
        }
    }

    /** Gets the width, in tiles. */
    public int getWidth() {
        return tiles.length;
    }

    /** Gets the height, in tiles. */
    public int getHeight() {
        return tiles[0].length;
    }

    /** Gets the tile grid, indexed {@code [x][y]} with y up. Don't change it. */
    Tile[][] tiles() {
        return tiles;
    }

    /** Gets the tile where the player appears, if the arena sets one. */
    public Optional<Vector2i> getPlayerStart() {
        return Optional.ofNullable(playerStart);
    }
}
