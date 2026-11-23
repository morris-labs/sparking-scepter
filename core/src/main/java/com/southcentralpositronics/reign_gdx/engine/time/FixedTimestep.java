package com.southcentralpositronics.reign_gdx.engine.time;

/**
 * Turns real frame times into a whole number of fixed simulation ticks.
 *
 * <p>Each frame, call {@link #advance(float)} with the frame time and run the
 * simulation that many ticks. Time left over carries into the next frame, so
 * the simulation runs {@link #TICKS_PER_SECOND} ticks per second at any frame
 * rate.
 */
public final class FixedTimestep {
    /** The number of ticks in one second of play. */
    public static final int    TICKS_PER_SECOND  = 60;
    /** The length of one tick, in seconds. */
    public static final float  TICK_SECONDS      = 1f / TICKS_PER_SECOND;
    /**
     * The longest frame time that counts, in seconds. A longer frame, such as a
     * stall while the window moves, runs only this many seconds of ticks, so the
     * simulation doesn't fall further behind trying to catch up.
     */
    public static final float  MAX_FRAME_SECONDS = 0.25f;
    /** Absorbs rounding error, so frames that add up to a whole tick run it. */
    private static final double EPSILON_TICKS    = 1e-6;

    private double accumulatedTicks;

    /**
     * Converts a rate per second to the amount per tick.
     *
     * @param perSecond The amount per second, such as a speed in pixels per second.
     * @return The amount per tick.
     */
    public static double perTick(double perSecond) {
        return perSecond / TICKS_PER_SECOND;
    }

    /**
     * Adds a frame's time and returns the number of ticks to run for it.
     *
     * @param frameSeconds The time since the last frame, in seconds.
     * @return The number of whole ticks that are due.
     */
    public int advance(float frameSeconds) {
        float clamped = Math.max(0f, Math.min(frameSeconds, MAX_FRAME_SECONDS));
        accumulatedTicks += clamped * (double) TICKS_PER_SECOND;
        int ticks = (int) Math.floor(accumulatedTicks + EPSILON_TICKS);
        accumulatedTicks = Math.max(0, accumulatedTicks - ticks);
        return ticks;
    }
}
