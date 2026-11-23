package com.southcentralpositronics.reign_gdx.engine.level;

import com.badlogic.gdx.files.FileHandle;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;
import com.southcentralpositronics.reign_gdx.engine.util.Vector2i;

import java.util.List;
import java.util.Optional;

/**
 * The layout of a level before play starts: its tiles, the player start, and
 * the spawn points. A simulation builds its level from an arena, so an arena
 * holds no entities and no textures.
 */
public final class Arena {
    /** The tile property that names the tile type, one of {@link Tile.TileType}. */
    public static final String TILE_TYPE       = "tileType";
    /** The boolean tile property that stops mobs from walking onto the tile. */
    public static final String BLOCKS_MOVEMENT = "blocksMovement";
    /** The boolean tile property that stops spells at the tile. */
    public static final String BLOCKS_SPELLS   = "blocksSpells";
    /** The class (or name) of the object that marks the player start. */
    public static final String PLAYER_START    = "PlayerStart";
    /** The class (or name) of an object that marks a spawn point. */
    public static final String SPAWN_POINT     = "SpawnPoint";

    private final Tile[][]       tiles;
    private final Vector2i       playerStart;
    private final List<Vector2i> spawnPoints;

    /**
     * Creates an arena from a tile grid, with no spawn points.
     *
     * @param tiles       The tiles, indexed {@code [x][y]} with y up.
     * @param playerStart The tile where the player appears, or null to let the
     *                    simulation pick a random open tile.
     */
    public Arena(Tile[][] tiles, Vector2i playerStart) {
        this(tiles, playerStart, List.of());
    }

    /**
     * Creates an arena from a tile grid.
     *
     * @param tiles       The tiles, indexed {@code [x][y]} with y up.
     * @param playerStart The tile where the player appears, or null to let the
     *                    simulation pick a random open tile.
     * @param spawnPoints The tiles where enemies enter, in the order the arena
     *                    lists them.
     */
    public Arena(Tile[][] tiles, Vector2i playerStart, List<Vector2i> spawnPoints) {
        if (tiles.length == 0 || tiles[0].length == 0) throw new IllegalArgumentException("An arena needs at least one tile");
        this.tiles       = tiles;
        this.playerStart = playerStart;
        this.spawnPoints = List.copyOf(spawnPoints);
    }

    /**
     * Loads an arena from a Tiled TMX file, with no window, OpenGL context, or
     * textures.
     *
     * <p>The map must be orthogonal, with 16x16 tiles. Each tile in its
     * tilesets has a {@link #TILE_TYPE} string property and the
     * {@link #BLOCKS_MOVEMENT} and {@link #BLOCKS_SPELLS} boolean properties;
     * a missing flag counts as false. Where tile layers overlap, the highest
     * layer wins, and a cell that no layer fills is void.
     *
     * <p>Object layers hold one {@link #PLAYER_START} object and at least one
     * {@link #SPAWN_POINT} object, matched by class or by name. Each marks the
     * tile under its center, so a point or a rectangle works.
     *
     * @param tmxFile The TMX file. Tilesets and images resolve relative to it.
     * @return The arena.
     * @throws InvalidArenaException If the file can't be read, or it lacks a
     *                               player start, spawn points, or tile
     *                               properties.
     */
    public static Arena fromTmx(FileHandle tmxFile) {
        return TmxArenaReader.read(tmxFile);
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

    /** Gets the tiles where enemies enter, in the order the arena lists them. The list can't change. */
    public List<Vector2i> getSpawnPoints() {
        return spawnPoints;
    }
}
