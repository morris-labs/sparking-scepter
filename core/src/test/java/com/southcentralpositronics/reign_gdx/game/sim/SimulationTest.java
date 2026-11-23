package com.southcentralpositronics.reign_gdx.game.sim;

import com.badlogic.gdx.Gdx;
import com.southcentralpositronics.reign_gdx.engine.input.PlayerInput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SimulationTest {
    private static final long SEED = 42L;

    @Test
    void playerHoldingRightMovesOneHundredTwentyPixelsInSixtyTicks() {
        Simulation simulation = new Simulation(TestArenas.parse(
            "############",
            "#P.........#",
            "############"), SEED);
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

    private static void tick(Simulation simulation, int ticks) {
        for (int i = 0; i < ticks; i++) simulation.tick();
    }
}
