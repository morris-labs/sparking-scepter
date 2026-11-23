package com.southcentralpositronics.reign_gdx.engine.entity.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Pool;

/**
 * Removes an entity after a set number of ticks. The
 * {@link com.southcentralpositronics.reign_gdx.engine.entity.system.LifetimeSystem}
 * class counts the ticks and removes the entity on the tick after its
 * lifetime runs out.
 */
public final class LifetimeComponent implements Component, Pool.Poolable {
    /** Reads this component from an entity. */
    public static final ComponentMapper<LifetimeComponent> MAPPER = ComponentMapper.getFor(LifetimeComponent.class);

    /** The ticks that the entity lives. */
    public int lifeTicks;
    /** The ticks that the entity has lived so far. */
    public int ageTicks;

    @Override
    public void reset() {
        lifeTicks = 0;
        ageTicks  = 0;
    }
}
