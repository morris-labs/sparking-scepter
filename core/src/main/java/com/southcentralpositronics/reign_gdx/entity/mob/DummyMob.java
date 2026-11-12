package com.southcentralpositronics.reign_gdx.entity.mob;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/** An enemy that wanders and never attacks. Nothing spawns it by default. */
public class DummyMob extends Mob {
    private int   xa                   = random.nextInt(3) - 1;
    private int   ya                   = random.nextInt(3) - 1;
    private float directionChangeTimer = 0;

    public DummyMob(int x, int y, TextureAtlas atlas) {
        loadAnimations(atlas, ENEMY_SPRITE);
        placeOnTile(x, y);
        this.type = Type.ENEMY;
    }

    private void updateRandomDirection() {
        directionChangeTimer += 1;

        if (directionChangeTimer >= random.nextInt(30, 60)) {
            directionChangeTimer = 0;

            if (random.nextBoolean()) {
                ya = random.nextInt(3) - 1;
                xa = 0;
            } else {
                xa = random.nextInt(3) - 1;
                ya = 0;
            }
        }
    }

    @Override
    public void update(float delta) {
        updateRandomDirection();

        if (xa != 0 || ya != 0) {
            if (collision(x, y + ya * 2)) ya *= -1;
            if (collision(x + xa * 2, y)) xa *= -1;
            move(xa, ya);
            walking = true;
        } else {
            walking = false;
        }

        // Determine direction
        if (Math.abs(ya) > Math.abs(xa)) {
            direction = ya < 0 ? Direction.UP : Direction.DOWN;
        } else if (xa != 0) {
            direction = xa < 0 ? Direction.LEFT : Direction.RIGHT;
        }

        super.updateAnimation(delta, walking, direction);
    }
}
