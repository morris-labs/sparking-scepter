package com.southcentralpositronics.reign_gdx.engine.entity;

import com.badlogic.ashley.core.Engine;
import com.southcentralpositronics.reign_gdx.engine.entity.component.LifetimeComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.ParticleComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.PositionComponent;

import java.util.Random;

/**
 * Bursts particles into an Ashley engine, for example where a spell hits or a
 * mob dies. It replaces the old particle {@code Spawner} class, so "spawn
 * point" only means where enemies enter.
 */
public final class ParticleEmitter {
    /** The most ticks that each particle adds to a burst's base lifetime. */
    public static final int MAX_EXTRA_LIFE_TICKS = 14;

    private final Engine engine;
    private final Random random;

    /**
     * Creates an emitter.
     *
     * @param engine The engine that holds the particles.
     * @param random The source of each particle's scatter and lifetime. Keep it
     *               apart from the random source of the rules, so particles
     *               don't change where enemies spawn.
     */
    public ParticleEmitter(Engine engine, Random random) {
        this.engine = engine;
        this.random = random;
    }

    /**
     * Adds a burst of particles at one point. Each particle scatters at a
     * random ground velocity, starts 2 to 3 px above the ground, and lives the
     * base lifetime plus 0 to {@link #MAX_EXTRA_LIFE_TICKS} ticks. The
     * particles first move on the next tick.
     *
     * @param x         The x coordinate of the ground point, in pixels.
     * @param y         The y coordinate of the ground point, in pixels.
     * @param lifeTicks The base lifetime, in ticks.
     * @param amount    The number of particles.
     */
    public void burst(double x, double y, int lifeTicks, int amount) {
        for (int i = 0; i < amount; i++) {
            com.badlogic.ashley.core.Entity entity = engine.createEntity();

            PositionComponent position = engine.createComponent(PositionComponent.class);
            position.x = x;
            position.y = y;

            LifetimeComponent lifetime = engine.createComponent(LifetimeComponent.class);
            lifetime.lifeTicks = lifeTicks + random.nextInt(MAX_EXTRA_LIFE_TICKS + 1);

            ParticleComponent particle = engine.createComponent(ParticleComponent.class);
            particle.velocityX = random.nextGaussian();
            particle.velocityY = random.nextGaussian();
            particle.z         = random.nextFloat() + 2.0;

            entity.add(position);
            entity.add(lifetime);
            entity.add(particle);
            engine.addEntity(entity);
        }
    }
}
