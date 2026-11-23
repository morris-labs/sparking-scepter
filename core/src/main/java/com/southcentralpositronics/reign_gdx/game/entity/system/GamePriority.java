package com.southcentralpositronics.reign_gdx.game.entity.system;

import com.southcentralpositronics.reign_gdx.engine.entity.system.SystemPriority;

/**
 * The order in which Reign's own Ashley systems run within one tick, placed
 * in the gaps between the engine's {@link SystemPriority} values. Ashley runs
 * lower priorities first.
 *
 * <p>One tick runs, in order: input, AI, the engine's movement and mob
 * separation, casting, spells, contact damage, death, waves, and animation,
 * and then the engine's particles and lifetime.
 */
public final class GamePriority {
    /** Player input becomes velocity and casting intent. */
    public static final int INPUT     = 100;
    /** Enemy AI sets velocity and casting intent. */
    public static final int AI        = 200;
    /** Casting spells. */
    public static final int CASTING   = 500;
    /** Spell flight, and the hits and damage that spells deal. */
    public static final int SPELLS    = 600;
    /** Damage that mobs deal by touching each other. */
    public static final int CONTACT   = 620;
    /** Death of mobs with no health left. */
    public static final int DEATH     = 650;
    /** Waves of enemies. */
    public static final int WAVES     = 700;
    /** Facing and walking animation, from the velocity that a mob asked for. */
    public static final int ANIMATION = 750;

    private GamePriority() {
    }
}
