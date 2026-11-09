package com.southcentralpositronics.reign_gdx.entity.mob;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import com.southcentralpositronics.reign_gdx.entity.projectile.Projectile;

import java.util.List;

public class ShooterMob extends Mob {
    private int   xa                   = random.nextInt(3) - 1;
    private int   ya                   = random.nextInt(3) - 1;
    private float directionChangeTimer = 0;

    public ShooterMob(int x, int y, TextureAtlas atlas) {
        loadAnimations(atlas, ENEMY_SPRITE);
        placeOnTile(x, y);
        this.type       = Type.ENEMY;
        this.health     = 1.0;
    }

    @Override
    public void update(float delta) {
        directionChangeTimer += delta;
        if (shootCooldown > 0f) shootCooldown -= delta;

        shootClosest();

        // Update direction every 0.5–1.5 seconds
        if (directionChangeTimer >= random.nextFloat(0.5f, 1.5f)) {
            directionChangeTimer = 0;
            if (random.nextBoolean()) {
                ya = random.nextInt(3) - 1;
                xa = 0;
            } else {
                xa = random.nextInt(3) - 1;
                ya = 0;
            }
        }

        if (xa != 0 || ya != 0) {
            if (collision(x, y + ya * 2)) ya *= -1;
            if (collision(x + xa * 2, y)) xa *= -1;
            move(xa, ya);
            walking = true;
        } else {
            walking = false;
        }

        // Determine facing direction
        if (Math.abs(ya) > Math.abs(xa)) {
            direction = ya < 0 ? Direction.UP : Direction.DOWN;
        } else if (xa != 0) {
            direction = xa < 0 ? Direction.LEFT : Direction.RIGHT;
        }

        super.updateAnimation(delta, walking, direction);
    }

//    private void shootClosest() {
//        if (shootCooldown < 1.5f) return;
//
//        List<PlayerMob> players = level.getPlayersInRadius(this, 300);
//        if (!players.isEmpty()) {
//            PlayerMob target = players.getFirst();
//            double dx = target.getX() - this.x;
//            double dy = target.getY() - this.y;
//            double angle = Math.atan2(dy, dx);
//            shoot(x + 8, y + 8, angle); // Fire from center
//            shootCooldown = 0;
//        }
//    }

}
