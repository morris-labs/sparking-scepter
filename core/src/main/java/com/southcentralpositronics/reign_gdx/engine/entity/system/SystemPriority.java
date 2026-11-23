package com.southcentralpositronics.reign_gdx.engine.entity.system;

/**
 * The order in which the simulation's Ashley systems run within one tick.
 * Ashley runs lower priorities first, and runs systems with equal priorities
 * in the order they were added.
 *
 * <p>The range below {@link #PARTICLES} is for the rules of play, in this
 * order: input, AI, movement, mob separation, casting, spells, damage and
 * death, waves, and score. Effects such as animation and particles run after
 * the rules, and {@link #LIFETIME} runs last, so every system sees an expiring
 * entity on its final tick.
 *
 * <p>An entity that a system adds or removes during a tick joins or leaves
 * the families as soon as that system finishes, so later systems in the same
 * tick already see the change.
 */
public final class SystemPriority {
    /** Player input becomes velocity and casting intent. */
    public static final int INPUT      = 100;
    /** Enemy AI sets velocity and casting intent. */
    public static final int AI         = 200;
    /** Movement with tile collision. */
    public static final int MOVEMENT   = 300;
    /** Pushing overlapping mobs apart. */
    public static final int SEPARATION = 400;
    /** Casting spells. */
    public static final int CASTING    = 500;
    /** Spell flight, and the hits and damage that spells deal. */
    public static final int SPELLS     = 600;
    /** Death of mobs with no health left. */
    public static final int DEATH      = 650;
    /** Waves of enemies. */
    public static final int WAVES      = 700;
    /** Facing and walking animation, from the velocity that a mob asked for. */
    public static final int ANIMATION  = 750;
    /** Particle physics. */
    public static final int PARTICLES  = 800;
    /** Removal of entities whose lifetime has run out. */
    public static final int LIFETIME   = 900;
    /** Particle bursts asked for during the tick join the engine, so they first move on the next tick. */
    public static final int EMIT       = 1000;

    private SystemPriority() {
    }
}
