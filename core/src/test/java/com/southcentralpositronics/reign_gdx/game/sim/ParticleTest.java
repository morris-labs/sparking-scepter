package com.southcentralpositronics.reign_gdx.game.sim;

import com.southcentralpositronics.reign_gdx.engine.input.PlayerInput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParticleTest {
    private static final long SEED = 42L;
    /** The fewest ticks that a particle lives. */
    private static final int  MIN_LIFE_TICKS = 10;
    /** The most ticks that a particle lives. */
    private static final int  MAX_LIFE_TICKS = 24;

    @Test
    void spellHittingAWallBurstsIntoParticlesThatExpireAfterTheirLifetime() {
        Simulation simulation = new Simulation(TestArenas.parse(
            "#######",
            "#..P..#",
            "#######"), SEED);

        // One cast to the left, toward the wall.
        simulation.setInput(PlayerInput.castAt(0, (float) simulation.getPlayer().y()));
        simulation.tick();
        simulation.setInput(PlayerInput.NONE);

        int burst = 0;
        for (int i = 0; i < 60 && burst == 0; i++) {
            simulation.tick();
            burst = countParticles(simulation);
        }
        assertTrue(burst > 0, "the spell never burst into particles");

        tick(simulation, MIN_LIFE_TICKS);
        assertEquals(burst, countParticles(simulation), "a particle expired before its shortest lifetime");

        tick(simulation, MAX_LIFE_TICKS + 1 - MIN_LIFE_TICKS);
        assertEquals(0, countParticles(simulation), "a particle outlived its longest lifetime");
    }

    @Test
    void particlesStayInsideTheArena() {
        Simulation simulation = new Simulation(TestArenas.parse(
            "#######",
            "#..P..#",
            "#######"), SEED);
        simulation.setInput(PlayerInput.castAt(0, (float) simulation.getPlayer().y()));
        simulation.tick();
        simulation.setInput(PlayerInput.NONE);

        for (int i = 0; i < 60; i++) {
            simulation.tick();
            for (EntityState state : simulation.getEntities()) {
                if (state.kind() != EntityKind.PARTICLE) continue;
                // Particles bounce off the walls, so their ground position stays between them.
                assertTrue(state.x() >= 16 && state.x() < 6 * 16, "particle left the arena at x = " + state.x());
            }
        }
    }

    private static int countParticles(Simulation simulation) {
        int count = 0;
        for (EntityState state : simulation.getEntities()) {
            if (state.kind() == EntityKind.PARTICLE) count++;
        }
        return count;
    }

    private static void tick(Simulation simulation, int ticks) {
        for (int i = 0; i < ticks; i++) simulation.tick();
    }
}
