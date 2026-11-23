package com.southcentralpositronics.reign_gdx.game.entity.projectile;

import com.badlogic.gdx.math.Vector2;
import com.southcentralpositronics.reign_gdx.engine.entity.Entity;
import com.southcentralpositronics.reign_gdx.engine.time.FixedTimestep;

/**
 * A projectile that flies in a straight line until it hits a solid tile, a
 * mob, or the end of its range. Projectiles hold no textures; a renderer draws
 * them by kind.
 */
public abstract class Projectile extends Entity {
    /** The time between shots, in seconds: 8 ticks. */
    public static final float SHOT_DELAY = 0.125f;
    /** The width and height of a projectile's sprite, in pixels. */
    public static final int   SIZE       = 16;

    protected Vector2 position;
    protected Vector2 origin;
    protected Vector2 velocity;

    protected double angle;
    /** The flight speed, in pixels per second. */
    protected double speed;
    /** The farthest the projectile flies, in pixels. */
    protected double range;

    protected Entity firedBy;


    /**
     * Creates a projectile.
     *
     * @param x         The x coordinate of the projectile's center.
     * @param y         The y coordinate of the projectile's center.
     * @param direction The direction, in radians, counterclockwise from +x.
     */
    public Projectile(double x, double y, double direction) {
        this.position = new Vector2((float) x, (float) y);
        this.origin   = new Vector2((float) x, (float) y);
        this.angle    = direction;
        // A 4x4 px wall-collision box at the center of the 16x16 sprite.
        paddingLeft   = 6;
        paddingRight  = 6;
        paddingTop    = 6;
        paddingBottom = 6;
        this.velocity = new Vector2((float) Math.cos(direction), (float) Math.sin(direction));
    }

    /** Advances the projectile by one tick. */
    public void update() {
        if (removed) return;
        float   x       = position.x;
        float   y       = position.y;
        double  step    = FixedTimestep.perTick(speed);
        double  xDelta  = velocity.x * step;
        double  yDelta  = velocity.y * step;
        Vector2 nextPos = position.cpy().add((float) xDelta, (float) yDelta);
        boolean hitWall = level.tileCollision(x - SIZE / 2.0 + xDelta, y - SIZE / 2.0 + yDelta, SIZE, SIZE,
                                              paddingLeft, paddingRight, paddingTop, paddingBottom);

        if (hitWall) {
            remove();
            return;
        }

        position.set(nextPos);

        if (position.dst(origin) > range) {
            remove();
        }
    }

    public void hitTarget() {
        if (removed) return;
        level.getParticleEmitter().burst(position.x, position.y, 10, 1000);
        removed = true;
    }

    @Override
    public void remove() {
        if (removed) return;
        level.getParticleEmitter().burst(position.x, position.y, 10, 250);
        removed = true;
    }

    public Entity getFiredBy() {
        return firedBy;
    }

    public double getX() {
        return position.x;
    }

    public double getY() {
        return position.y;
    }

    /** Gets the x coordinate of the projectile's center. */
    public double getCenterX() {
        return position.x;
    }

    /** Gets the y coordinate of the projectile's center. */
    public double getCenterY() {
        return position.y;
    }
}
