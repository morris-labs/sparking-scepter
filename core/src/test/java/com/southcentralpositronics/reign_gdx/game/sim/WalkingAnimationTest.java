package com.southcentralpositronics.reign_gdx.game.sim;

import com.southcentralpositronics.reign_gdx.engine.input.PlayerInput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Scenarios for the walking animation's facing: walking up faces up and walking down faces down. */
class WalkingAnimationTest {
    private static final long SEED = 42L;

    @Test
    void playerWalkingUpFacesUp() {
        Simulation simulation = openArena();

        simulation.setInput(PlayerInput.move(0, 1));
        tick(simulation, 10);

        assertTrue(simulation.getPlayer().walking());
        assertEquals(Facing.UP, simulation.getPlayer().facing());
    }

    @Test
    void playerWalkingDownFacesDown() {
        Simulation simulation = openArena();

        simulation.setInput(PlayerInput.move(0, -1));
        tick(simulation, 10);

        assertTrue(simulation.getPlayer().walking());
        assertEquals(Facing.DOWN, simulation.getPlayer().facing());
    }

    @Test
    void chaserWalkingUpToThePlayerFacesUp() {
        Simulation simulation = openArena();
        simulation.spawnEnemy(EntityKind.CHASER, 5, 1);

        tick(simulation, 10);

        assertEquals(Facing.UP, chaser(simulation).facing());
    }

    @Test
    void chaserWalkingDownToThePlayerFacesDown() {
        Simulation simulation = openArena();
        simulation.spawnEnemy(EntityKind.CHASER, 5, 9);

        tick(simulation, 10);

        assertEquals(Facing.DOWN, chaser(simulation).facing());
    }

    /** An 11x11 walled grass box with the player in the middle and no wave enemies. */
    private static Simulation openArena() {
        Simulation simulation = new Simulation(TestArenas.open(11, 11, 5, 5), SEED);
        simulation.clearEnemies();
        return simulation;
    }

    private static EntityState chaser(Simulation simulation) {
        return simulation.getEntities().stream().filter(entity -> entity.kind() == EntityKind.CHASER)
            .findFirst().orElseThrow();
    }

    private static void tick(Simulation simulation, int ticks) {
        for (int i = 0; i < ticks; i++) simulation.tick();
    }
}
