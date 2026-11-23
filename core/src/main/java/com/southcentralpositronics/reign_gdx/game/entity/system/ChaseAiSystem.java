package com.southcentralpositronics.reign_gdx.game.entity.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.southcentralpositronics.reign_gdx.engine.entity.component.PositionComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.VelocityComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.ChaseAiComponent;

/**
 * Points each chasing mob straight at the player in range, at full speed along
 * each axis that differs. Walls don't stop the decision, only the movement.
 */
public final class ChaseAiSystem extends PlayerAwareSystem {
    public ChaseAiSystem() {
        super(Family.all(ChaseAiComponent.class, PositionComponent.class, VelocityComponent.class).get(),
              GamePriority.AI);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        ChaseAiComponent  chase    = ChaseAiComponent.MAPPER.get(entity);
        PositionComponent position = PositionComponent.MAPPER.get(entity);
        VelocityComponent velocity = VelocityComponent.MAPPER.get(entity);
        velocity.x = 0;
        velocity.y = 0;

        Entity player = player();
        if (player == null) return;
        PositionComponent target = PositionComponent.MAPPER.get(player);
        if (Math.hypot(target.x - position.x, target.y - position.y) > chase.range) return;

        velocity.x = Math.signum(target.x - position.x) * chase.speed;
        velocity.y = Math.signum(target.y - position.y) * chase.speed;
    }
}
