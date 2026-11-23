package com.southcentralpositronics.reign_gdx.game.entity.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.southcentralpositronics.reign_gdx.engine.entity.component.BodyComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.PositionComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.VelocityComponent;
import com.southcentralpositronics.reign_gdx.engine.level.Level;
import com.southcentralpositronics.reign_gdx.game.entity.component.WanderAiComponent;

import java.util.Random;

/**
 * Moves each wandering mob in its current direction, turns it at random
 * intervals, and turns it back from a tile that blocks movement 2 px ahead.
 * Every random choice comes from the level's random source, so the seed
 * decides how mobs wander.
 */
public final class WanderAiSystem extends IteratingSystem {
    /** How far ahead a wandering mob looks for a blocking tile, in pixels. */
    private static final int LOOK_AHEAD = 2;

    private final Level level;

    /**
     * Creates the system.
     *
     * @param level The level whose tiles turn mobs back and whose random source picks turns.
     */
    public WanderAiSystem(Level level) {
        super(Family.all(WanderAiComponent.class, PositionComponent.class, BodyComponent.class,
                         VelocityComponent.class).get(), GamePriority.AI);
        this.level = level;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        WanderAiComponent wander   = WanderAiComponent.MAPPER.get(entity);
        PositionComponent position = PositionComponent.MAPPER.get(entity);
        BodyComponent     body     = BodyComponent.MAPPER.get(entity);
        VelocityComponent velocity = VelocityComponent.MAPPER.get(entity);
        Random            random   = level.getRandom();

        wander.secondsSinceTurn += deltaTime;
        if (wander.secondsSinceTurn >= random.nextFloat(wander.minTurnSeconds, wander.maxTurnSeconds)) {
            wander.secondsSinceTurn = 0f;
            if (random.nextBoolean()) {
                wander.directionY = random.nextInt(3) - 1;
                wander.directionX = 0;
            } else {
                wander.directionX = random.nextInt(3) - 1;
                wander.directionY = 0;
            }
        }

        double left   = body.left(position);
        double bottom = body.bottom(position);
        if (wander.directionX != 0 || wander.directionY != 0) {
            if (level.blocksMovement(left, bottom + wander.directionY * LOOK_AHEAD, body.width, body.height)) {
                wander.directionY *= -1;
            }
            if (level.blocksMovement(left + wander.directionX * LOOK_AHEAD, bottom, body.width, body.height)) {
                wander.directionX *= -1;
            }
        }
        velocity.x = wander.directionX * wander.speed;
        velocity.y = wander.directionY * wander.speed;
    }
}
