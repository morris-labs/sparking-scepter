package com.southcentralpositronics.reign_gdx.engine.entity.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Pool;

/**
 * The velocity that an entity asks to move at, in pixels per second. Input
 * and AI systems set it each tick, and the movement system moves the entity
 * by it, stopping at tiles that block movement.
 */
public final class VelocityComponent implements Component, Pool.Poolable {
    /** Reads this component from an entity. */
    public static final ComponentMapper<VelocityComponent> MAPPER = ComponentMapper.getFor(VelocityComponent.class);

    /** The velocity along x, in pixels per second. */
    public double x;
    /** The velocity along y, in pixels per second. */
    public double y;

    /** Checks whether the entity asks to move at all. */
    public boolean isMoving() {
        return x != 0 || y != 0;
    }

    @Override
    public void reset() {
        x = 0;
        y = 0;
    }
}
