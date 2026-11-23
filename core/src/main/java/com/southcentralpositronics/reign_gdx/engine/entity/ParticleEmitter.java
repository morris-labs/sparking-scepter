package com.southcentralpositronics.reign_gdx.engine.entity;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.southcentralpositronics.reign_gdx.engine.entity.component.LifetimeComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.ParticleComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.PositionComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.system.SystemPriority;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Bursts particles into an Ashley engine, for example where a spell hits or a
 * mob dies. It replaces the old particle {@code Spawner} class, so "spawn
 * point" only means where enemies enter.
 *
 * <p>The emitter is also the last system in each tick: a burst asked for
 * during a tick joins the engine when the tick ends, so its particles first
 * age and move on the next tick, whichever system asked for it. The
 * {@link com.southcentralpositronics.reign_gdx.engine.level.Level} constructor
 * adds the emitter to its engine.
 */
public final class ParticleEmitter extends EntitySystem {
    /** The most ticks that each particle adds to a burst's base lifetime. */
    public static final int MAX_EXTRA_LIFE_TICKS = 14;

    private final Engine      engine;
    private final Random      random;
    private final List<Burst> pending = new ArrayList<>();

    /**
     * Creates an emitter.
     *
     * @param engine The engine that holds the particles.
     * @param random The source of each particle's scatter and lifetime. Keep it
     *               apart from the random source of the rules, so particles
     *               don't change where enemies spawn.
     */
    public ParticleEmitter(Engine engine, Random random) {
        super(SystemPriority.EMIT);
        this.engine = engine;
        this.random = random;
    }

    /**
     * Asks for a burst of particles at one point. Each particle scatters at a
     * random ground velocity, starts 2 to 3 px above the ground, and lives the
     * base lifetime plus 0 to {@link #MAX_EXTRA_LIFE_TICKS} ticks. The
     * particles join the engine at the end of the tick and first move on the
     * next one.
     *
     * @param x         The x coordinate of the ground point, in pixels.
     * @param y         The y coordinate of the ground point, in pixels.
     * @param lifeTicks The base lifetime, in ticks.
     * @param amount    The number of particles.
     */
    public void burst(double x, double y, int lifeTicks, int amount) {
        pending.add(new Burst(x, y, lifeTicks, amount));
    }

    /** Adds every burst asked for since the last tick ended to the engine. */
    @Override
    public void update(float deltaTime) {
        for (Burst burst : pending) emit(burst);
        pending.clear();
    }

    private void emit(Burst burst) {
        for (int i = 0; i < burst.amount; i++) {
            Entity entity = engine.createEntity();

            PositionComponent position = engine.createComponent(PositionComponent.class);
            position.x = burst.x;
            position.y = burst.y;

            LifetimeComponent lifetime = engine.createComponent(LifetimeComponent.class);
            lifetime.lifeTicks = burst.lifeTicks + random.nextInt(MAX_EXTRA_LIFE_TICKS + 1);

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

    /** One burst waiting for the end of the tick. */
    private record Burst(double x, double y, int lifeTicks, int amount) {
    }
}
