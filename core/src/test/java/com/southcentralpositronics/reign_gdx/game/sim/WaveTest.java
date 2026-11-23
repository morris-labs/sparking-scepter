package com.southcentralpositronics.reign_gdx.game.sim;

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
