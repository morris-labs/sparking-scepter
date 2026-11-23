package com.southcentralpositronics.reign_gdx.game.entity.mob;

import com.southcentralpositronics.reign_gdx.engine.time.FixedTimestep;
import com.southcentralpositronics.reign_gdx.game.sim.EntityKind;

import java.util.List;

/** An enemy that walks straight at the player, ignoring walls. */
public class ChaserMob extends Mob {
    /**
     * The walking speed along each axis, in pixels per second. The frame-based
     * version asked for 0.8 px per frame, but its pixel stepping rounded that up
     * to a whole pixel, so the chaser always moved 60 px per second.
     */
    public static final double SPEED = 60;

    private double xa = 0;
    private double ya = 0;

    public ChaserMob(int x, int y) {
        placeOnTile(x, y);
        this.type = Type.ENEMY;
    }

    @Override
    public EntityKind kind() {
        return EntityKind.CHASER;
    }

    private void chasePlayer() {
        double step = FixedTimestep.perTick(SPEED);
        xa = 0;
        ya = 0;

        List<PlayerMob> players = gameLevel.getPlayersInRadius(this, 800);
        if (!players.isEmpty()) {
            PlayerMob target = players.getFirst();

            if (x < target.getX()) xa += step;
            if (x > target.getX()) xa -= step;
            if (y < target.getY()) ya += step;
            if (y > target.getY()) ya -= step;
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
        faceMovement(xa, ya);
        updateAnimation(delta);
    }
}
