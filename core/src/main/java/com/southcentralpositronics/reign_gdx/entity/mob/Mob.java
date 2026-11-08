package com.southcentralpositronics.reign_gdx.entity.mob;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.southcentralpositronics.reign_gdx.entity.Entity;
import com.southcentralpositronics.reign_gdx.entity.projectile.Projectile;
import com.southcentralpositronics.reign_gdx.entity.projectile.SpellProjectile;
import com.southcentralpositronics.reign_gdx.entity.spawner.ParticleSpawner;
import com.southcentralpositronics.reign_gdx.graphics.AnimatedSprite;
import com.southcentralpositronics.reign_gdx.input.Mouse;
import com.southcentralpositronics.reign_gdx.util.Vector2i;

import java.util.List;

public class Mob extends Entity {
    protected static final int   DETECTION_DISTANCE = 100;
    protected static final int   tileSize           = 16;
    protected static final int   mobSize            = 32;
    protected              float shootCooldown      = 0f;
    protected static final float SHOOT_DELAY        = 0.5f; // 0.2 seconds between shots
    /** The atlas region prefix of the enemy sprite, ported from the Java2D dummy sheet. */
    protected static final String ENEMY_SPRITE      = "King_Berno";

    protected AnimatedSprite mobUp, mobDown, mobLeft, mobRight;
    protected AnimatedSprite animSprite;
    protected Direction      direction;
    protected Viewport       viewport;

    protected int          collisionOffsetX;
    protected int          collisionOffsetY;
    protected boolean      walking;
    protected double       health;
    protected TextureAtlas atlas;



//    public Mob(String path) {
//        walking          = false;
//        dir              = Direction.DOWN;
//        collisionOffsetX = -24;
//        collisionOffsetY = -32;
//
//        atlas = new TextureAtlas(Gdx.files.internal(path));
//
//        mobUp    = new LibGDXAnimatedSprite(atlas, "King_Berno_Up", 0.2f, true);
//        mobDown  = new LibGDXAnimatedSprite(atlas, "King_Berno_Down", 0.2f, true);
//        mobLeft  = new LibGDXAnimatedSprite(atlas, "King_Berno_Left", 0.2f, true);
//        mobRight = new LibGDXAnimatedSprite(atlas, "King_Berno_Right", 0.2f, true);
//
//        animSprite = mobDown;
//    }

    /**
     * Loads the four walking animations for this mob and faces it down.
     *
     * @param atlas  The atlas that holds the mob sprites.
     * @param prefix The region prefix; the atlas must hold
     *               {@code PREFIX_Up}, {@code PREFIX_Down}, {@code PREFIX_Left},
     *               and {@code PREFIX_Right} regions.
     */
    protected void loadAnimations(TextureAtlas atlas, String prefix) {
        this.atlas = atlas;
        mobUp      = new AnimatedSprite(atlas, prefix + "_Up", 0.2f, true);
        mobDown    = new AnimatedSprite(atlas, prefix + "_Down", 0.2f, true);
        mobLeft    = new AnimatedSprite(atlas, prefix + "_Left", 0.2f, true);
        mobRight   = new AnimatedSprite(atlas, prefix + "_Right", 0.2f, true);
        animSprite = mobDown;
        direction  = Direction.DOWN;
    }

    public void move(double xa, double ya) {
        if (xa > 0) direction = Direction.RIGHT;
        else if (xa < 0) direction = Direction.LEFT;

        if (ya > 0) direction = Direction.DOWN;
        else if (ya < 0) direction = Direction.UP;

        // Move x
        for (int xStep = 0; xStep < Math.abs(xa); xStep++) {
            double stepX = xa > 0 ? 1 : -1;
            if (!collision(x + stepX, y)) {
                x += stepX;
            } else {
                break;
            }
        }

        // Move y
        for (int yStep = 0; yStep < Math.abs(ya); yStep++) {
            double stepY = ya > 0 ? 1 : -1;
            if (!collision(x, y + stepY)) {
                y += stepY;
            } else {
                break;
            }
        }

        // Clamp to map bounds
        int mapPixelWidth  = level.getWidth() * tileSize;
        int mapPixelHeight = level.getHeight() * tileSize;

        x = MathUtils.clamp(x, 0, mapPixelWidth - tileSize);
        y = MathUtils.clamp(y, 0, mapPixelHeight - tileSize);

        walking = xa != 0 || ya != 0;
    }


