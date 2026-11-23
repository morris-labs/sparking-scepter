package com.southcentralpositronics.reign_gdx.engine.entity.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.southcentralpositronics.reign_gdx.engine.entity.component.BodyComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.PositionComponent;
import com.southcentralpositronics.reign_gdx.engine.level.Level;

/**
 * Pushes overlapping bodies apart once per tick, so mobs don't stack.
 *
 * <p>For each overlapping pair, the system pushes along the axis where the
 * boxes overlap least, half the overlap each way. When a tile stops one body,
 * the other takes the rest of the push. When tiles stop both, the system tries
 * the other axis. Every push goes through {@link MovementSystem#moveBox}, so a
 * body never enters a tile that blocks movement.
 *
 * <p>Two bodies at exactly the same point push apart along y, the axis a 16x8
 * mob box overlaps least on, with the body that joined the family first going
 * down.
 */
public final class SeparationSystem extends EntitySystem {
    /** Overlaps smaller than this count as touching, not overlapping, in pixels. */
    private static final double EPSILON = 1e-6;

    private final Level                  level;
    private final Family                 family;
    private       ImmutableArray<Entity> bodies;

    /**
     * Creates the system.
     *
     * @param level  The level whose tiles block movement.
     * @param family The entities to keep apart. Each must have a
     *               {@link PositionComponent} and a {@link BodyComponent}.
     */
    public SeparationSystem(Level level, Family family) {
        super(SystemPriority.SEPARATION);
        this.level  = level;
        this.family = family;
    }

    @Override
    public void addedToEngine(Engine engine) {
        bodies = engine.getEntitiesFor(family);
    }

    @Override
    public void removedFromEngine(Engine engine) {
        bodies = null;
    }

    @Override
    public void update(float deltaTime) {
        for (int i = 0; i < bodies.size(); i++) {
            for (int j = i + 1; j < bodies.size(); j++) separate(bodies.get(i), bodies.get(j));
        }
    }

    private void separate(Entity first, Entity second) {
        PositionComponent a     = PositionComponent.MAPPER.get(first);
        PositionComponent b     = PositionComponent.MAPPER.get(second);
        BodyComponent     aBody = BodyComponent.MAPPER.get(first);
        BodyComponent     bBody = BodyComponent.MAPPER.get(second);

        double overlapX = overlapX(a, aBody, b, bBody);
        double overlapY = overlapY(a, aBody, b, bBody);
        if (overlapX <= EPSILON || overlapY <= EPSILON) return;

        boolean alongX = overlapX < overlapY;
        if (push(a, aBody, b, bBody, alongX, alongX ? overlapX : overlapY)) return;
        push(a, aBody, b, bBody, !alongX, alongX ? overlapY(a, aBody, b, bBody) : overlapX(a, aBody, b, bBody));
    }

    /**
     * Pushes two bodies apart along one axis by up to their overlap.
     *
     * @return Whether the bodies no longer overlap along that axis.
     */
    private boolean push(PositionComponent a, BodyComponent aBody, PositionComponent b, BodyComponent bBody,
                         boolean alongX, double overlap) {
        double gap       = alongX ? b.x - a.x : b.y - a.y;
        double direction = gap < 0 ? -1 : 1;

        double aMoved = move(a, aBody, alongX, -direction * overlap / 2);
        double bMoved = move(b, bBody, alongX, direction * (overlap - aMoved));
        if (bMoved < overlap - aMoved) aMoved += move(a, aBody, alongX, -direction * (overlap - aMoved - bMoved));
        return aMoved + bMoved >= overlap - EPSILON;
    }

    /** Moves a body along one axis and returns how far it went. */
    private double move(PositionComponent position, BodyComponent body, boolean alongX, double distance) {
        double before = alongX ? position.x : position.y;
        MovementSystem.moveBox(level, position, body, alongX ? distance : 0, alongX ? 0 : distance);
        return Math.abs((alongX ? position.x : position.y) - before);
    }

    private static double overlapX(PositionComponent a, BodyComponent aBody, PositionComponent b, BodyComponent bBody) {
        return Math.min(aBody.left(a) + aBody.width, bBody.left(b) + bBody.width)
            - Math.max(aBody.left(a), bBody.left(b));
    }

    private static double overlapY(PositionComponent a, BodyComponent aBody, PositionComponent b, BodyComponent bBody) {
        return Math.min(aBody.bottom(a) + aBody.height, bBody.bottom(b) + bBody.height)
            - Math.max(aBody.bottom(a), bBody.bottom(b));
    }
}
