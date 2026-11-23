package com.southcentralpositronics.reign_gdx.engine.entity.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.southcentralpositronics.reign_gdx.engine.entity.component.LifetimeComponent;

/** Ages every entity that has a lifetime by one tick, and removes it once the lifetime has passed. */
public final class LifetimeSystem extends IteratingSystem {
    public LifetimeSystem() {
        super(Family.all(LifetimeComponent.class).get(), SystemPriority.LIFETIME);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        LifetimeComponent lifetime = LifetimeComponent.MAPPER.get(entity);
        lifetime.ageTicks++;
        if (lifetime.ageTicks > lifetime.lifeTicks) getEngine().removeEntity(entity);
    }
}