    public void render(SpriteBatch batch) {
        if (animSprite != null) {
            TextureRegion frame = animSprite.getCurrentFrame();
            batch.draw(frame, (float) x, (float) y);
        }
    }


    private int abs(double value) {
        return value < 0 ? -1 : 1;
    }

    protected void shootAtPos(int x, int y, double angle) {
        TextureAtlas tileAtlas  = level.getGame().getTileAtlas();
        Projectile   projectile = new SpellProjectile(x, y - 16, angle, this, tileAtlas);
        level.add(projectile);
    }

    /**
     * Fires at the closest player within {@link #DETECTION_DISTANCE}, at most
     * once every {@link Projectile#SHOT_DELAY} seconds. The caller must count
     * {@code shootCooldown} down by the frame time.
     */
    protected void shootClosest() {
        if (shootCooldown > 0f) return;

        PlayerMob       closestPlayer = null;
        List<PlayerMob> players       = level.getPlayersInRadius(this, DETECTION_DISTANCE);
        double          minDist       = Double.MAX_VALUE;

        for (PlayerMob player : players) {
            double dist = Vector2i.getDistance(new Vector2i((int) x, (int) y), new Vector2i((int) player.getX(), (int) player.getY()));

            if (dist < minDist) {
                minDist       = dist;
                closestPlayer = player;
            }
        }

        if (closestPlayer != null) {
            double dx          = closestPlayer.getX() - x;
            double dy          = closestPlayer.getY() - y;
            double firingAngle = Math.atan2(dy, dx);
            shootAtPos((int) x, (int) y, firingAngle);
            shootCooldown = Projectile.SHOT_DELAY;
        }
    }

    protected void firePlayerSpell() {
        if (shootCooldown > 0f) return;

        Vector2 worldMouse = viewport.unproject(new Vector2(Mouse.getX(), Mouse.getY()));
        float   dx         = worldMouse.x - (float) this.x;
        float   dy         = worldMouse.y - (float) this.y;
        float   angle      = (float) Math.atan2(dy, dx);

        shootAtPos((int) x + 20, (int) y + 20, angle);

        shootCooldown = SHOOT_DELAY; // reset cooldown
    }

    /**
     * Base update function that should be called by all extending mobs.
     */
    public void update(float delta) {

        if (shootCooldown > 0f) {
            shootCooldown -= Gdx.graphics.getDeltaTime();
        }
        updateAnimation(delta, walking, direction);
    }

    /**
     * Updates the current animation frame based on movement direction and walking state.
     */
    protected void updateAnimation(float delta, boolean isWalking, Direction direction) {
        if (mobUp == null || mobDown == null || mobLeft == null || mobRight == null) return;

        // Change animation direction
        switch (direction) {
            case UP -> animSprite = mobUp;
            case DOWN -> animSprite = mobDown;
            case LEFT -> animSprite = mobLeft;
            case RIGHT -> animSprite = mobRight;
        }

        if (isWalking) {
            animSprite.update(delta);
        } else {
            animSprite.setFrame(0);
        }
    }

    protected boolean collision(double x, double y) {
        return level.tileCollision(x, y, mobSize, mobSize, paddingLeft, paddingRight, paddingTop, paddingBottom);
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    @Override
    public void remove() {
        super.remove();
        new ParticleSpawner(x, y, 0, 10, 100, level);
    }

    public int getPaddingLeft() {
        return paddingLeft;
    }

    public int getPaddingRight() {
        return paddingRight;
    }

    public int getPaddingTop() {
        return paddingTop;
    }

    public int getPaddingBottom() {
        return paddingBottom;
    }
}
