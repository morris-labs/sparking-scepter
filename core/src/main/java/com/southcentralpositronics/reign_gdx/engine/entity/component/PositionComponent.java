package com.southcentralpositronics.reign_gdx.engine.entity.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Pool;

/**
 * Where an entity is in the world, in pixels, with libGDX axes: the origin is
 * the arena's bottom-left corner and y points up. Each kind of entity says
 * which point of itself the position marks; for a particle, it's the ground
 * point under it.
 */
public final class PositionComponent implements Component, Pool.Poolable {
    /** Reads this component from an entity. */
    public static final ComponentMapper<PositionComponent> MAPPER = ComponentMapper.getFor(PositionComponent.class);

    public double x;
    public double y;

    @Override
    public void reset() {
        x = 0;
        y = 0;
    }
}
