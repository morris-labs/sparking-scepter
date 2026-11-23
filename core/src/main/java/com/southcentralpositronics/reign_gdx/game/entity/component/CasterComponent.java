package com.southcentralpositronics.reign_gdx.game.entity.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Pool;

/**
 * Lets a mob cast spells. Input and AI systems set {@link #casting} and the
 * aim point each tick; the casting system casts when the cooldown allows.
 */
public final class CasterComponent implements Component, Pool.Poolable {
    /** Reads this component from an entity. */
    public static final ComponentMapper<CasterComponent> MAPPER = ComponentMapper.getFor(CasterComponent.class);

    /** The seconds between casts. */
    public float   shotDelay;
    /** The seconds left before the next cast; 0 or less when the mob can cast. */
    public float   cooldown;
    /** Whether the mob wants to cast this tick. */
    public boolean casting;
    /** The x coordinate of the world point to cast toward. */
    public double  aimX;
    /** The y coordinate of the world point to cast toward. */
    public double  aimY;
    /** The health that each of this mob's spells takes from the mob it hits. */
    public double  spellDamage;

    @Override
    public void reset() {
        shotDelay   = 0f;
        cooldown    = 0f;
        casting     = false;
        aimX        = 0;
        aimY        = 0;
        spellDamage = 0;
    }
}
