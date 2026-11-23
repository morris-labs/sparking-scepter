package com.southcentralpositronics.reign_gdx.engine.entity.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Pool;

/**
 * The collision box that tile collision uses, centered on the entity's
 * {@link PositionComponent}. A mob's box is the 16x8 px box at its feet, so a
 * mob fits in a corridor one tile wide.
 *
 * <p>Mob separation reads the same box: two mobs overlap when their boxes do.
 */
public final class BodyComponent implements Component, Pool.Poolable {
    /** Reads this component from an entity. */
    public static final ComponentMapper<BodyComponent> MAPPER = ComponentMapper.getFor(BodyComponent.class);

    /** The box's width, in pixels. */
    public int width;
    /** The box's height, in pixels. */
    public int height;

    /** Gets the x coordinate of the box's left edge for an entity at a position. */
    public double left(PositionComponent position) {
        return position.x - width / 2.0;
    }

    /** Gets the y coordinate of the box's bottom edge for an entity at a position. */
    public double bottom(PositionComponent position) {
        return position.y - height / 2.0;
    }

    @Override
    public void reset() {
        width  = 0;
        height = 0;
    }
}
