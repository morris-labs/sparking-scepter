package com.southcentralpositronics.reign_gdx.game.entity.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.southcentralpositronics.reign_gdx.engine.entity.component.HealthComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.VelocityComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.system.SystemPriority;
import com.southcentralpositronics.reign_gdx.engine.input.PlayerInput;
import com.southcentralpositronics.reign_gdx.game.entity.component.CasterComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.PlayerComponent;

/**
 * Turns the player's {@link PlayerInput} into a velocity and a casting intent.
 * Each move component scales the speed along its own axis, so diagonals
 * aren't normalized, as before. A dead player ignores its input.
 */
public final class PlayerInputSystem extends IteratingSystem {
    public PlayerInputSystem() {
        super(Family.all(PlayerComponent.class, VelocityComponent.class, CasterComponent.class,
                         HealthComponent.class).get(),
              SystemPriority.INPUT);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PlayerComponent   player   = PlayerComponent.MAPPER.get(entity);
        VelocityComponent velocity = VelocityComponent.MAPPER.get(entity);
        CasterComponent   caster   = CasterComponent.MAPPER.get(entity);
        // A dead player stands still and stops casting, whatever the input.
        PlayerInput       input    = HealthComponent.MAPPER.get(entity).isEmpty() ? PlayerInput.NONE : player.input;

        velocity.x     = input.moveX() * player.speed;
        velocity.y     = input.moveY() * player.speed;
        caster.casting = input.casting();
        caster.aimX    = input.aimX();
        caster.aimY    = input.aimY();
    }
}
