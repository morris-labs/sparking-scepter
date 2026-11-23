package com.southcentralpositronics.reign_gdx.engine.entity;

import com.southcentralpositronics.reign_gdx.engine.level.Level;

/**
 * One red pixel that bounces off solid tiles and expires. Particles have no
 * effect on play; a renderer draws each one at {@link #getDrawX()} and
 * {@link #getDrawY()}.
 */
public class Particle extends Entity {
    protected double xx, yy, zz, xa, ya, za;

    /**
     * Creates a particle.
     *
     * @param x     The x coordinate where it starts.
     * @param y     The y coordinate where it starts.
     * @param angle Unused; particles scatter at random.
     * @param life  The base lifetime, in ticks. Each particle adds up to 14 more.
     * @param level The level that the particle bounces in.
     */
    public Particle(double x, double y, double angle, int life, Level level) {
        this.x    = x;
        this.y    = y;
        this.xx   = x;
        this.yy   = y;
        this.life = life + random.nextInt(0, 15);
        this.xa   = random.nextGaussian();
        this.ya   = random.nextGaussian();
        this.zz   = random.nextFloat() + 2.0;

        init(level);
    }

    /** Advances the particle by one tick. */
    public void update() {
        time++;
        if (time >= Integer.MAX_VALUE - 1) {
            time = 0;
        }
        if (time > life) {
            remove();
        }

        za -= 0.1;
        if (zz <= 0) {
            za *= -0.5;
            xa *= 1;
            ya *= 1;
        }

        move();
    }

    private void move() {
        if (collision(xa, 0)) {
            this.xa *= -1;
        }
        if (collision(0, ya)) {
            this.ya *= -1;
            this.za *= -1;
        }

        this.xx += xa;
        this.yy += ya;
        this.zz += za;
    }

    public boolean collision(double xb, double yb) {
        return level.tileCollision(xx + xb, yy + yb, 1, 1);
    }

    /** Gets the x coordinate to draw the particle at: its ground position. */
    public double getDrawX() {
        return xx;
    }

    /**
     * Gets the y coordinate to draw the particle at: its ground position,
     * raised by its height. The Java2D version drew at (x - 5, y - z + 5) for
     * its y-down screen; in libGDX's y-up world that put particles 5 px left of
     * and below where they collide, and made height push them down.
     */
    public double getDrawY() {
        return yy + zz;
    }
}
