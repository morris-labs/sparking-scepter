package com.southcentralpositronics.reign_gdx.game.entity.mob;

import com.southcentralpositronics.reign_gdx.engine.level.Level;
import com.southcentralpositronics.reign_gdx.engine.time.FixedTimestep;
import com.southcentralpositronics.reign_gdx.game.sim.EntityKind;

import java.util.Random;

/** An enemy that wanders and never attacks. Nothing spawns it by default. */
public class DummyMob extends Mob {
    /** The wandering speed, in pixels per second. */
    public static final double SPEED = 60;

    private int   xa;
    private int   ya;
    private float directionChangeTimer = 0;

    public DummyMob(int x, int y) {
        placeOnTile(x, y);
        this.type = Type.ENEMY;
    }

    @Override
    public EntityKind kind() {
        return EntityKind.DUMMY;
    }

    @Override
    public void init(Level level) {
        super.init(level);
        Random random = gameLevel.getRandom();
        xa = random.nextInt(3) - 1;
        ya = random.nextInt(3) - 1;
    }

    /** Picks a new direction every 0.5 to 1 seconds. */
    private void updateRandomDirection(float delta) {
        Random random = gameLevel.getRandom();
        directionChangeTimer += delta;

        if (directionChangeTimer >= random.nextFloat(0.5f, 1f)) {
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
        updateRandomDirection(delta);

        if (xa != 0 || ya != 0) {
            if (collision(x, y + ya * 2)) ya *= -1;
            if (collision(x + xa * 2, y)) xa *= -1;
            double step = FixedTimestep.perTick(SPEED);
            move(xa * step, ya * step);
            walking = true;
        } else {
            walking = false;
        }

        faceMovement(xa, ya);
        updateAnimation(delta);
    }
}
