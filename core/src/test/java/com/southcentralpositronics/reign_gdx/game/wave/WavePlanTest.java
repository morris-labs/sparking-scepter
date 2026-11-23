package com.southcentralpositronics.reign_gdx.game.wave;

import com.southcentralpositronics.reign_gdx.game.sim.EntityKind;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks the shape of the wave plan: waves grow, and grow tougher. */
class WavePlanTest {
    @Test
    void firstWaveIsThreeDummiesAndAChaser() {
        assertEquals(List.of(EntityKind.DUMMY, EntityKind.DUMMY, EntityKind.DUMMY, EntityKind.CHASER),
                     WavePlan.enemies(1));
    }

    @Test
    void everyWaveHasMoreEnemiesThanTheOneBefore() {
        for (int wave = 2; wave <= 30; wave++) {
            assertTrue(WavePlan.enemies(wave).size() > WavePlan.enemies(wave - 1).size(),
                       "wave " + wave + " isn't bigger than wave " + (wave - 1));
        }
    }

    @Test
    void theShareOfAStarChasersAndShootersNeverShrinksAndGrowsOverTime() {
        double previous = toughShare(1);
        for (int wave = 2; wave <= 30; wave++) {
            double share = toughShare(wave);
            assertTrue(share >= previous, "wave " + wave + " has a smaller share of tough enemies");
            previous = share;
        }
        assertEquals(0, toughShare(1));
        assertTrue(toughShare(5) > 0.5);
        assertTrue(toughShare(30) > toughShare(10));
    }

    @Test
    void laterWavesKeepGrowingPastTheEndOfTheTable() {
        assertEquals(WavePlan.enemies(20).size() + 2, WavePlan.enemies(21).size());
    }

    private static double toughShare(int wave) {
        List<EntityKind> enemies = WavePlan.enemies(wave);
        long tough = enemies.stream()
                            .filter(kind -> kind == EntityKind.ASTAR_CHASER || kind == EntityKind.SHOOTER)
                            .count();
        return (double) tough / enemies.size();
    }
}
