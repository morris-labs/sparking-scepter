package com.southcentralpositronics.reign_gdx.engine.time;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedTimestepTest {
    @Test
    void aThirtyFpsFrameRunsTwoTicks() {
        assertEquals(2, new FixedTimestep().advance(1f / 30));
    }

    @Test
    void shortFramesCarryTheirTimeUntilATickIsDue() {
        FixedTimestep timestep = new FixedTimestep();

        assertEquals(0, timestep.advance(1f / 144));
        assertEquals(0, timestep.advance(1f / 144));
        assertEquals(1, timestep.advance(1f / 144));
    }

    @Test
    void aLongStallRunsAtMostAQuarterSecondOfTicks() {
        assertEquals(15, new FixedTimestep().advance(3f));
    }

    @Test
    void convertsPerSecondRatesToPerTick() {
        assertEquals(2.0, FixedTimestep.perTick(120), 1e-12);
    }

    @Test
    void aPausedTimestepRunsNoTicks() {
        FixedTimestep timestep = new FixedTimestep();

        timestep.pause();

        assertTrue(timestep.isPaused());
        assertEquals(0, timestep.advance(1f / 30));
        assertEquals(0, timestep.advance(0.25f));
    }

    @Test
    void resumingDoesNotMakeUpThePausedTime() {
        FixedTimestep timestep = new FixedTimestep();
        timestep.pause();
        timestep.advance(0.25f);

        timestep.resume();

        assertFalse(timestep.isPaused());
        assertEquals(2, timestep.advance(1f / 30));
    }

    @Test
    void aPartTickBeforeThePauseCarriesOverAfterIt() {
        FixedTimestep timestep = new FixedTimestep();
        timestep.advance(1f / 120);

        timestep.pause();
        timestep.advance(1f);
        timestep.resume();

        assertEquals(1, timestep.advance(1f / 120));
    }
}
