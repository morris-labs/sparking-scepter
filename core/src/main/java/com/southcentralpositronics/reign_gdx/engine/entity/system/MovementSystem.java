package com.southcentralpositronics.reign_gdx.engine.entity.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.southcentralpositronics.reign_gdx.engine.entity.component.BodyComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.PositionComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.VelocityComponent;
import com.southcentralpositronics.reign_gdx.engine.level.Level;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;
import com.southcentralpositronics.reign_gdx.engine.time.FixedTimestep;

/** Moves every entity with a body by its velocity for one tick, stopping at tiles that block movement. */
public final class MovementSystem extends IteratingSystem {
    private final Level level;

    /**
     * Creates the system.
     *
     * @param level The level whose tiles block movement.
     */
    public MovementSystem(Level level) {
        super(Family.all(PositionComponent.class, VelocityComponent.class, BodyComponent.class).get(),
              SystemPriority.MOVEMENT);
        this.level = level;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        VelocityComponent velocity = VelocityComponent.MAPPER.get(entity);
        if (!velocity.isMoving()) return;
        moveBox(level, PositionComponent.MAPPER.get(entity), BodyComponent.MAPPER.get(entity),
                FixedTimestep.perTick(velocity.x), FixedTimestep.perTick(velocity.y));
    }

    /**
     * Moves a body along x, then along y, one pixel at a time, so it stops
     * flush against a tile that blocks movement. The body then stays inside
     * the map.
     *
     * <p>Any system that moves a body, such as mob separation, must move it
     * through this method so that it never ends up inside a solid tile.
     *
     * @param level    The level whose tiles block movement.
     * @param position The position to change.
     * @param body     The collision box around the position.
     * @param dx       The distance to move along x, in pixels.
     * @param dy       The distance to move along y, in pixels.
     */
    public static void moveBox(Level level, PositionComponent position, BodyComponent body, double dx, double dy) {
        for (double remaining = dx; remaining != 0; ) {
            double step = Math.max(-1, Math.min(1, remaining));
            if (level.blocksMovement(body.left(position) + step, body.bottom(position), body.width, body.height)) break;
            position.x += step;
            remaining -= step;
        }

        for (double remaining = dy; remaining != 0; ) {
            double step = Math.max(-1, Math.min(1, remaining));
            if (level.blocksMovement(body.left(position), body.bottom(position) + step, body.width, body.height)) break;
            position.y += step;
            remaining -= step;
        }

        double halfWidth  = body.width / 2.0;
        double halfHeight = body.height / 2.0;
        position.x = MathUtils.clamp(position.x, halfWidth, level.getWidth() * Tile.SIZE - halfWidth);
        position.y = MathUtils.clamp(position.y, halfHeight, level.getHeight() * Tile.SIZE - halfHeight);
    }
}
