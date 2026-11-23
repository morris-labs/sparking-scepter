package com.southcentralpositronics.reign_gdx.engine.entity.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.southcentralpositronics.reign_gdx.engine.entity.component.ParticleComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.PositionComponent;
import com.southcentralpositronics.reign_gdx.engine.level.Level;

/**
 * Moves particles by one tick: gravity pulls them down, they bounce off the
 * ground, and they bounce off solid tiles. The physics match the Java2D
 * {@code Particle} class step for step, so particles look the same.
 */
public final class ParticleSystem extends IteratingSystem {
    /** The pull of gravity, in pixels per tick per tick. */
    private static final double GRAVITY       = 0.1;
    /** The share of vertical speed that a bounce off the ground keeps. */
    private static final double GROUND_BOUNCE = 0.5;

    private final Level level;

    /**
     * Creates the system.
     *
     * @param level The level whose solid tiles particles bounce off.
     */
    public ParticleSystem(Level level) {
        super(Family.all(ParticleComponent.class, PositionComponent.class).get(), SystemPriority.PARTICLES);
        this.level = level;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PositionComponent position = PositionComponent.MAPPER.get(entity);
        ParticleComponent particle = ParticleComponent.MAPPER.get(entity);

        particle.velocityZ -= GRAVITY;
        if (particle.z <= 0) particle.velocityZ *= -GROUND_BOUNCE;

        if (blocked(position.x + particle.velocityX, position.y)) particle.velocityX *= -1;
        if (blocked(position.x, position.y + particle.velocityY)) {
            // The Java2D version also flipped the vertical speed here. Keep it so particles look the same.
            particle.velocityY *= -1;
            particle.velocityZ *= -1;
        }

        position.x += particle.velocityX;
        position.y += particle.velocityY;
        particle.z += particle.velocityZ;
    }

    private boolean blocked(double x, double y) {
        return level.tileCollision(x, y, 1, 1);
    }
}
