package com.southcentralpositronics.reign_gdx.game.sim;

import com.southcentralpositronics.reign_gdx.engine.input.PlayerInput;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Scenarios for the player's spells and each enemy type's behavior. */
class MobTest {
    private static final long SEED = 42L;

    @Test
    void oneSpellTakesATenthOfAnEnemysHealth() {
        Simulation simulation = new Simulation(TestArenas.parse(
            "########",
            "#P.....#",
            "########"), SEED);
        simulation.spawnEnemy(EntityKind.DUMMY, 5, 1);
        simulation.drainEvents();

        // One cast along the corridor. The Dummy can only wander along it, so the spell can't miss.
        simulation.setInput(PlayerInput.castAt(1000, (float) simulation.getPlayer().y() + 8));
        simulation.tick();
        simulation.setInput(PlayerInput.NONE);
        List<SimulationEvent> events = new ArrayList<>(simulation.drainEvents());
        tick(simulation, 40, events);

        assertEquals(1, count(events, SimulationEvent.Type.HIT));
        assertEquals(0.9, enemy(simulation, EntityKind.DUMMY).health(), 1e-9);
    }

    @Test
    void chaserRunsStraightAtThePlayerAtSixtyPixelsPerSecond() {
        Simulation simulation = new Simulation(TestArenas.parse(
            "########",
            "#P.....#",
            "########"), SEED);
        simulation.spawnEnemy(EntityKind.CHASER, 6, 1);
        assertEquals(80, distanceToPlayer(simulation, EntityKind.CHASER), 1e-9);

        tick(simulation, 60, new ArrayList<>());

        assertEquals(20, distanceToPlayer(simulation, EntityKind.CHASER), 1e-9);
    }

    @Test
    void aStarChaserReachesThePlayerAroundAHedge() {
        Simulation simulation = new Simulation(TestArenas.parse(
            "###########",
            "#....P....#",
            "#.HHHHHHH.#",
            "#.........#",
            "###########"), SEED);
        simulation.spawnEnemy(EntityKind.ASTAR_CHASER, 5, 1);

        int ticks = 0;
        while (distanceToPlayer(simulation, EntityKind.ASTAR_CHASER) > Tile.SIZE && ticks < 600) {
            simulation.tick();
            ticks++;
        }

        assertTrue(ticks < 600, "the A* Chaser never reached the player");
    }

    @Test
    void shooterCastsAtAPlayerInRange() {
        Simulation simulation = new Simulation(TestArenas.parse(
            "########",
            "#P.....#",
            "########"), SEED);
        simulation.spawnEnemy(EntityKind.SHOOTER, 4, 1);
        simulation.drainEvents();

        List<SimulationEvent> events = new ArrayList<>();
        tick(simulation, 30, events);

        assertTrue(events.stream().anyMatch(event -> event.type() == SimulationEvent.Type.CAST
            && event.kind() == EntityKind.SHOOTER), "the Shooter never cast");
        assertTrue(count(events, SimulationEvent.Type.PLAYER_DAMAGE) > 0, "no Shooter spell hit the player");
        assertTrue(simulation.getPlayer().health() < 1.0);
    }

    private static double distanceToPlayer(Simulation simulation, EntityKind kind) {
        EntityState enemy  = enemy(simulation, kind);
        EntityState player = simulation.getPlayer();
        return Math.hypot(enemy.x() - player.x(), enemy.y() - player.y());
    }

    private static EntityState enemy(Simulation simulation, EntityKind kind) {
        return simulation.getEntities().stream().filter(entity -> entity.kind() == kind).findFirst().orElseThrow();
    }

    private static long count(List<SimulationEvent> events, SimulationEvent.Type type) {
        return events.stream().filter(event -> event.type() == type).count();
    }

    private static void tick(Simulation simulation, int ticks, List<SimulationEvent> events) {
        for (int i = 0; i < ticks; i++) {
            simulation.tick();
            events.addAll(simulation.drainEvents());
        }
    }
}
