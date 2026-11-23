package com.southcentralpositronics.reign_gdx.game.entity.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Pool;

/**
 * Lets an enemy hurt the player by touching it: one hit of {@link #damage}
 * each time its cooldown runs out while the two bodies touch.
 */
public final class ContactDamageComponent implements Component, Pool.Poolable {
    /** Reads this component from an entity. */
    public static final ComponentMapper<ContactDamageComponent> MAPPER =
        ComponentMapper.getFor(ContactDamageComponent.class);

    /** The health that one touch takes from the player. */
    public double damage;
    /** The seconds between touches that hurt. */
    public float  delay;
    /** The seconds left before the next touch can hurt; 0 or less when it can. */
    public float  cooldown;

    @Override
    public void reset() {
        damage   = 0;
        delay    = 0f;
        cooldown = 0f;
    }
}
