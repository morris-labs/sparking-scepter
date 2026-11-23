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

    @Test
    void fasterMovementWalksTwentyPercentFaster() {
        Simulation simulation = choose(Upgrade.FASTER_MOVEMENT);
        double     startX     = simulation.getPlayer().x();

        simulation.setInput(PlayerInput.move(1, 0));
        WaveTest.tick(simulation, 30, new ArrayList<>());

        // 144 px/s instead of 120 for half a second.
        assertEquals(72, simulation.getPlayer().x() - startX, 1e-6);
    }

    @Test
    void fasterCastingCastsTenTimesASecondInsteadOfEight() {
        Simulation simulation = choose(Upgrade.FASTER_CASTING);
        EntityState player    = simulation.getPlayer();

        List<SimulationEvent> events = new ArrayList<>();
        simulation.setInput(PlayerInput.castAt((float) player.x() + 100, (float) player.y() + 8));
        WaveTest.tick(simulation, 60, events);

        assertEquals(10, WaveTest.count(events, SimulationEvent.Type.CAST));
    }

    @Test
    void strongerSpellsTakeAQuarterMoreHealthPerHit() {
        Simulation simulation = choose(Upgrade.STRONGER_SPELLS);
        // Let the spells cast at the last enemy of wave 1 fly out, so only new ones hit.
        WaveTest.tick(simulation, 60, new ArrayList<>());
        simulation.spawnEnemy(EntityKind.DUMMY, 5, 1);

        List<SimulationEvent> events = new ArrayList<>();
        int ticks = 0;
        while (WaveTest.count(events, SimulationEvent.Type.HIT) == 0 && ticks < 600) {
            EntityState target = nearestEnemy(simulation);
            simulation.setInput(PlayerInput.castAt((float) target.x(), (float) target.y() + 8));
            simulation.tick();
            events.addAll(simulation.drainEvents());
            ticks++;
        }

        assertEquals(1, WaveTest.count(events, SimulationEvent.Type.HIT));
        assertEquals(1 - 0.125, nearestEnemy(simulation).health(), 1e-9);
    }

    @Test
    void moreHealthRaisesTheMaximumAndHealsByAQuarter() {
        Simulation simulation = clearWaveOneOffering(Upgrade.MORE_HEALTH);
        assertEquals(1.0, simulation.getPlayerMaxHealth(), 1e-9);
        double health = simulation.getPlayer().health();

        simulation.chooseUpgrade(Upgrade.MORE_HEALTH);

        assertEquals(1.25, simulation.getPlayerMaxHealth(), 1e-9);
        assertEquals(health + 0.25, simulation.getPlayer().health(), 1e-9);
    }

    @Test
    void upgradesStackWhenTakenAgain() {
        // Water keeps the later waves' Chasers off the player, and spells fly over it.
        Simulation simulation = clearWaveOne(moatedCorridor(), SEED);
        simulation.chooseUpgrade(simulation.getUpgradeOffers().contains(Upgrade.MORE_HEALTH)
                                     ? Upgrade.MORE_HEALTH : simulation.getUpgradeOffers().getFirst());
        clearNextWave(simulation);
        while (!simulation.getUpgradeOffers().contains(Upgrade.MORE_HEALTH)) {
            simulation.chooseUpgrade(simulation.getUpgradeOffers().getFirst());
            clearNextWave(simulation);
        }

        simulation.chooseUpgrade(Upgrade.MORE_HEALTH);

        long healthUpgrades = simulation.getUpgradesTaken().stream().filter(Upgrade.MORE_HEALTH::equals).count();
        assertTrue(healthUpgrades >= 2);
        assertEquals(1 + 0.25 * healthUpgrades, simulation.getPlayerMaxHealth(), 1e-9);
    }

    @Test
    void aNewRunStartsWithNoUpgrades() {
        Simulation first = choose(Upgrade.MORE_HEALTH);
        assertEquals(List.of(Upgrade.MORE_HEALTH), first.getUpgradesTaken());

        Simulation next = new Simulation(corridor(), SEED);

        assertTrue(next.getUpgradesTaken().isEmpty());
        assertEquals(1.0, next.getPlayerMaxHealth(), 1e-9);
        assertEquals(1.0, next.getPlayer().health(), 1e-9);
    }

    @Test
    void theSameSeedOffersTheSameUpgradesAndOtherSeedsVary() {
        assertEquals(clearWaveOne(SEED).getUpgradeOffers(), clearWaveOne(SEED).getUpgradeOffers());

        var seen = new HashSet<List<Upgrade>>();
        for (long seed = 1; seed <= 6; seed++) seen.add(clearWaveOne(seed).getUpgradeOffers());
        assertTrue(seen.size() > 1, "every seed offered the same upgrades");
    }

    @Test
    void choosingAnUpgradeThatIsNotOnOfferFails() {
        Simulation simulation = new Simulation(corridor(), SEED);
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
            () -> simulation.chooseUpgrade(Upgrade.MORE_HEALTH));
    }

    /** Clears wave 1 with a seed that offers the upgrade, and takes it. */
    private static Simulation choose(Upgrade upgrade) {
        Simulation simulation = clearWaveOneOffering(upgrade);
        simulation.chooseUpgrade(upgrade);
        return simulation;
    }

    /** Clears wave 1 with the first seed from 1 up whose offers include the upgrade. */
    private static Simulation clearWaveOneOffering(Upgrade upgrade) {
        for (long seed = 1; seed <= 20; seed++) {
            Simulation simulation = clearWaveOne(seed);
            if (simulation.getUpgradeOffers().contains(upgrade)) return simulation;
        }
        throw new AssertionError("no seed offered " + upgrade);
    }

    /** Waits out the break after a choice, then kills the next wave. */
    private static void clearNextWave(Simulation simulation) {
        int wave = simulation.getWave();
        WaveTest.tick(simulation, 300, new ArrayList<>());
        assertEquals(wave + 1, simulation.getWave());
        killWave(simulation);
    }

    /** Starts a run in the corridor and kills wave 1, which leaves the run at the start of the next break. */
    static Simulation clearWaveOne(long seed) {
        return clearWaveOne(corridor(), seed);
    }

    /** Starts a run in an arena and kills wave 1, which leaves the run at the start of the next break. */
    private static Simulation clearWaveOne(Arena arena, long seed) {
        Simulation simulation = new Simulation(arena, seed);
        WaveTest.tick(simulation, 180, new ArrayList<>());
        assertEquals(1, simulation.getWave());
        killWave(simulation);
        return simulation;
    }

    /** Casts at the nearest enemy until the wave is gone, then checks that its break has started. */
    private static void killWave(Simulation simulation) {
        int ticks = 0;
        while (simulation.getEnemyCount() > 0 && !simulation.isRunOver() && ticks < 7200) {
            EntityState target = nearestEnemy(simulation);
            simulation.setInput(PlayerInput.castAt((float) target.x(), (float) target.y() + 8));
            simulation.tick();
            ticks++;
        }
        simulation.setInput(PlayerInput.NONE);
        simulation.drainEvents();
        assertFalse(simulation.isRunOver(), "the player died");
        assertEquals(0, simulation.getEnemyCount(), "the player never cleared the wave");
        assertTrue(simulation.isBreak());
    }

    private static EntityState nearestEnemy(Simulation simulation) {
        EntityState player = simulation.getPlayer();
        return simulation.getEntities().stream().filter(entity -> entity.kind().isEnemy())
            .min(java.util.Comparator.comparingDouble(enemy -> Math.hypot(enemy.x() - player.x(), enemy.y() - player.y())))
            .orElseThrow();
    }

    /** The corridor with a water tile between the player and the rest of it. */
    private static Arena moatedCorridor() {
        return TestArenas.parse(
            "##########",
            "#P~.....S#",
            "##########");
    }

    /** A corridor with the player at the left end and one spawn point at the right, seven tiles away. */
    private static Arena corridor() {
        return TestArenas.parse(
            "##########",
            "#P......S#",
            "##########");
    }
}
