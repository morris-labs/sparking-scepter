package com.southcentralpositronics.reign_gdx.engine.level.tile;

/**
 * One map tile: its type, whether it blocks movement, and whether it blocks
 * spells. A renderer draws it by its type.
 *
 * <p>The two blocking flags are separate so that a tile such as water can stop
 * mobs but let spells fly over it. Mob movement, mob separation, and A* paths
 * read {@link #blocksMovement()}; spell flight and particles read
 * {@link #blocksSpells()}.
 */
public class Tile {
    /** The width and height of a tile, in world pixels. */
    public static final int SIZE = 16;

    public int x, y;
    /** The type, which picks the tile's image. */
    public final  TileType type;
    private final boolean  blocksMovement;
    private final boolean  blocksSpells;

    /**
     * Creates a tile that blocks movement and spells alike, or neither.
     *
     * @param type   The type, which picks the tile's image.
     * @param blocks Whether the tile blocks both movement and spells.
     */
    public Tile(TileType type, boolean blocks) {
        this(type, blocks, blocks);
    }

    /**
     * Creates a tile.
     *
     * @param type           The type, which picks the tile's image.
     * @param blocksMovement Whether mobs can't walk onto the tile.
     * @param blocksSpells   Whether spells stop when they reach the tile.
     */
    public Tile(TileType type, boolean blocksMovement, boolean blocksSpells) {
        this.type           = type;
        this.blocksMovement = blocksMovement;
        this.blocksSpells   = blocksSpells;
    }

    /** Checks whether the tile blocks movement. */
    public boolean blocksMovement() {
        return blocksMovement;
    }

    /** Checks whether the tile stops spells. */
    public boolean blocksSpells() {
        return blocksSpells;
    }

    public enum TileType {
        floor, grass, hedge, wall, wall_alt, water, void_tile
    }

}
