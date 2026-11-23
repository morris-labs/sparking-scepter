package com.southcentralpositronics.reign_gdx.engine.level.tile;

/**
 * One map tile: its type, whether it blocks movement, and whether it blocks
 * spells. A renderer draws it by its type.
 *
 * <p>The two blocking flags are separate so that a tile such as water can stop
 * mobs but let spells fly over it. Mob movement, mob separation, and A* paths
 * read {@link #isSolid()}; spell flight and particles read
 * {@link #blocksSpells()}.
 */
public class Tile {
    /** The width and height of a tile, in world pixels. */
    public static final int SIZE = 16;

    public int x, y;
    public boolean       isSolid      = true;
    public boolean       blocksSpells = true;
    public tileType      tile_Type    = tileType.void_tile;

    /**
     * Creates a tile that blocks movement and spells alike, or neither.
     *
     * @param tileType The type, which picks the tile's image.
     * @param isSolid  Whether the tile blocks both movement and spells.
     */
    public Tile(tileType tileType, boolean isSolid) {
        this(tileType, isSolid, isSolid);
    }

    /**
     * Creates a tile.
     *
     * @param tileType       The type, which picks the tile's image.
     * @param blocksMovement Whether mobs can't walk onto the tile.
     * @param blocksSpells   Whether spells stop when they reach the tile.
     */
    public Tile(tileType tileType, boolean blocksMovement, boolean blocksSpells) {
        this.tile_Type    = tileType;
        this.isSolid      = blocksMovement;
        this.blocksSpells = blocksSpells;
    }

    /** Checks whether the tile blocks movement. */
    public boolean isSolid() {
        return isSolid;
    }

    /** Checks whether the tile stops spells. */
    public boolean blocksSpells() {
        return blocksSpells;
    }

    public enum tileType {
        floor, grass, hedge, wall, wall_alt, water, void_tile
    }

}
