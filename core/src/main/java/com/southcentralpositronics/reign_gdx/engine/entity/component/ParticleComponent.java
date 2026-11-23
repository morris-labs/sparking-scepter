package com.southcentralpositronics.reign_gdx.engine.entity.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Pool;

/**
 * Marks an entity as a particle: one red pixel with a height above the ground
 * that bounces off solid tiles. Particles have no effect on play.
 *
 * <p>The velocities are in pixels per tick, not per second, because particle
 * physics were tuned per frame at 60 FPS and the tick runs at that rate.
 */
public final class ParticleComponent implements Component, Pool.Poolable {
    /** Reads this component from an entity. */
    public static final ComponentMapper<ParticleComponent> MAPPER = ComponentMapper.getFor(ParticleComponent.class);

    /** The height above the ground, in pixels. */
    public double z;
    /** The ground velocity along x, in pixels per tick. */
    public double velocityX;
    /** The ground velocity along y, in pixels per tick. */
    public double velocityY;
    /** The vertical velocity, in pixels per tick. */
    public double velocityZ;

    @Override
    public void reset() {
        z         = 0;
        velocityX = 0;
        velocityY = 0;
        velocityZ = 0;
    }
}
