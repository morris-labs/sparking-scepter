package com.southcentralpositronics.reign_gdx.game.sim;

import com.southcentralpositronics.reign_gdx.engine.input.PlayerInput;
import com.southcentralpositronics.reign_gdx.engine.level.Arena;
import com.southcentralpositronics.reign_gdx.game.upgrade.Upgrade;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Scenarios for the upgrades offered in the breaks between waves. */
class UpgradeTest {
    private static final long SEED = 42L;

    @Test
    void theBreakBeforeTheFirstWaveOffersNoUpgrades() {
        Simulation simulation = new Simulation(corridor(), SEED);

        assertTrue(simulation.isBreak());
        assertFalse(simulation.isUpgradePending());
        assertTrue(simulation.getUpgradeOffers().isEmpty());
    }

    @Test
    void aClearedWaveOffersThreeDifferentUpgradesAndTheBreakWaitsForAChoice() {
        Simulation simulation = clearWaveOne(SEED);

        assertTrue(simulation.isUpgradePending());
        List<Upgrade> offers = simulation.getUpgradeOffers();
        assertEquals(3, offers.size());
        assertEquals(3, new HashSet<>(offers).size(), "an upgrade was offered twice");

        WaveTest.tick(simulation, 1200, new ArrayList<>());
        assertTrue(simulation.isBreak());
        assertEquals(5f, simulation.getBreakSecondsLeft(), 1e-6);
        assertEquals(1, simulation.getWave());

        simulation.chooseUpgrade(offers.get(1));
        assertFalse(simulation.isUpgradePending());
        assertTrue(simulation.getUpgradeOffers().isEmpty());
        assertEquals(List.of(offers.get(1)), simulation.getUpgradesTaken());

        WaveTest.tick(simulation, 299, new ArrayList<>());
        assertTrue(simulation.isBreak());
        WaveTest.tick(simulation, 1, new ArrayList<>());
        assertEquals(2, simulation.getWave());
    }

    /** Starts a run in the corridor and kills wave 1, which leaves the run at the start of the next break. */
    static Simulation clearWaveOne(long seed) {
        Simulation simulation = new Simulation(corridor(), seed);
        WaveTest.tick(simulation, 180, new ArrayList<>());
        assertEquals(1, simulation.getWave());

        int ticks = 0;
        while (simulation.getEnemyCount() > 0 && ticks < 3600) {
            EntityState target = nearestEnemy(simulation);
            simulation.setInput(PlayerInput.castAt((float) target.x(), (float) target.y() + 8));
            simulation.tick();
            ticks++;
        }
        simulation.setInput(PlayerInput.NONE);
        simulation.drainEvents();
        assertEquals(0, simulation.getEnemyCount(), "the player never cleared wave 1");
        assertTrue(simulation.isBreak());
        return simulation;
    }

    private static EntityState nearestEnemy(Simulation simulation) {
        EntityState player = simulation.getPlayer();
        return simulation.getEntities().stream().filter(entity -> entity.kind().isEnemy())
            .min(java.util.Comparator.comparingDouble(enemy -> Math.hypot(enemy.x() - player.x(), enemy.y() - player.y())))
            .orElseThrow();
    }

    /** A corridor with the player at the left end and one spawn point at the right, seven tiles away. */
    private static Arena corridor() {
        return TestArenas.parse(
            "##########",
            "#P......S#",
            "##########");
    }
}
