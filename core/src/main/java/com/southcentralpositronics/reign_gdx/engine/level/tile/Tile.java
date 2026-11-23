package com.southcentralpositronics.reign_gdx.engine.level.tile;

/** One map tile: its type and whether it blocks movement. A renderer draws it by its type. */
public class Tile {
    /** The width and height of a tile, in world pixels. */
    public static final int SIZE = 16;

    public int x, y;
    public boolean       isSolid   = true;
    public tileType      tile_Type = tileType.void_tile;

    public Tile(tileType tileType, boolean isSolid) {
        this.tile_Type = tileType;
        this.isSolid   = isSolid;
    }

    public boolean isSolid() {
        return isSolid;
    }

    public enum tileType {
        floor, grass, hedge, wall, wall_alt, water, void_tile
    }

}
