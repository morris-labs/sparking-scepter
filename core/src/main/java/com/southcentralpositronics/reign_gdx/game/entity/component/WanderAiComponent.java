package com.southcentralpositronics.reign_gdx.game.entity.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Pool;

/**
 * Wanders in a random direction, or stands still, and turns at random
 * intervals. After the first turn it moves along one axis at a time. It turns
 * back from a tile that blocks movement.
 */
public final class WanderAiComponent implements Component, Pool.Poolable {
    /** Reads this component from an entity. */
    public static final ComponentMapper<WanderAiComponent> MAPPER = ComponentMapper.getFor(WanderAiComponent.class);

    /** The walking speed along each axis, in pixels per second. */
    public double speed;
    /** The direction along x: -1, 0, or 1. */
    public int    directionX;
    /** The direction along y: -1, 0, or 1. */
    public int    directionY;
    /** The shortest time between turns, in seconds. */
    public float  minTurnSeconds;
    /** The longest time between turns, in seconds. */
    public float  maxTurnSeconds;
    /** The seconds since the last turn. */
    public float  secondsSinceTurn;

    @Override
    public void reset() {
        speed            = 0;
        directionX       = 0;
        directionY       = 0;
        minTurnSeconds   = 0f;
        maxTurnSeconds   = 0f;
        secondsSinceTurn = 0f;
    }
}
