package com.southcentralpositronics.reign_gdx.game.entity.mob;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import java.util.List;

/** An enemy that walks straight at the player, ignoring walls. */
public class ChaserMob extends Mob {
    private       double xa    = 0;
    private       double ya    = 0;
    private final double speed = 0.8;

    public ChaserMob(int x, int y, TextureAtlas atlas) {
        loadAnimations(atlas, ENEMY_SPRITE);
        placeOnTile(x, y);
        this.type = Type.ENEMY;
    }

    private void chasePlayer() {
        xa = 0;
        ya = 0;

        List<PlayerMob> players = gameLevel.getPlayersInRadius(this, 800);
        if (!players.isEmpty()) {
            PlayerMob target = players.getFirst();

            if (x < target.getX()) xa += speed;
            if (x > target.getX()) xa -= speed;
            if (y < target.getY()) ya += speed;
            if (y > target.getY()) ya -= speed;
        }

        if (xa != 0 || ya != 0) {
            move(xa, ya);
            walking = true;
        } else {
            walking = false;
        }
    }

    @Override
    public void update(float delta) {
        chasePlayer();

        // Determine direction
        if (Math.abs(ya) > Math.abs(xa)) {
            direction = ya < 0 ? Direction.UP : Direction.DOWN;
        } else if (xa != 0) {
            direction = xa < 0 ? Direction.LEFT : Direction.RIGHT;
        }

        super.updateAnimation(delta, walking, direction);
    }
}
