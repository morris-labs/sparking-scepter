package com.southcentralpositronics.reign_gdx.game.wave;

import com.southcentralpositronics.reign_gdx.game.sim.EntityKind;

import java.util.ArrayList;
import java.util.List;

/**
 * The wave plan: which enemies each wave sends. Tune the game's difficulty
 * here; no system needs to change.
 *
 * <p>{@link #ROWS} lists the enemies of the first waves, one row per wave.
 * After the last row, each wave adds {@link #GROWTH} to the wave before it, so
 * every wave is bigger than the last and A* Chasers and Shooters make up a
 * growing share of it.
 */
public final class WavePlan {
    /** The seconds of the break before the first wave. */
    public static final float FIRST_BREAK_SECONDS = 3f;
    /** The seconds of the break after the player clears a wave. */
    public static final float BREAK_SECONDS       = 5f;
    /** The closest that a spawn point can be to the player for an enemy to enter there, in pixels: 6 tiles. */
    public static final double MIN_SPAWN_DISTANCE = 6 * 16;

    /** The order of the columns in {@link #ROWS} and {@link #GROWTH}. */
    private static final EntityKind[] KINDS = {
        EntityKind.DUMMY, EntityKind.CHASER, EntityKind.ASTAR_CHASER, EntityKind.SHOOTER};

    /** The enemies of each of the first waves, by kind, in the order of {@link #KINDS}. */
    private static final int[][] ROWS = {
        //  Dummy, Chaser, A* Chaser, Shooter
        {3, 1, 0, 0},   // Wave 1: 4 enemies
        {3, 3, 0, 0},   // Wave 2: 6
        {2, 4, 1, 1},   // Wave 3: 8
        {1, 5, 2, 2},   // Wave 4: 10
        {0, 5, 3, 4},   // Wave 5: 12
        {0, 5, 4, 5},   // Wave 6: 14
    };

    /** The enemies that each wave after the last row adds to the wave before it. */
    private static final int[] GROWTH = {0, 0, 1, 1};

    private WavePlan() {
    }

    /**
     * Lists the enemies of a wave, grouped by kind: Dummies, Chasers, A*
     * Chasers, then Shooters.
     *
     * @param wave The wave number, from 1. Numbers below 1 give wave 1.
     * @return The kind of each enemy in the wave.
     */
    public static List<EntityKind> enemies(int wave) {
        int    index  = Math.max(1, wave) - 1;
        int[]  counts = ROWS[Math.min(index, ROWS.length - 1)].clone();
        int    extra  = Math.max(0, index - (ROWS.length - 1));
        for (int column = 0; column < counts.length; column++) counts[column] += GROWTH[column] * extra;

        List<EntityKind> enemies = new ArrayList<>();
        for (int column = 0; column < KINDS.length; column++) {
            for (int i = 0; i < counts[column]; i++) enemies.add(KINDS[column]);
        }
        return enemies;
    }
}
