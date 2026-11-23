package com.southcentralpositronics.reign_gdx.game.sim;

import com.southcentralpositronics.reign_gdx.engine.input.PlayerInput;
import com.southcentralpositronics.reign_gdx.engine.time.FixedTimestep;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Drives a simulation the way the game does: real frame times through a fixed timestep. */
class FrameRateTest {
    @ParameterizedTest(name = "{0} FPS")
    @ValueSource(ints = {30, 60, 144})
    void playerMovesOneHundredTwentyPixelsPerSecondAtAnyFrameRate(int framesPerSecond) {
        Simulation    simulation = new Simulation(TestArenas.open(16, 5, 1, 2), 7L);
        FixedTimestep timestep   = new FixedTimestep();
        double        startX     = simulation.getPlayer().x();

        simulation.setInput(PlayerInput.move(1, 0));
        for (int frame = 0; frame < framesPerSecond; frame++) {
            for (int ticks = timestep.advance(1f / framesPerSecond); ticks > 0; ticks--) simulation.tick();
        }

        assertEquals(60, simulation.getTickCount());
        assertEquals(startX + 120, simulation.getPlayer().x(), 1e-9);
    }
}
