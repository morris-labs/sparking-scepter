package com.southcentralpositronics.reign_gdx.game.entity.projectile;

import com.southcentralpositronics.reign_gdx.engine.entity.Entity;

/** A fireball that flies 150 px at 300 px per second. */
public class SpellProjectile extends Projectile {
    /** The flight speed, in pixels per second: 5 px per tick. */
    public static final double SPEED = 300;
    /** The farthest a spell flies, in pixels. */
    public static final double RANGE = 150;

    public SpellProjectile(double x, double y, double angle, Entity firedBy) {
        super(x, y, angle);
        this.firedBy = firedBy;
        this.range   = RANGE;
        this.speed   = SPEED;
    }
}
