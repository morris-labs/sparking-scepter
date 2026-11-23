package com.southcentralpositronics.reign_gdx.game.sim;

/**
 * Where a run stands: in a break, in a wave, or over. The simulation's queries
 * read it, and the systems that apply the rules change it.
 *
 * <p>A run starts in a break. A break counts down in ticks and then a wave
 * starts; clearing the wave starts the next break. Ending the run is final:
 * nothing starts a break or a wave after it.
 */
public final class RunState {
    /** The three states of a run. */
    public enum Phase {
        /** The pause between waves, which counts down to the next wave. */
        BREAK,
        /** A wave of enemies is in the arena. */
        WAVE,
        /** The run has ended. */
        OVER
    }

    private Phase phase          = Phase.BREAK;
    private int   wave           = 0;
    private int   breakTicksLeft = 0;

    /** Gets the run's phase. */
    public Phase getPhase() {
        return phase;
    }

    /** Gets the number of waves that have started. */
    public int getWave() {
        return wave;
    }

    /** Checks whether a break is running. */
    public boolean isBreak() {
        return phase == Phase.BREAK;
    }

    /** Checks whether a wave is running. */
    public boolean isWave() {
        return phase == Phase.WAVE;
    }

    /** Checks whether the run is over. */
    public boolean isOver() {
        return phase == Phase.OVER;
    }

    /** Gets the ticks left in the break, or 0 when no break is running. */
    public int getBreakTicksLeft() {
        return isBreak() ? breakTicksLeft : 0;
    }

    /**
     * Starts a break, unless the run is over.
     *
     * @param ticks The length of the break, in ticks.
     */
    public void startBreak(int ticks) {
        if (isOver()) return;
        phase          = Phase.BREAK;
        breakTicksLeft = ticks;
    }

    /**
     * Counts one tick off the break.
     *
     * @return True when the break has no ticks left.
     */
    public boolean countDownBreak() {
        if (!isBreak()) return false;
        if (breakTicksLeft > 0) breakTicksLeft--;
        return breakTicksLeft == 0;
    }

    /** Starts the next wave, unless the run is over, and counts it. */
    public void startWave() {
        if (isOver()) return;
        phase          = Phase.WAVE;
        breakTicksLeft = 0;
        wave++;
    }

    /** Ends the run. It stays over. */
    public void end() {
        phase          = Phase.OVER;
        breakTicksLeft = 0;
    }
}
