package com.southcentralpositronics.reign_gdx.game.sim;

import com.southcentralpositronics.reign_gdx.engine.input.PlayerInput;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Scenarios for the player's health, death, and the end of the run. */
class PlayerDeathTest {
    private static final long SEED = 42L;

    @Test
    void chaserTouchingThePlayerCostsHealthAndRaisesPlayerDamage() {
        Simulation simulation = new Simulation(TestArenas.parse(
            "#######",
            "#P....#",
            "#######"), SEED);
        simulation.spawnEnemy(EntityKind.CHASER, 3, 1);
        List<SimulationEvent> events = new ArrayList<>();

        tick(simulation, 120, events);

        assertTrue(simulation.getPlayer().health() < 1.0, "contact cost no health");
        assertTrue(count(events, SimulationEvent.Type.PLAYER_DAMAGE) > 0);
        assertFalse(simulation.isRunOver());
    }

    @Test
    void playerAtZeroHealthEndsTheRunAndRaisesOnePlayerDeath() {
        Simulation simulation = new Simulation(TestArenas.parse(
            "#######",
            "#P....#",
            "#######"), SEED);
        simulation.spawnEnemy(EntityKind.CHASER, 3, 1);
        List<SimulationEvent> events = new ArrayList<>();

        int ticks = 0;
        while (!simulation.isRunOver() && ticks < 3600) {
            simulation.tick();
            events.addAll(simulation.drainEvents());
            ticks++;
        }
        tick(simulation, 120, events);

        assertTrue(simulation.isRunOver(), "the player never died");
        assertEquals(0.0, simulation.getPlayer().health(), 1e-9);
        assertEquals(1, count(events, SimulationEvent.Type.PLAYER_DEATH));
        assertEquals(EntityKind.PLAYER, events.stream()
            .filter(event -> event.type() == SimulationEvent.Type.PLAYER_DEATH).findFirst().orElseThrow().kind());
    }

    @Test
    void deadPlayerNeitherMovesNorCastsNorTakesDamage() {
        // The chaser stays on the player's row, so only the player's input could move it up.
        Simulation simulation = new Simulation(TestArenas.open(9, 5, 2, 2), SEED);
        simulation.spawnEnemy(EntityKind.CHASER, 4, 2);
        int ticks = 0;
        while (!simulation.isRunOver() && ticks++ < 3600) simulation.tick();
        simulation.drainEvents();
        EntityState dead = simulation.getPlayer();

        simulation.setInput(new PlayerInput(0, 1, 1000, (float) dead.y(), true));
        List<SimulationEvent> events = new ArrayList<>();
        tick(simulation, 60, events);

        assertEquals(dead.y(), simulation.getPlayer().y(), 1e-9);
        assertEquals(0, count(events, SimulationEvent.Type.CAST));
        assertEquals(0, count(events, SimulationEvent.Type.PLAYER_DAMAGE));
    }

    @Test
    void dummyNeverHurtsThePlayer() {
        Simulation simulation = new Simulation(TestArenas.parse(
            "#####",
            "#P..#",
            "#####"), SEED);
        simulation.spawnEnemy(EntityKind.DUMMY, 2, 1);

        tick(simulation, 600, new ArrayList<>());

        assertEquals(1.0, simulation.getPlayer().health(), 1e-9);
    }

    private static long count(List<SimulationEvent> events, SimulationEvent.Type type) {
        return events.stream().filter(event -> event.type() == type).count();
    }

    private static void tick(Simulation simulation, int ticks, List<SimulationEvent> events) {
        for (int i = 0; i < ticks; i++) {
            simulation.tick();
            events.addAll(simulation.drainEvents());
        }
    }
}
