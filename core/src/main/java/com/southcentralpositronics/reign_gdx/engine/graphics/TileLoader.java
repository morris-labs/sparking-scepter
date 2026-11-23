package com.southcentralpositronics.reign_gdx.engine.graphics;

import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;

import java.util.HashMap;
import java.util.Map;

public class TileLoader {

    // Color constants (ARGB8888)
    public static final int COLOR_GRASS   = 0xFF125B0A;
    public static final int COLOR_HEDGE   = 0xFF004B00;
    public static final int COLOR_WATER   = 0xFFC4DBFF;
    public static final int COLOR_WALL    = 0xFF665C09;
    public static final int COLOR_WALLALT = 0xFF6C3279;
    public static final int COLOR_FLOOR   = 0xFF434343;
    public static final int COLOR_VOID    = 0xFF280A2B;
    /** Void in the Java2D level files, such as {@code SpawnLevel.png}. */
    public static final int COLOR_VOID_WHITE = 0xFFFFFFFF;

    /** Builds the color-to-tile table. The tiles have no textures; a renderer draws each tile by its type. */
    public static Map<Integer, Tile> loadTiles() {
        Map<Integer, Tile> tileMap = new HashMap<>();

        tileMap.put(COLOR_GRASS,   new Tile(Tile.tileType.grass, false));
        tileMap.put(COLOR_HEDGE,   new Tile(Tile.tileType.hedge, true));
        tileMap.put(COLOR_WATER,   new Tile(Tile.tileType.water, false));
        tileMap.put(COLOR_WALL,    new Tile(Tile.tileType.wall, true));
        tileMap.put(COLOR_WALLALT, new Tile(Tile.tileType.wall_alt, true));
        tileMap.put(COLOR_FLOOR,   new Tile(Tile.tileType.floor, false));
        tileMap.put(COLOR_VOID,    new Tile(Tile.tileType.void_tile, true)); // fallback
        tileMap.put(COLOR_VOID_WHITE, tileMap.get(COLOR_VOID));

        return tileMap;
    }
}
