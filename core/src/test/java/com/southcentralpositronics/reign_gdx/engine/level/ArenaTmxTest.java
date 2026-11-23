package com.southcentralpositronics.reign_gdx.engine.level;

import com.badlogic.gdx.files.FileHandle;
import com.southcentralpositronics.reign_gdx.engine.util.Vector2i;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Loads broken TMX arenas and checks that each fails with an error that names what's wrong. */
class ArenaTmxTest {
    private static final String PLAYER_START = "<object id=\"1\" type=\"PlayerStart\" x=\"24\" y=\"24\"><point/></object>";
    private static final String SPAWN_POINT  = "<object id=\"2\" type=\"SpawnPoint\" x=\"40\" y=\"40\"><point/></object>";

    @TempDir
    Path folder;

    @Test
    void arenaWithoutAPlayerStartNamesTheMissingPlayerStart() throws IOException {
        FileHandle tmx = write("no-start.tmx", SPAWN_POINT);

        InvalidArenaException error = assertThrows(InvalidArenaException.class, () -> Arena.fromTmx(tmx));

        assertTrue(error.getMessage().contains("no-start.tmx"), error.getMessage());
        assertTrue(error.getMessage().contains("player start"), error.getMessage());
        assertTrue(error.getMessage().contains("PlayerStart"), error.getMessage());
    }

    @Test
    void arenaWithoutSpawnPointsNamesTheMissingSpawnPoints() throws IOException {
        FileHandle tmx = write("no-spawns.tmx", PLAYER_START);

        InvalidArenaException error = assertThrows(InvalidArenaException.class, () -> Arena.fromTmx(tmx));

        assertTrue(error.getMessage().contains("no-spawns.tmx"), error.getMessage());
        assertTrue(error.getMessage().contains("spawn point"), error.getMessage());
        assertTrue(error.getMessage().contains("SpawnPoint"), error.getMessage());
    }

    @Test
    void spawnPointOutsideTheMapNamesItsTile() throws IOException {
        FileHandle tmx = write("outside.tmx", PLAYER_START,
                               "<object id=\"3\" type=\"SpawnPoint\" x=\"88\" y=\"8\"><point/></object>");

        InvalidArenaException error = assertThrows(InvalidArenaException.class, () -> Arena.fromTmx(tmx));

        assertTrue(error.getMessage().contains("spawn point at tile (5, 0)"), error.getMessage());
        assertTrue(error.getMessage().contains("outside the map"), error.getMessage());
    }

    @Test
    void validArenaLoadsItsStartAndSpawnPoints() throws IOException {
        Arena arena = Arena.fromTmx(write("valid.tmx", PLAYER_START, SPAWN_POINT));

        // Tiled's y runs down and the arena's runs up: pixel row 24 of 64 is tile row 2.
        assertEquals(Optional.of(new Vector2i(1, 2)), arena.getPlayerStart());
        assertEquals(List.of(new Vector2i(2, 1)), arena.getSpawnPoints());
    }

    /**
     * Writes a 4x4 grass arena, with its tileset inside the TMX file, and the
     * given objects in its object layer.
     */
    private FileHandle write(String name, String... objects) throws IOException {
        String tmx = """
            <?xml version="1.0" encoding="UTF-8"?>
            <map version="1.10" orientation="orthogonal" renderorder="right-down" width="4" height="4" tilewidth="16" tileheight="16" infinite="0">
             <tileset firstgid="1" name="tiles" tilewidth="16" tileheight="16" tilecount="1" columns="0">
              <tile id="0">
               <properties>
                <property name="tileType" value="grass"/>
                <property name="blocksMovement" type="bool" value="false"/>
                <property name="blocksSpells" type="bool" value="false"/>
               </properties>
               <image width="16" height="16" source="grass.png"/>
              </tile>
             </tileset>
             <layer id="1" name="Tiles" width="4" height="4">
              <data encoding="csv">1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1</data>
             </layer>
             <objectgroup id="2" name="Objects">
            %s
             </objectgroup>
            </map>
            """.formatted(String.join("\n", objects));
        Path file = folder.resolve(name);
        Files.writeString(file, tmx);
        return new FileHandle(file.toFile());
    }
}
