package com.southcentralpositronics.reign_gdx.engine.time;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
