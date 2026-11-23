package com.southcentralpositronics.reign_gdx.game.sim;

import com.badlogic.gdx.files.FileHandle;
import com.southcentralpositronics.reign_gdx.engine.input.PlayerInput;
import com.southcentralpositronics.reign_gdx.engine.level.Arena;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;
import com.southcentralpositronics.reign_gdx.engine.util.Vector2i;
import com.southcentralpositronics.reign_gdx.game.Game;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Plays arenas loaded from Tiled TMX files. The test arena, in
 * {@code core/src/test/arenas/test-arena.tmx}, is 14 by 8 tiles inside a wall:
 *
 * <pre>
 * ##############
 * #P...~~....S.#
 * #....~~......#
 * #............#
 * #S...........#
 * #............#
 * #H%_V........#
 * ##############
 * </pre>
 *
 * <p>{@code P} is the player start, {@code S} a spawn point, {@code ~} water,
 * {@code H} hedge, {@code %} alternate wall, {@code _} floor, and {@code V}
 * void. Rows count up from the bottom, so the player start is tile (1, 6).
 */
class TiledArenaTest {
    private static final long SEED = 42L;

    /**
     * Loads the test arena. Gradle runs the tests in the {@code core} folder; the
     * path is absolute so that the tileset path, which climbs out of that
     * folder, resolves.
     */
    static Arena testArena() {
        return Arena.fromTmx(new FileHandle(new File("src/test/arenas/test-arena.tmx").getAbsoluteFile()));
    }

    @Test
    void playerAppearsAtThePlayerStart() {
        for (long seed = 1; seed <= 5; seed++) {
            Simulation simulation = new Simulation(testArena(), seed);

            EntityState player = simulation.getPlayer();
            assertEquals(1 * 16 + 8, player.x(), 1e-9);
            assertEquals(6 * 16 + 4, player.y(), 1e-9);
        }
    }

    @Test
    void gameArenaIsTheConvertedSpawnLevel() {
        Arena arena = Arena.fromTmx(new FileHandle(new File("../assets/" + Game.ARENA_PATH).getAbsoluteFile()));
        Simulation simulation = new Simulation(arena, SEED);

        // The Java2D game started the player at tile (20, 42), counted from the top of the 75-row map.
        assertEquals(40, simulation.getWidth());
        assertEquals(75, simulation.getHeight());
        assertEquals(20 * 16 + 8, simulation.getPlayer().x(), 1e-9);
        assertEquals((75 - 1 - 42) * 16 + 4, simulation.getPlayer().y(), 1e-9);
        assertEquals(6, simulation.getSpawnPoints().size());
    }

    @Test
    void spawnPointsLoadFromTheObjectLayer() {
        Simulation simulation = new Simulation(testArena(), SEED);

        assertEquals(List.of(new Vector2i(11, 6), new Vector2i(1, 3)), simulation.getSpawnPoints());
    }

    @Test
    void waterBlocksThePlayer() {
        Simulation simulation = new Simulation(testArena(), SEED);

        simulation.setInput(PlayerInput.move(1, 0));
        tick(simulation, 60);

        // The water's left edge is at x = 80, and the collision box is 16 px wide.
        assertEquals(80 - 8, simulation.getPlayer().x(), 1e-9);
    }

    @Test
    void spellFliesOverWaterAndHitsAnEnemyBeyondIt() {
        Simulation simulation = new Simulation(testArena(), SEED);
        simulation.clearEnemies();
        simulation.spawnEnemy(EntityKind.CHASER, 8, 6);
        simulation.drainEvents();

        simulation.setInput(PlayerInput.castAt(128, 108));
        boolean hit = false;
        for (int tick = 0; tick < 30 && !hit; tick++) {
            simulation.tick();
            hit = simulation.drainEvents().stream().anyMatch(event -> event.type() == SimulationEvent.Type.HIT);
        }

        assertTrue(hit, "A spell crosses the water and hits the Chaser");
        assertTrue(simulation.getEntities().stream()
                             .filter(state -> state.kind() == EntityKind.CHASER)
                             .allMatch(state -> state.x() >= 7 * 16 + 8), "The Chaser stays behind the water");
    }

    @Test
    void tilePropertiesSetWhatBlocksMovementAndSpells() {
        Simulation simulation = new Simulation(testArena(), SEED);

        assertBlocks(simulation, 0, 0, Tile.tileType.wall, true, true);
        assertBlocks(simulation, 1, 1, Tile.tileType.hedge, true, true);
        assertBlocks(simulation, 2, 1, Tile.tileType.wall_alt, true, true);
        assertBlocks(simulation, 3, 1, Tile.tileType.floor, false, false);
        assertBlocks(simulation, 4, 1, Tile.tileType.void_tile, true, true);
        assertBlocks(simulation, 5, 6, Tile.tileType.water, true, false);
        assertBlocks(simulation, 1, 2, Tile.tileType.grass, false, false);
    }

    private static void assertBlocks(Simulation simulation, int tx, int ty, Tile.tileType type,
                                     boolean blocksMovement, boolean blocksSpells) {
        Tile tile = simulation.getTileAt(tx, ty);
        assertEquals(type, tile.tile_Type, "Tile type at (" + tx + ", " + ty + ")");
        assertEquals(blocksMovement, tile.isSolid(), type + " blocks movement");
        assertEquals(blocksSpells, tile.blocksSpells(), type + " blocks spells");
    }

    private static void tick(Simulation simulation, int ticks) {
        for (int i = 0; i < ticks; i++) simulation.tick();
    }
}
