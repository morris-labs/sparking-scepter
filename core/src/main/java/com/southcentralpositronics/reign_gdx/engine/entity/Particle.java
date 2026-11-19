package com.southcentralpositronics.reign_gdx.engine.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.southcentralpositronics.reign_gdx.engine.level.Level;

public class Particle extends Entity {
    protected double xx, yy, zz, xa, ya, za;

    /**
     * One red pixel that every particle draws. A texture per particle leaked
     * GPU memory and broke sprite batching: a single fireball spawns up to
     * 1,000 particles.
     */
    private static TextureRegion pixel;

    /** Gets the shared red pixel, creating it on first use on the GL thread. */
    private static TextureRegion pixel() {
        if (pixel == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.RED);
            pixmap.fill();
            pixel = new TextureRegion(new Texture(pixmap));
            pixmap.dispose();
        }
        return pixel;
    }


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

    /**
     * Draws the particle at its ground position, raised by its height. The
     * Java2D version drew at (x - 5, y - z + 5) for its y-down screen; in
     * libGDX's y-up world that put particles 5 px left of and below where
     * they collide, and made height push them down.
     */
    public void render(SpriteBatch batch) {
        batch.draw(pixel(), (float) xx, (float) (yy + zz));
    }
}
