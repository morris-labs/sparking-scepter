package com.southcentralpositronics.reign_gdx.game.sim;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

/** Scenarios for mob separation: mobs push apart instead of stacking, and never into a solid tile. */
class SeparationTest {
    private static final long SEED = 42L;

    /** The size of a mob's collision box at its feet, in pixels. */
    private static final double BOX_WIDTH  = 16;
    private static final double BOX_HEIGHT = 8;

    @Test
    void twoStackedChasersSeparate() {
        Simulation simulation = new Simulation(TestArenas.open(12, 12, 2, 2), SEED);
        simulation.clearEnemies();
        simulation.spawnEnemy(EntityKind.CHASER, 8, 8);
        simulation.spawnEnemy(EntityKind.CHASER, 8, 8);

        tick(simulation, 5);

        List<EntityState> chasers = mobs(simulation).stream()
            .filter(mob -> mob.kind() == EntityKind.CHASER).toList();
        assertFalse(overlap(chasers.get(0), chasers.get(1)), "the stacked Chasers still overlap");
    }

    @Test
    void chasersPressingThePlayerIntoACornerNeverPushAnyMobIntoAWall() {
        Simulation simulation = new Simulation(TestArenas.parse(
            "#####",
            "#P..#",
            "#####"), SEED);
        simulation.spawnEnemy(EntityKind.CHASER, 1, 1);
        simulation.spawnEnemy(EntityKind.CHASER, 3, 1);

        for (int tick = 0; tick < 120; tick++) {
            simulation.tick();
            for (EntityState mob : mobs(simulation)) {
                assertFalse(insideSolidTile(simulation, mob), mob.kind() + " was pushed into a wall on tick " + tick);
            }
        }
    }

    private static boolean insideSolidTile(Simulation simulation, EntityState mob) {
        double left   = mob.x() - BOX_WIDTH / 2;
        double bottom = mob.y() - BOX_HEIGHT / 2;
        double[][] corners = {
            {left, bottom}, {left + BOX_WIDTH - 1e-6, bottom},
            {left, bottom + BOX_HEIGHT - 1e-6}, {left + BOX_WIDTH - 1e-6, bottom + BOX_HEIGHT - 1e-6}};
        for (double[] corner : corners) {
            int tx = (int) Math.floor(corner[0] / 16);
            int ty = (int) Math.floor(corner[1] / 16);
            if (simulation.getTileAt(tx, ty).blocksMovement()) return true;
        }
        return false;
    }

    private static List<EntityState> mobs(Simulation simulation) {
        return simulation.getEntities().stream().filter(entity -> entity.kind().isMob()).toList();
    }

    private static boolean overlap(EntityState a, EntityState b) {
        return Math.abs(a.x() - b.x()) < BOX_WIDTH - 1e-9 && Math.abs(a.y() - b.y()) < BOX_HEIGHT - 1e-9;
    }

    private static void tick(Simulation simulation, int ticks) {
        for (int i = 0; i < ticks; i++) simulation.tick();
    }
}
