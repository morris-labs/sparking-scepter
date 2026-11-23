package com.southcentralpositronics.reign_gdx.game.sim;

import com.southcentralpositronics.reign_gdx.engine.input.PlayerInput;
import com.southcentralpositronics.reign_gdx.engine.level.Arena;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Scenarios for waves and the breaks between them. */
class WaveTest {
    private static final long SEED = 42L;

    @Test
    void runStartsWithAThreeSecondBreakThenWaveOne() {
        Simulation simulation = new Simulation(corridor(), SEED);

        assertTrue(simulation.isBreak());
        assertEquals(0, simulation.getWave());
        assertEquals(3f, simulation.getBreakSecondsLeft(), 1e-6);
        assertEquals(0, simulation.getEnemyCount());

        List<SimulationEvent> events = new ArrayList<>(simulation.drainEvents());
        tick(simulation, 179, events);
        assertTrue(simulation.isBreak());
        assertEquals(0, count(events, SimulationEvent.Type.WAVE_START));

        tick(simulation, 1, events);
        assertFalse(simulation.isBreak());
        assertEquals(1, simulation.getWave());
        assertEquals(0f, simulation.getBreakSecondsLeft());
        assertTrue(simulation.getEnemyCount() > 0);
        assertEquals(1, count(events, SimulationEvent.Type.WAVE_START));
    }

    @Test
    void waveEndsWhenItsLastEnemyDiesAndAFiveSecondBreakAfterTheUpgradeLeadsToTheNextWave() {
        Simulation simulation = new Simulation(corridor(), SEED);
        tick(simulation, 180, new ArrayList<>());
        assertEquals(1, simulation.getWave());

        // Fire down the corridor at the nearest enemy until the wave is gone.
        int ticks = 0;
        while (simulation.getEnemyCount() > 0 && ticks < 3600) {
            assertFalse(simulation.isBreak(), "the break started while enemies were left");
            EntityState target = nearestEnemy(simulation);
            simulation.setInput(PlayerInput.castAt((float) target.x(), (float) target.y() + 8));
            simulation.tick();
            ticks++;
        }
        simulation.setInput(PlayerInput.NONE);
        simulation.drainEvents();

        assertEquals(0, simulation.getEnemyCount(), "the player never cleared wave 1");
        assertTrue(simulation.isBreak());
        assertEquals(5f, simulation.getBreakSecondsLeft(), 1e-6);
        assertEquals(1, simulation.getWave());

        // The break waits for an upgrade; once one is chosen, it runs for 5 seconds.
        simulation.chooseUpgrade(simulation.getUpgradeOffers().getFirst());
        List<SimulationEvent> events = new ArrayList<>();
        tick(simulation, 299, events);
        assertTrue(simulation.isBreak());
        assertEquals(1, simulation.getWave());
        assertEquals(0, simulation.getEnemyCount());

        tick(simulation, 1, events);
        assertFalse(simulation.isBreak());
        assertEquals(2, simulation.getWave());
        assertTrue(simulation.getEnemyCount() > 0);
        assertEquals(1, count(events, SimulationEvent.Type.WAVE_START));
    }

    @Test
    void enemiesEnterOnlyAtSpawnPointsAwayFromThePlayer() {
        for (long seed = 1; seed <= 5; seed++) {
            // The left spawn point is 2 tiles from the player; the right one is 8.
            Simulation simulation = new Simulation(TestArenas.parse(
                "#############",
                "#S.P.......S#",
                "#############"), seed);
            tick(simulation, 180, new ArrayList<>());

            assertEquals(1, simulation.getWave());
            assertTrue(simulation.getEnemyCount() > 0);
            assertAllEnemiesOnTile(simulation, 11, 1);
        }
    }

    @Test
    void enemiesEnterAtTheFarthestSpawnPointWhenNoneIsFarEnough() {
        for (long seed = 1; seed <= 5; seed++) {
            // The left spawn point is 3 tiles from the player and the right one 2; both are too close.
            Simulation simulation = new Simulation(TestArenas.parse(
                "########",
                "#S..P.S#",
                "########"), seed);
            tick(simulation, 180, new ArrayList<>());

            assertEquals(1, simulation.getWave());
            assertTrue(simulation.getEnemyCount() > 0);
            assertAllEnemiesOnTile(simulation, 1, 1);
        }
    }

