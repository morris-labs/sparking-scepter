package com.southcentralpositronics.reign_gdx.engine.tools;

import com.southcentralpositronics.reign_gdx.engine.graphics.TileLoader;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Converts a level PNG file into a Tiled TMX arena, once, by hand. Each pixel
 * becomes one tile through the {@link TileLoader} color table, and an unknown
 * color becomes void, as the PNG loader did. The output uses the shared
 * tileset {@code tiles.tsx} in the same folder, and its object layer holds the
 * player start and spawn points given on the command line.
 *
 * <p>Usage, with tile coordinates as Tiled shows them (row 0 at the top):
 *
 * <pre>
 * PngToTmxConverter INPUT.png OUTPUT.tmx START_X,START_Y SPAWN_X,SPAWN_Y [SPAWN_X,SPAWN_Y ...]
 * </pre>
 */
public final class PngToTmxConverter {
    /** The tile types in the order of the tile IDs in {@code tiles.tsx}. */
    private static final List<Tile.tileType> TILESET_ORDER = List.of(
        Tile.tileType.grass, Tile.tileType.floor, Tile.tileType.water, Tile.tileType.hedge,
        Tile.tileType.wall, Tile.tileType.wall_alt, Tile.tileType.void_tile);

    private PngToTmxConverter() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println("Usage: PngToTmxConverter INPUT.png OUTPUT.tmx START_X,START_Y SPAWN_X,SPAWN_Y [...]");
            System.exit(2);
        }
        BufferedImage image = ImageIO.read(Path.of(args[0]).toFile());
        if (image == null) throw new IOException("Can't read " + args[0] + " as an image");
        List<int[]> spawnPoints = new ArrayList<>();
        for (int i = 3; i < args.length; i++) spawnPoints.add(parsePoint(args[i]));
        Files.writeString(Path.of(args[1]), toTmx(image, parsePoint(args[2]), spawnPoints));
    }

    private static int[] parsePoint(String text) {
        String[] parts = text.split(",");
        return new int[]{Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim())};
    }

    static String toTmx(BufferedImage image, int[] playerStart, List<int[]> spawnPoints) {
        Map<Integer, Tile> colors = TileLoader.loadTiles();
        int                width  = image.getWidth();
        int                height = image.getHeight();

        StringBuilder data = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Tile          tile = colors.get(image.getRGB(x, y));
                Tile.tileType type = tile == null ? Tile.tileType.void_tile : tile.tile_Type;
                data.append(TILESET_ORDER.indexOf(type) + 1);
                if (x < width - 1 || y < height - 1) data.append(',');
            }
            data.append('\n');
        }

        StringBuilder objects = new StringBuilder();
        int           id      = 1;
        objects.append(object(id++, "PlayerStart", playerStart));
        for (int[] spawnPoint : spawnPoints) objects.append(object(id++, "SpawnPoint", spawnPoint));

        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <map version="1.10" tiledversion="1.10.2" orientation="orthogonal" renderorder="right-down" \
            width="%d" height="%d" tilewidth="16" tileheight="16" infinite="0" nextlayerid="3" nextobjectid="%d">
             <tileset firstgid="1" source="tiles.tsx"/>
             <layer id="1" name="Tiles" width="%d" height="%d">
              <data encoding="csv">
            %s</data>
             </layer>
             <objectgroup id="2" name="Objects">
            %s </objectgroup>
            </map>
            """.formatted(width, height, id, width, height, data, objects);
    }

    /** Writes a point object at the center of a tile. */
    private static String object(int id, String kind, int[] tile) {
        return "  <object id=\"%d\" name=\"%s\" type=\"%s\" x=\"%d\" y=\"%d\">\n   <point/>\n  </object>\n"
            .formatted(id, kind, kind, tile[0] * Tile.SIZE + Tile.SIZE / 2, tile[1] * Tile.SIZE + Tile.SIZE / 2);
    }
}
