package com.southcentralpositronics.reign_gdx.game.sim;

import com.southcentralpositronics.reign_gdx.engine.input.PlayerInput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Scenarios for the score: each enemy type is worth its own points. */
class ScoreTest {
    private static final long SEED = 42L;

    @Test
    void runStartsWithNoScore() {
        assertEquals(0, new Simulation(TestArenas.open(9, 5, 2, 2), SEED).getScore());
    }

    @Test
    void eachEnemyTypeAddsItsOwnScoreValue() {
        assertEquals(50, scoreForKilling(EntityKind.DUMMY));
        assertEquals(100, scoreForKilling(EntityKind.CHASER));
        assertEquals(150, scoreForKilling(EntityKind.ASTAR_CHASER));
        assertEquals(200, scoreForKilling(EntityKind.SHOOTER));
    }

    @Test
    void scoresAddUpAcrossEnemyTypes() {
        Simulation simulation = new Simulation(TestArenas.open(12, 5, 2, 2), SEED);

        kill(simulation, EntityKind.DUMMY);
        kill(simulation, EntityKind.SHOOTER);
        kill(simulation, EntityKind.DUMMY);

        assertEquals(50 + 200 + 50, simulation.getScore());
    }

    private static long scoreForKilling(EntityKind kind) {
        Simulation simulation = new Simulation(TestArenas.open(12, 5, 2, 2), SEED);
        kill(simulation, kind);
        return simulation.getScore();
    }

    /** Spawns one enemy a few tiles right of the player and casts at it until it dies. */
    private static void kill(Simulation simulation, EntityKind kind) {
        simulation.spawnEnemy(kind, 6, 2);
        for (int i = 0; i < 1200 && simulation.getEnemyCount() > 0; i++) {
            EntityState enemy = simulation.getEntities().stream()
                .filter(entity -> entity.kind() == kind).findFirst().orElseThrow();
            // Aim at the middle of the body, above the collision box at the feet.
            simulation.setInput(PlayerInput.castAt((float) enemy.x(), (float) enemy.y() + 8));
            simulation.tick();
        }
        simulation.setInput(PlayerInput.NONE);
        assertEquals(0, simulation.getEnemyCount(), kind + " didn't die");
    }
}
