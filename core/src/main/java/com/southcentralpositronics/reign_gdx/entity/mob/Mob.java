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
    /** The health an enemy loses per hit, as in the Java2D ShooterMob: 10 hits kill. */
    protected static final double ENEMY_HIT_DAMAGE  = 0.1;
    /** Absorbs floating-point error so 10 hits of 0.1 reach zero. */
    protected static final double HEALTH_EPSILON    = 1e-9;
    /** The height of the body that projectiles can hit, from the feet up, in pixels. */
    protected static final int    HURT_HEIGHT       = 24;
    /** How far outside the body a projectile's center still counts as a hit, in pixels. */
    protected static final int    HIT_RADIUS        = 4;
    /** The atlas region prefix of the enemy sprite, ported from the Java2D dummy sheet. */
    protected static final String ENEMY_SPRITE      = "King_Berno";

    protected AnimatedSprite mobUp, mobDown, mobLeft, mobRight;
    protected AnimatedSprite animSprite;
    protected Direction      direction;
    protected Viewport       viewport;

    protected boolean      walking;
    protected double       health;
    protected TextureAtlas atlas;



//    public Mob(String path) {
//        walking          = false;
//        dir              = Direction.DOWN;
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
     * Gives every mob the player's collision box: a 16x8 px box at the feet,
     * centered at the bottom of the 32x32 sprite. A mob therefore fits in a
     * corridor one tile wide.
     */
    protected Mob() {
        health        = 1.0;
        paddingLeft   = 8;
        paddingRight  = 8;
        paddingTop    = 24;
        paddingBottom = 0;
    }

    /**
     * Places the mob so that its collision box fills the bottom half of a tile.
     *
     * @param tx The tile column.
     * @param ty The tile row.
     */
    public void placeOnTile(int tx, int ty) {
        x = tx * tileSize - paddingLeft;
        y = ty * tileSize - paddingBottom;
    }

    /** Gets the tile column under the center of the collision box. */
    public int getTileX() {
        double centerX = x + paddingLeft + (mobSize - paddingLeft - paddingRight) / 2.0;
        return (int) Math.floor(centerX / tileSize);
    }

    /** Gets the tile row under the center of the collision box. */
    public int getTileY() {
        double centerY = y + paddingBottom + (mobSize - paddingTop - paddingBottom) / 2.0;
        return (int) Math.floor(centerY / tileSize);
    }

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

        // Clamp so the collision box stays on the map.
        x = MathUtils.clamp(x, -paddingLeft, mapPixelWidth - mobSize + paddingRight);
        y = MathUtils.clamp(y, -paddingBottom, mapPixelHeight - mobSize + paddingTop);

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

    /**
     * Fires a spell from this mob's body center.
     *
     * @param angle The direction, in radians, counterclockwise from +x.
     */
    protected void shootAt(double angle) {
        TextureAtlas tileAtlas  = level.getGame().getTileAtlas();
        Projectile   projectile = new SpellProjectile(getBodyCenterX(), getBodyCenterY(), angle, this, tileAtlas);
        level.add(projectile);
    }

    /** Gets the x coordinate of the center of the body that projectiles hit. */
    public double getBodyCenterX() {
        return x + paddingLeft + (mobSize - paddingLeft - paddingRight) / 2.0;
    }

    /** Gets the y coordinate of the center of the body that projectiles hit. */
    public double getBodyCenterY() {
        return y + HURT_HEIGHT / 2.0;
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
            double dx          = closestPlayer.getBodyCenterX() - getBodyCenterX();
            double dy          = closestPlayer.getBodyCenterY() - getBodyCenterY();
            shootAt(Math.atan2(dy, dx));
            shootCooldown = Projectile.SHOT_DELAY;
        }
    }

    protected void firePlayerSpell() {
        if (shootCooldown > 0f) return;

        Vector2 worldMouse = viewport.unproject(new Vector2(Mouse.getX(), Mouse.getY()));
        double  dx         = worldMouse.x - getBodyCenterX();
        double  dy         = worldMouse.y - getBodyCenterY();
        shootAt(Math.atan2(dy, dx));

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

    /**
     * Applies every projectile that hits this mob. Enemies take hits from
     * player projectiles, and players take hits from enemy projectiles.
     * {@link Level#update(float)} calls this once per frame.
     */
    public void checkForHits() {
        if (removed) return;
        List<Projectile> projectiles = level.getProjectiles();
        for (int i = 0, n = projectiles.size(); i < n; i++) {
            Projectile projectile = projectiles.get(i);
            if (projectile.isRemoved()) continue;
            boolean firedByPlayer = projectile.getFiredBy() instanceof PlayerMob;
            if (firedByPlayer == (this instanceof PlayerMob)) continue;
            if (!isHitBy(projectile)) continue;
            projectile.hitTarget();
            onHit(projectile);
            if (removed) return;
        }
    }

    /**
     * Checks whether a projectile's center is inside this mob's body: the
     * sprite without its side padding, from the feet up to {@link #HURT_HEIGHT}.
     */
    protected boolean isHitBy(Projectile projectile) {
        double cx = projectile.getCenterX();
        double cy = projectile.getCenterY();
        return cx >= x + paddingLeft - HIT_RADIUS && cx <= x + mobSize - paddingRight + HIT_RADIUS
            && cy >= y - HIT_RADIUS && cy <= y + HURT_HEIGHT + HIT_RADIUS;
    }

    /**
     * Called by {@link #checkForHits()} when a projectile hits this mob. An
     * enemy loses {@link #ENEMY_HIT_DAMAGE} health and dies at zero, and the
     * player who fired the shot scores the kill.
     */
    protected void onHit(Projectile projectile) {
        health -= ENEMY_HIT_DAMAGE;
        if (health <= HEALTH_EPSILON) {
            remove();
            if (projectile.getFiredBy() instanceof PlayerMob player) player.scoredKill();
        }
    }

    /** Gets the health, from 0 to 1. */
    public double getHealth() {
        return health;
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    @Override
    public void remove() {
        if (removed) return;
        super.remove();
        level.add(new ParticleSpawner(x + mobSize / 2.0, y + HURT_HEIGHT / 2.0, 0, 10, 100, level));
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
