package com.southcentralpositronics.reign_gdx.game.entity.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.southcentralpositronics.reign_gdx.engine.entity.component.VelocityComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.system.SystemPriority;
import com.southcentralpositronics.reign_gdx.game.entity.component.MobComponent;
import com.southcentralpositronics.reign_gdx.game.sim.EntityKind;
import com.southcentralpositronics.reign_gdx.game.sim.Facing;

/**
 * Sets each mob's facing and walking animation from the velocity it asked for
 * this tick, whether or not a wall stopped it. A mob that stands still keeps
 * its facing and restarts its animation.
 *
 * <p>Known issue 13 is kept on purpose: in libGDX's y-up world, moving up
 * faces {@link Facing#DOWN} and moving down faces {@link Facing#UP}. The player
 * faces up or down whenever it moves along y; an enemy faces along the axis it
 * moves faster on, and sideways on a tie.
 */
public final class MobAnimationSystem extends IteratingSystem {
    public MobAnimationSystem() {
        super(Family.all(MobComponent.class, VelocityComponent.class).get(), SystemPriority.ANIMATION);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        MobComponent      mob      = MobComponent.MAPPER.get(entity);
        VelocityComponent velocity = VelocityComponent.MAPPER.get(entity);

        mob.walking = velocity.isMoving();
        if (mob.walking) mob.facing = mob.kind == EntityKind.PLAYER ? playerFacing(mob.facing, velocity)
            : enemyFacing(mob.facing, velocity);
        mob.animationTime = mob.walking ? mob.animationTime + deltaTime : 0f;
    }

    private static Facing playerFacing(Facing current, VelocityComponent velocity) {
        Facing facing = current;
        if (velocity.x > 0) facing = Facing.RIGHT;
        else if (velocity.x < 0) facing = Facing.LEFT;
        if (velocity.y > 0) facing = Facing.DOWN;
        else if (velocity.y < 0) facing = Facing.UP;
        return facing;
    }

    private static Facing enemyFacing(Facing current, VelocityComponent velocity) {
        if (Math.abs(velocity.y) > Math.abs(velocity.x)) return velocity.y < 0 ? Facing.UP : Facing.DOWN;
        if (velocity.x != 0) return velocity.x < 0 ? Facing.LEFT : Facing.RIGHT;
        return current;
    }
}
