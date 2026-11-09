package com.southcentralpositronics.reign_gdx.entity.mob;

import com.southcentralpositronics.reign_gdx.Game;
import com.southcentralpositronics.reign_gdx.entity.projectile.Projectile;
import com.southcentralpositronics.reign_gdx.input.Keyboard;
import com.southcentralpositronics.reign_gdx.input.Mouse;


public class PlayerMob extends Mob {
    private Keyboard keyboard;
    private Mouse    mouse;
    private String   name;

    private final double  speed    = 2;
    private       boolean shooting = false;
    public        long    score    = 0;
    /** The health the player loses per enemy hit, as in the Java2D version. */
    private static final double PLAYER_HIT_DAMAGE = 0.01;
    protected     int     height;
    protected     int     width;

    public PlayerMob(Game game, int x, int y, String name) {
        this.x           = x;
        this.y           = y;
        keyboard         = game.getKeyboard();
        mouse            = game.getMouse();
        this.name        = name;
        this.health      = 1.0;
        this.type        = Type.PLAYER;
        walking          = false;
        direction        = Direction.DOWN;

        loadAnimations(game.getMobAtlas(), "King_Cherno");

        height = animSprite.getCurrentFrame().getRegionHeight();
        width  = animSprite.getCurrentFrame().getRegionWidth();

        viewport = game.getViewport();
    }


    @Override
    public void update(float delta) {
        double xa = 0, ya = 0;

        if (keyboard.up) {
            ya += speed;
            direction = Direction.UP;
        } else if (keyboard.down) {
            ya -= speed;
            direction = Direction.DOWN;
        }

        if (keyboard.left) {
            xa -= speed;
            direction = Direction.LEFT;
        } else if (keyboard.right) {
            xa += speed;
            direction = Direction.RIGHT;
        }

        boolean isMoving = xa != 0 || ya != 0;
        if (isMoving) {
            move(xa, ya);
        }
        if (shootCooldown > 0f) {
            shootCooldown -= delta;
        }
        updateAnimation(delta, isMoving, direction);
        if (mouse.isLeftPressed()) {
            firePlayerSpell();  // your existing method
        }

        clearProjectiles();
    }


    private void clearProjectiles() {
        level.getProjectiles().removeIf(Projectile::isRemoved);
    }

    /**
     * Called when this player kills an enemy. The score goes up by one kill,
     * shown as 100 points, and the player heals by half, up to full health.
     * The Java2D version didn't cap the healing.
     */
    public void scoredKill() {
        score++;
        health = Math.min(1.0, health + 0.5);
    }

    /**
     * Takes {@link #PLAYER_HIT_DAMAGE} health per enemy hit. As in the Java2D
     * version, health stops at zero and the player doesn't die.
     */
    @Override
    protected void onHit(Projectile projectile) {
        health = Math.max(0.0, health - PLAYER_HIT_DAMAGE);
    }

    /** Gets the number of kills; the HUD shows 100 points per kill. */
    public long getScore() {
        return score;
    }

    public String getName() {
        return name;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
