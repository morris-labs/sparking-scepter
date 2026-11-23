package com.southcentralpositronics.reign_gdx.engine.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LevelTest {
    @Test
    void loadsPngTilesWithoutAWindowOrGraphicsContext() throws Exception {
        GdxNativesLoader.load();
        FileHandle image = new FileHandle(
            java.nio.file.Path.of(getClass().getResource("/levels/smoke.png").toURI()).toFile());

        Level level = new Level(image);

        assertNull(Gdx.app);
        assertNull(Gdx.gl);
        assertEquals(3, level.getWidth());
        assertEquals(2, level.getHeight());
        assertEquals(Tile.tileType.grass, level.getTileAt(0, 1).tile_Type);
        assertFalse(level.getTileAt(0, 1).isSolid());
        assertEquals(Tile.tileType.hedge, level.getTileAt(1, 1).tile_Type);
        assertTrue(level.getTileAt(1, 1).isSolid());
        assertEquals(Tile.tileType.water, level.getTileAt(2, 1).tile_Type);
        assertFalse(level.getTileAt(2, 1).isSolid());
        assertEquals(Tile.tileType.wall, level.getTileAt(0, 0).tile_Type);
        assertEquals(Tile.tileType.floor, level.getTileAt(1, 0).tile_Type);
        assertEquals(Tile.tileType.void_tile, level.getTileAt(2, 0).tile_Type);
        assertTrue(level.getTileAt(2, 0).isSolid());
        assertNull(level.getTileAt(3, 0));
    }
}
