package com.southcentralpositronics.reign_gdx.game.sim;

import com.southcentralpositronics.reign_gdx.engine.level.Arena;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;
import com.southcentralpositronics.reign_gdx.engine.util.Vector2i;

/**
 * Builds small arenas from text, one character per tile, so a test shows its
 * layout. The first row is the top of the arena, as in a level PNG file.
 *
 * <ul>
 *   <li>{@code .}: grass
 *   <li>{@code #}: wall
 *   <li>{@code ~}: water
 *   <li>{@code P}: grass with the player start on it
 * </ul>
 */
final class TestArenas {
    private TestArenas() {
    }

    static Arena parse(String... rows) {
        int      width       = rows[0].length();
        int      height      = rows.length;
        Tile[][] tiles       = new Tile[width][height];
        Vector2i playerStart = null;

        for (int row = 0; row < height; row++) {
            if (rows[row].length() != width) throw new IllegalArgumentException("Row " + row + " isn't " + width + " wide");
            int ty = height - 1 - row;
            for (int tx = 0; tx < width; tx++) {
                char symbol = rows[row].charAt(tx);
                tiles[tx][ty] = switch (symbol) {
                    case '.', 'P' -> new Tile(Tile.tileType.grass, false);
                    case '#' -> new Tile(Tile.tileType.wall, true);
                    case '~' -> new Tile(Tile.tileType.water, false);
                    default -> throw new IllegalArgumentException("Unknown tile symbol: " + symbol);
                };
                if (symbol == 'P') playerStart = new Vector2i(tx, ty);
            }
        }
        return new Arena(tiles, playerStart);
    }

    /** Builds an open grass arena with a wall border and the player start at the given tile. */
    static Arena open(int width, int height, int startX, int startY) {
        String[] rows = new String[height];
        for (int row = 0; row < height; row++) {
            int           ty      = height - 1 - row;
            StringBuilder builder = new StringBuilder();
            for (int tx = 0; tx < width; tx++) {
                boolean border = tx == 0 || ty == 0 || tx == width - 1 || ty == height - 1;
                builder.append(border ? '#' : tx == startX && ty == startY ? 'P' : '.');
            }
            rows[row] = builder.toString();
        }
        return parse(rows);
    }
}
