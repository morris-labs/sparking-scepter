package com.southcentralpositronics.reign_gdx.game.entity.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.southcentralpositronics.reign_gdx.engine.entity.component.BodyComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.PositionComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.CasterComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.MobComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.ShooterAiComponent;

/** Makes each shooting mob cast at the middle of the player's body while the player is in range. */
public final class ShooterAiSystem extends PlayerAwareSystem {
    public ShooterAiSystem() {
        super(Family.all(ShooterAiComponent.class, CasterComponent.class, PositionComponent.class).get(),
              GamePriority.AI);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        ShooterAiComponent shooter  = ShooterAiComponent.MAPPER.get(entity);
        CasterComponent    caster   = CasterComponent.MAPPER.get(entity);
        PositionComponent  position = PositionComponent.MAPPER.get(entity);
        caster.casting = false;

        Entity player = player();
        if (player == null) return;
        PositionComponent target = PositionComponent.MAPPER.get(player);
        if (Math.hypot(target.x - position.x, target.y - position.y) > shooter.range) return;

        caster.casting = true;
        caster.aimX    = target.x;
        caster.aimY    = MobComponent.bodyCenterY(target, BodyComponent.MAPPER.get(player));
    }
}
