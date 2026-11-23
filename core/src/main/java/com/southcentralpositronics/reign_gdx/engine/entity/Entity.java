package com.southcentralpositronics.reign_gdx.engine.entity;

import com.badlogic.gdx.math.Vector2;
import com.southcentralpositronics.reign_gdx.engine.level.Level;

import java.util.Random;

/** Anything on the old class tree that lives in a level: mobs and projectiles. Particles are Ashley entities. */
public class Entity {
    protected int paddingLeft, paddingRight, paddingTop, paddingBottom;
    protected final Random  random  = new Random();
    protected       boolean removed = false;
    protected       int     life;
    protected       int     time    = 0;
    protected       double  x;
    protected       double  y;
    protected       Level   level;
    protected       Type    type;

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    /** Runs once per frame. Subclasses override it. */
    public void update() {
    }

    public void init(Level level) {
        this.level = level;
    }

    /**
     * Marks the entity as removed and triggers onRemove hook.
     * Subclasses can override onRemove() to add custom removal behavior.
     */
    public void remove() {
        if (!removed) {
            removed = true;
            onRemove();
        }
    }

    /** Runs once when the entity is removed. Subclasses can override it. */
    protected void onRemove() {
    }

    public boolean isRemoved() {
        return removed;
    }

    public Type getType() {
        return type;
    }

    public Vector2 getPosition() {
        return new Vector2((float) x, (float) y);
    }

    public float distanceTo(Entity other) {
        return this.getPosition().dst(other.getPosition());
    }

    public enum Type {
        ENEMY, PLAYER, PARTICLE, PROJECTILE, SPAWNER
    }
}