    /** Checks that every enemy stands where a mob placed on the tile does: separation hasn't moved them yet. */
    private static void assertAllEnemiesOnTile(Simulation simulation, int tx, int ty) {
        for (EntityState enemy : simulation.getEntities()) {
            if (!enemy.kind().isEnemy()) continue;
            assertEquals(tx * 16 + 8, enemy.x(), 1e-9);
            assertEquals(ty * 16 + 4, enemy.y(), 1e-9);
        }
    }

    @Test
    void clearMobsHoldsTheRunUntilAddMobs() {
        Simulation simulation = new Simulation(corridor(), SEED);
        tick(simulation, 180, new ArrayList<>());
        assertEquals(1, simulation.getWave());

        simulation.clearEnemies();
        tick(simulation, 600, new ArrayList<>());
        assertEquals(0, simulation.getEnemyCount());
        assertFalse(simulation.isBreak(), "an emptied wave ended while Clear Mobs held it");
        assertEquals(1, simulation.getWave());

        simulation.addEnemies();
        assertEquals(4, simulation.getEnemyCount());
        assertEquals(1, simulation.getWave());
    }

    @Test
    void theSameSeedSendsTheSameWaveAndItMovesTheSameWay() {
        assertEquals(enemiesTwoSecondsIntoWaveOne(SEED), enemiesTwoSecondsIntoWaveOne(SEED));
    }

    @Test
    void differentSeedsSendTheWaveToDifferentSpawnPoints() {
        assertNotEquals(enemiesTwoSecondsIntoWaveOne(1L), enemiesTwoSecondsIntoWaveOne(2L));
    }

    /** Runs a 30x30 arena with four spawn points into wave 1 and lists the enemies. Particles aren't seeded. */
    private static List<EntityState> enemiesTwoSecondsIntoWaveOne(long seed) {
        Simulation simulation = new Simulation(openWithSpawnPointsInTheCorners(), seed);
        tick(simulation, 180 + 120, new ArrayList<>());
        assertEquals(1, simulation.getWave());
        return simulation.getEntities().stream().filter(entity -> entity.kind().isEnemy()).toList();
    }

    /** A 30x30 walled grass box with the player in the middle and a spawn point near each corner. */
    private static Arena openWithSpawnPointsInTheCorners() {
        String[] rows = new String[30];
        for (int row = 0; row < 30; row++) {
            StringBuilder builder = new StringBuilder();
            for (int column = 0; column < 30; column++) {
                boolean border = row == 0 || column == 0 || row == 29 || column == 29;
                boolean corner = (row == 2 || row == 27) && (column == 2 || column == 27);
                builder.append(border ? '#' : corner ? 'S' : row == 15 && column == 15 ? 'P' : '.');
            }
            rows[row] = builder.toString();
        }
        return TestArenas.parse(rows);
    }

    /** A corridor with the player at the left end and one spawn point at the right, seven tiles away. */
    private static Arena corridor() {
        return TestArenas.parse(
            "##########",
            "#P......S#",
            "##########");
    }

    private static EntityState nearestEnemy(Simulation simulation) {
        EntityState player = simulation.getPlayer();
        return simulation.getEntities().stream().filter(entity -> entity.kind().isEnemy())
            .min(java.util.Comparator.comparingDouble(enemy -> Math.hypot(enemy.x() - player.x(), enemy.y() - player.y())))
            .orElseThrow();
    }

    static long count(List<SimulationEvent> events, SimulationEvent.Type type) {
        return events.stream().filter(event -> event.type() == type).count();
    }

    static void tick(Simulation simulation, int ticks, List<SimulationEvent> events) {
        for (int i = 0; i < ticks; i++) {
            simulation.tick();
            events.addAll(simulation.drainEvents());
        }
    }
}
