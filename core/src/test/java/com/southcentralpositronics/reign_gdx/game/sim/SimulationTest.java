package com.southcentralpositronics.reign_gdx.game.sim;

import com.badlogic.gdx.Gdx;
import com.southcentralpositronics.reign_gdx.engine.input.PlayerInput;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationTest {
    private static final long SEED = 42L;

    @Test
    void playerHoldingRightMovesOneHundredTwentyPixelsInSixtyTicks() {
        Simulation simulation = new Simulation(TestArenas.parse(
            "############",
            "#P.........#",
            "############"), SEED);
        simulation.clearEnemies(); // Wave 1 would block the player's path; this test is about speed.
        EntityState start = simulation.getPlayer();

        simulation.setInput(PlayerInput.move(1, 0));
        tick(simulation, 60);

        EntityState end = simulation.getPlayer();
        assertEquals(start.x() + 120, end.x(), 1e-9);
        assertEquals(start.y(), end.y(), 1e-9);
    }

    @Test
    void wallStopsThePlayerFlushAgainstIt() {
        Simulation simulation = new Simulation(TestArenas.parse(
            "#########",
            "#....P..#",
            "#########"), SEED);

        simulation.setInput(PlayerInput.move(-1, 0));
        tick(simulation, 60);

        // The wall's right edge is at x = 16, and the collision box is 16 px wide.
        assertEquals(16 + 8, simulation.getPlayer().x(), 1e-9);
    }

    @Test
    void simulationRunsWithoutAnApplicationOrOpenGl() {
        Simulation simulation = new Simulation(TestArenas.open(8, 8, 3, 3), SEED);
        simulation.setInput(new PlayerInput(1, 1, 100, 100, true));
        tick(simulation, 120);

        assertNull(Gdx.app);
        assertNull(Gdx.gl);
        assertNull(Gdx.audio);
        assertEquals(120, simulation.getTickCount());
    }

    @Test
    void castingRaisesACastEventThatReadingClears() {
        Simulation simulation = new Simulation(TestArenas.open(7, 5, 3, 2), SEED);
        simulation.drainEvents();

        simulation.setInput(PlayerInput.castAt(200, 40));
        simulation.tick();

        List<SimulationEvent> events = simulation.drainEvents();
        assertEquals(1, events.size());
        assertEquals(SimulationEvent.Type.CAST, events.getFirst().type());
        assertEquals(EntityKind.PLAYER, events.getFirst().kind());
        assertTrue(simulation.drainEvents().isEmpty());
    }

    @Test
    void playerCastsEightSpellsPerSecondWhileCasting() {
        Simulation simulation = new Simulation(TestArenas.open(7, 5, 3, 2), SEED);
        simulation.drainEvents();

        simulation.setInput(PlayerInput.castAt(200, 40));
        tick(simulation, 60);

        assertEquals(8, count(simulation.drainEvents(), SimulationEvent.Type.CAST));
    }

    @Test
    void killingAnEnemyWithSpellsScoresItAndRaisesHitAndDeathEvents() {
        Simulation simulation = new Simulation(TestArenas.open(9, 5, 2, 2), SEED);
        simulation.spawnEnemy(EntityKind.DUMMY, 5, 2);
        simulation.drainEvents();

        List<SimulationEvent> events = new ArrayList<>();
        for (int i = 0; i < 600 && simulation.getEnemyCount() > 0; i++) {
            EntityState dummy = simulation.getEntities().stream()
                .filter(entity -> entity.kind() == EntityKind.DUMMY).findFirst().orElseThrow();
            // Aim at the middle of the body, above the collision box at the feet.
            simulation.setInput(PlayerInput.castAt((float) dummy.x(), (float) dummy.y() + 8));
            simulation.tick();
            events.addAll(simulation.drainEvents());
        }

        assertEquals(0, simulation.getEnemyCount());
        assertEquals(10, count(events, SimulationEvent.Type.HIT));
        assertEquals(1, count(events, SimulationEvent.Type.ENEMY_DEATH));
        assertEquals(Simulation.POINTS_PER_KILL, simulation.getScore());
    }

    private static long count(List<SimulationEvent> events, SimulationEvent.Type type) {
        return events.stream().filter(event -> event.type() == type).count();
    }

    private static void tick(Simulation simulation, int ticks) {
        for (int i = 0; i < ticks; i++) simulation.tick();
    }
}
