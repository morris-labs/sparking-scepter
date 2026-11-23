package com.southcentralpositronics.reign_gdx.engine.entity.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Pool;

/** A mob's health. Damage lowers it to no less than zero, and healing raises it to no more than the maximum. */
public final class HealthComponent implements Component, Pool.Poolable {
    /** Reads this component from an entity. */
    public static final ComponentMapper<HealthComponent> MAPPER = ComponentMapper.getFor(HealthComponent.class);

    /** Absorbs floating-point error, so that ten hits of 0.1 empty a health of 1. */
    public static final double EPSILON = 1e-9;

    /** The health left. */
    public double health;
    /** The most health the entity can have. */
    public double max;

    /** Lowers the health by an amount, to no less than zero. */
    public void damage(double amount) {
        health = Math.max(0.0, health - amount);
    }

    /** Raises the health by an amount, to no more than {@link #max}. */
    public void heal(double amount) {
        health = Math.min(max, health + amount);
    }

    /** Checks whether no health is left. */
    public boolean isEmpty() {
        return health <= EPSILON;
    }

    @Override
    public void reset() {
        health = 0;
        max    = 0;
    }
}
