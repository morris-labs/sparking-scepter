package com.southcentralpositronics.reign_gdx.game.sim;

import com.southcentralpositronics.reign_gdx.engine.input.PlayerInput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private static void tick(Simulation simulation, int ticks) {
        for (int i = 0; i < ticks; i++) simulation.tick();
    }
}
