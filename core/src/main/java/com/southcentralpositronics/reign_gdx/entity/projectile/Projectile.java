package com.southcentralpositronics.reign_gdx.entity.projectile;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.southcentralpositronics.reign_gdx.entity.Entity;
import com.southcentralpositronics.reign_gdx.entity.spawner.ParticleSpawner;
import com.southcentralpositronics.reign_gdx.level.Level;

public abstract class Projectile extends Entity {
    /** The time between shots, in seconds: 8 frames at 60 FPS. */
    public static final float SHOT_DELAY = 0.125f;

    protected Vector2 position;
    protected Vector2 origin;
    protected Vector2 velocity;

    protected double angle;
    protected double speed;
    protected double range;

    protected TextureRegion texture;
    protected Entity        firedBy;


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

    public void update() {
        if (removed) return;
        float   x       = position.x;
        float   y       = position.y;
        double  xDelta  = velocity.x * speed;
        double  yDelta  = velocity.y * speed;
        int     width   = texture.getRegionWidth();
        int     height  = texture.getRegionHeight();
        Vector2 nextPos = position.cpy().add((float) xDelta, (float) yDelta);
        boolean hitWall = level.tileCollision(x - width / 2.0 + xDelta, y - height / 2.0 + yDelta, width, height,
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

    public void render(Batch batch) {
        if (texture != null && !removed) {
            batch.draw(texture, position.x - texture.getRegionWidth() / 2f, position.y - texture.getRegionHeight() / 2f);
        }
    }

    public void hitTarget() {
        if (removed) return;
        level.add(new ParticleSpawner(position.x, position.y, angle, 10, 1000, level));
        removed = true;
    }

    @Override
    public void remove() {
        if (removed) return;
        level.add(new ParticleSpawner(position.x, position.y, angle, 10, 250, level));
        removed = true;
    }

    public void init(Level level) {
        this.level = level;
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

    public TextureRegion getTexture() {
        return texture;
    }
}
