package com.southcentralpositronics.reign_gdx.game.sim;

import com.southcentralpositronics.reign_gdx.engine.input.PlayerInput;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Scenarios for the events that audio plays sounds for. Each test checks that
 * an event comes out of the same tick as the change it reports, and only then,
 * so a sound never plays early, late, or twice.
 */
class AudioEventTest {
    private static final long SEED = 42L;
    /** Enough ticks for anything in these small arenas to happen. */
    private static final int  LIMIT = 3600;

    @Test
    void castFiresOnTheTickTheSpellAppears() {
        Simulation simulation = new Simulation(TestArenas.open(9, 5, 2, 2), SEED);
        simulation.drainEvents();
        simulation.tick();
        assertTrue(simulation.drainEvents().isEmpty(), "an idle player raised events");

        simulation.setInput(PlayerInput.castAt(1000, 40));
        simulation.tick();

        List<SimulationEvent> events = simulation.drainEvents();
        assertEquals(List.of(SimulationEvent.Type.CAST), types(events));
        assertEquals(EntityKind.PLAYER, events.getFirst().kind());
        assertEquals(1, count(simulation, EntityKind.SPELL));
        assertEquals(simulation.getPlayer().x(), events.getFirst().x(), 1e-9);
    }

    @Test
    void hitFiresOnTheTickTheEnemyLosesHealth() {
        Simulation simulation = new Simulation(TestArenas.open(9, 5, 2, 2), SEED);
        simulation.spawnEnemy(EntityKind.DUMMY, 5, 2);
        simulation.drainEvents();
        aimAtFirstEnemy(simulation);

        double health = enemy(simulation).health();
        for (int tick = 0; tick < LIMIT; tick++) {
            simulation.tick();
            List<SimulationEvent> events = simulation.drainEvents();
            boolean hurt = enemy(simulation).health() < health;
            assertEquals(hurt, types(events).contains(SimulationEvent.Type.HIT),
                         "HIT and the health loss came apart on tick " + tick);
            if (hurt) {
                assertEquals(EntityKind.DUMMY, find(events, SimulationEvent.Type.HIT).kind());
                return;
            }
        }
        fail("no spell hit the enemy");
    }

    @Test
    void enemyDeathFiresOnceOnTheTickTheEnemyLeaves() {
        Simulation simulation = new Simulation(TestArenas.open(9, 5, 2, 2), SEED);
        simulation.spawnEnemy(EntityKind.CHASER, 6, 2);
        simulation.drainEvents();

        for (int tick = 0; tick < LIMIT; tick++) {
            aimAtFirstEnemy(simulation);
            simulation.tick();
            List<SimulationEvent> events = simulation.drainEvents();
            boolean gone = simulation.getEnemyCount() == 0;
            assertEquals(gone ? 1 : 0, count(events, SimulationEvent.Type.ENEMY_DEATH),
                         "ENEMY_DEATH and the enemy leaving came apart on tick " + tick);
            if (gone) {
                assertEquals(EntityKind.CHASER, find(events, SimulationEvent.Type.ENEMY_DEATH).kind());
                simulation.tick();
                assertEquals(0, count(simulation.drainEvents(), SimulationEvent.Type.ENEMY_DEATH));
                return;
            }
        }
        fail("the enemy never died");
    }

    @Test
    void playerDamageFiresOnEveryTickThePlayerLosesHealth() {
        Simulation simulation = new Simulation(TestArenas.parse(
            "#######",
            "#P....#",
            "#######"), SEED);
        simulation.spawnEnemy(EntityKind.CHASER, 4, 1);
        simulation.drainEvents();

        int    hurtTicks = 0;
        double health    = simulation.getPlayer().health();
        for (int tick = 0; tick < 600; tick++) {
            simulation.tick();
            List<SimulationEvent> events = simulation.drainEvents();
            boolean hurt = simulation.getPlayer().health() < health;
            health = simulation.getPlayer().health();
            assertEquals(hurt ? 1 : 0, count(events, SimulationEvent.Type.PLAYER_DAMAGE),
                         "PLAYER_DAMAGE and the health loss came apart on tick " + tick);
            if (hurt) hurtTicks++;
        }
        assertTrue(hurtTicks >= 2, "the Chaser hurt the player " + hurtTicks + " times");
    }

    @Test
    void playerDeathFiresOnceOnTheTickTheRunEnds() {
        Simulation simulation = new Simulation(TestArenas.parse(
            "#######",
            "#P....#",
            "#######"), SEED);
        simulation.spawnEnemy(EntityKind.CHASER, 4, 1);
        simulation.drainEvents();

        for (int tick = 0; tick < LIMIT; tick++) {
            simulation.tick();
            List<SimulationEvent> events = simulation.drainEvents();
            boolean over = simulation.isRunOver();
            assertEquals(over ? 1 : 0, count(events, SimulationEvent.Type.PLAYER_DEATH),
                         "PLAYER_DEATH and the end of the run came apart on tick " + tick);
            if (over) {
                assertEquals(EntityKind.PLAYER, find(events, SimulationEvent.Type.PLAYER_DEATH).kind());
                for (int after = 0; after < 120; after++) simulation.tick();
                assertEquals(0, count(simulation.drainEvents(), SimulationEvent.Type.PLAYER_DEATH));
                return;
            }
        }
        fail("the player never died");
    }

    @Test
    void waveStartFiresOnTheTickTheWavesEnemiesAppear() {
        Simulation simulation = new Simulation(TestArenas.parse(
            "#############",
            "#P.........S#",
            "#############"), SEED);
        simulation.drainEvents();

        for (int tick = 0; tick < LIMIT; tick++) {
            int before = simulation.getEnemyCount();
            simulation.tick();
            List<SimulationEvent> events = simulation.drainEvents();
            boolean arrived = before == 0 && simulation.getEnemyCount() > 0;
            assertEquals(arrived ? 1 : 0, count(events, SimulationEvent.Type.WAVE_START),
                         "WAVE_START and the wave's enemies came apart on tick " + tick);
            if (arrived) {
                assertEquals(1, simulation.getWave());
                assertFalse(simulation.isBreak());
                return;
            }
        }
        fail("no wave started");
    }

    /** Points the player's casting at the middle of the first enemy's body. */
    private static void aimAtFirstEnemy(Simulation simulation) {
        EntityState target = enemy(simulation);
        simulation.setInput(PlayerInput.castAt((float) target.x(), (float) target.y() + 8));
    }

    private static EntityState enemy(Simulation simulation) {
        return simulation.getEntities().stream().filter(entity -> entity.kind().isEnemy()).findFirst().orElseThrow();
    }

    private static long count(Simulation simulation, EntityKind kind) {
        return simulation.getEntities().stream().filter(entity -> entity.kind() == kind).count();
    }

    private static long count(List<SimulationEvent> events, SimulationEvent.Type type) {
        return events.stream().filter(event -> event.type() == type).count();
    }

    private static SimulationEvent find(List<SimulationEvent> events, SimulationEvent.Type type) {
        return events.stream().filter(event -> event.type() == type).findFirst().orElseThrow();
    }

    private static List<SimulationEvent.Type> types(List<SimulationEvent> events) {
        return events.stream().map(SimulationEvent::type).toList();
    }
}
