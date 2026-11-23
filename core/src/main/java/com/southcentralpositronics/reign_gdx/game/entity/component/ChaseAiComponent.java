package com.southcentralpositronics.reign_gdx.game.entity.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Pool;

/** Walks straight at the player while the player is in range, ignoring walls. */
public final class ChaseAiComponent implements Component, Pool.Poolable {
    /** Reads this component from an entity. */
    public static final ComponentMapper<ChaseAiComponent> MAPPER = ComponentMapper.getFor(ChaseAiComponent.class);

    /** The walking speed along each axis, in pixels per second. */
    public double speed;
    /** The farthest the player can be for the chase to start, in pixels. */
    public double range;

    @Override
    public void reset() {
        speed = 0;
        range = 0;
    }
}
