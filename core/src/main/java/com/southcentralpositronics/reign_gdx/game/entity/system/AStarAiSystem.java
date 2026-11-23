package com.southcentralpositronics.reign_gdx.game.entity.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.southcentralpositronics.reign_gdx.engine.entity.component.BodyComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.PositionComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.VelocityComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.system.SystemPriority;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;
import com.southcentralpositronics.reign_gdx.engine.pathfinding.AStarPathfinder;
import com.southcentralpositronics.reign_gdx.engine.util.Vector2i;
import com.southcentralpositronics.reign_gdx.game.entity.component.AStarAiComponent;

/**
 * Steers each A* mob along a path to the player's tile. The path is searched
 * again every {@link AStarAiComponent#refreshSeconds}, and the mob heads for
 * the spot where it would stand on the path's next tile.
 */
public final class AStarAiSystem extends PlayerAwareSystem {
    private final AStarPathfinder pathfinder;

    /**
     * Creates the system.
     *
     * @param pathfinder The service that finds paths around tiles that block movement.
     */
    public AStarAiSystem(AStarPathfinder pathfinder) {
        super(Family.all(AStarAiComponent.class, PositionComponent.class, BodyComponent.class,
                         VelocityComponent.class).get(), SystemPriority.AI);
        this.pathfinder = pathfinder;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        AStarAiComponent  ai       = AStarAiComponent.MAPPER.get(entity);
        PositionComponent position = PositionComponent.MAPPER.get(entity);
        BodyComponent     body     = BodyComponent.MAPPER.get(entity);
        VelocityComponent velocity = VelocityComponent.MAPPER.get(entity);

        ai.secondsSinceSearch += deltaTime;
        if (ai.secondsSinceSearch >= ai.refreshSeconds) {
            Entity player = player();
            ai.path = player == null ? null
                : pathfinder.findPath(tileOf(position), tileOf(PositionComponent.MAPPER.get(player)));
            ai.secondsSinceSearch = 0f;
        }

        velocity.x = 0;
        velocity.y = 0;
        if (ai.path == null || ai.path.isEmpty()) return;

        // Head for the spot where a mob placed on the next tile stands.
        Vector2i next    = ai.path.getFirst();
        double   targetX = next.x * Tile.SIZE + body.width / 2.0;
        double   targetY = next.y * Tile.SIZE + body.height / 2.0;
        velocity.x = Math.signum(targetX - position.x) * ai.speed;
        velocity.y = Math.signum(targetY - position.y) * ai.speed;
    }

    /** Gets the tile under a position. */
    private static Vector2i tileOf(PositionComponent position) {
        return new Vector2i((int) Math.floor(position.x / Tile.SIZE), (int) Math.floor(position.y / Tile.SIZE));
    }
}
