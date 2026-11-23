package com.southcentralpositronics.reign_gdx.engine.level;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;
import com.southcentralpositronics.reign_gdx.engine.util.Vector2i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Turns a Tiled TMX file into an {@link Arena}. {@link Arena#fromTmx} documents the format. */
final class TmxArenaReader {
    private final FileHandle tmxFile;
    private final TiledMap   map;
    private final int        width;
    private final int        height;

    private TmxArenaReader(FileHandle tmxFile) {
        this.tmxFile = tmxFile;
        try {
            this.map = new HeadlessTmxLoader().load(tmxFile);
        } catch (RuntimeException e) {
            throw new InvalidArenaException("Can't read arena " + tmxFile.path() + ": " + e.getMessage(), e);
        }
        MapProperties properties = map.getProperties();
        this.width  = properties.get("width", 0, Integer.class);
        this.height = properties.get("height", 0, Integer.class);
        int tileWidth  = properties.get("tilewidth", 0, Integer.class);
        int tileHeight = properties.get("tileheight", 0, Integer.class);
        if (width <= 0 || height <= 0) throw fail("has no tiles; give the map a width and height");
        if (tileWidth != Tile.SIZE || tileHeight != Tile.SIZE) {
            throw fail("has " + tileWidth + "x" + tileHeight + " tiles; arenas use " + Tile.SIZE + "x" + Tile.SIZE + " tiles");
        }
    }

    static Arena read(FileHandle tmxFile) {
        return new TmxArenaReader(tmxFile).toArena();
    }

    private Arena toArena() {
        Tile[][]       tiles       = readTiles();
        Vector2i       playerStart = null;
        List<Vector2i> spawnPoints = new ArrayList<>();
        for (MapLayer layer : map.getLayers()) {
            if (layer instanceof TiledMapTileLayer) continue;
            for (MapObject object : layer.getObjects()) {
                if (is(object, Arena.PLAYER_START)) {
                    if (playerStart != null) throw fail("has more than one player start; keep one " + Arena.PLAYER_START + " object");
                    playerStart = openTileOf(object, tiles, "the player start");
                } else if (is(object, Arena.SPAWN_POINT)) {
                    spawnPoints.add(openTileOf(object, tiles, "a spawn point"));
                }
            }
        }
        if (playerStart == null) {
            throw fail("has no player start; add an object of class " + Arena.PLAYER_START + " to an object layer");
        }
        if (spawnPoints.isEmpty()) {
            throw fail("has no spawn points; add at least one object of class " + Arena.SPAWN_POINT + " to an object layer");
        }
        return new Arena(tiles, playerStart, spawnPoints);
    }

    /** Builds the error for a problem, which completes the sentence "Arena FILE ...". */
    private InvalidArenaException fail(String problem) {
        return new InvalidArenaException("Arena " + tmxFile.path() + " " + problem);
    }

    /** Builds the tile grid. A cell in a higher layer covers the cells below it; an empty cell is void. */
    private Tile[][] readTiles() {
        Tile[][] tiles = new Tile[width][height];
        for (TiledMapTileLayer layer : map.getLayers().getByType(TiledMapTileLayer.class)) {
            for (int tx = 0; tx < Math.min(width, layer.getWidth()); tx++) {
                for (int ty = 0; ty < Math.min(height, layer.getHeight()); ty++) {
                    TiledMapTileLayer.Cell cell = layer.getCell(tx, ty);
                    if (cell != null && cell.getTile() != null) tiles[tx][ty] = toTile(cell.getTile());
                }
            }
        }
        for (int tx = 0; tx < width; tx++) {
            for (int ty = 0; ty < height; ty++) {
                if (tiles[tx][ty] == null) tiles[tx][ty] = new Tile(Tile.tileType.void_tile, true);
                tiles[tx][ty].x = tx;
                tiles[tx][ty].y = ty;
            }
        }
        return tiles;
    }

    private Tile toTile(TiledMapTile mapTile) {
        MapProperties properties = mapTile.getProperties();
        String        typeName   = properties.get(Arena.TILE_TYPE, String.class);
        // The ID is the map's global tile ID: the tileset's first ID plus the tile's ID in Tiled.
        String        tileName   = "the tile with global ID " + mapTile.getId();
        if (typeName == null) throw fail("uses " + tileName + ", which has no " + Arena.TILE_TYPE + " property");
        Tile.tileType type;
        try {
            type = Tile.tileType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            throw fail("uses " + tileName + ", whose " + Arena.TILE_TYPE + " \"" + typeName + "\" isn't one of "
                       + Arrays.toString(Tile.tileType.values()));
        }
        return new Tile(type, flag(properties, Arena.BLOCKS_MOVEMENT), flag(properties, Arena.BLOCKS_SPELLS));
    }

    private static boolean flag(MapProperties properties, String name) {
        return Boolean.TRUE.equals(properties.get(name, Boolean.class));
    }

    /** Checks whether an object's class (its type) or its name is the given kind. */
    private static boolean is(MapObject object, String kind) {
        return kind.equals(object.getProperties().get("type", String.class)) || kind.equals(object.getName());
    }

    /**
     * Finds the tile under an object's center, and checks that a mob can stand
     * there. The loader has already flipped y to point up.
     */
    private Vector2i openTileOf(MapObject object, Tile[][] tiles, String what) {
        if (!(object instanceof RectangleMapObject rectangle)) {
            throw fail("marks " + what + " with a shape other than a point or a rectangle (object ID "
                       + object.getProperties().get("id") + ")");
        }
        Rectangle box = rectangle.getRectangle();
        int       tx  = (int) Math.floor((box.x + box.width / 2) / Tile.SIZE);
        int       ty  = (int) Math.floor((box.y + box.height / 2) / Tile.SIZE);
        // Report the tile the way Tiled shows it: the row counts down from the top.
        String place = "places " + what + " at tile (" + tx + ", " + (height - 1 - ty) + ") in Tiled, ";
        if (tx < 0 || ty < 0 || tx >= width || ty >= height) throw fail(place + "outside the map");
        if (tiles[tx][ty].isSolid()) throw fail(place + "on a tile that blocks movement");
        return new Vector2i(tx, ty);
    }
}
