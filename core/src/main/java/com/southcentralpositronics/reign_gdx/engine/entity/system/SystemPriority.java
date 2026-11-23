package com.southcentralpositronics.reign_gdx.engine.entity.system;

/**
 * The order in which the simulation's Ashley systems run within one tick.
 * Ashley runs lower priorities first.
 *
 * <p>The range below {@link #PARTICLES} is for the rules of play, in this
 * order: input, AI, movement, mob separation, casting, spells, damage and
 * death, waves, and score. Effects such as particles run after the rules, and
 * {@link #LIFETIME} runs last, so every system sees an expiring entity on its
 * final tick.
 */
public final class SystemPriority {
    /** Particle physics. */
    public static final int PARTICLES = 800;
    /** Removal of entities whose lifetime has run out. */
    public static final int LIFETIME  = 900;

    private SystemPriority() {
    }
}
