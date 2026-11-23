package com.southcentralpositronics.reign_gdx.game.entity.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Pool;
import com.southcentralpositronics.reign_gdx.engine.entity.component.BodyComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.PositionComponent;
import com.southcentralpositronics.reign_gdx.game.sim.EntityKind;
import com.southcentralpositronics.reign_gdx.game.sim.Facing;

/**
 * Marks an entity as a mob: the player or an enemy. It holds the mob's kind
 * and the state its walking animation needs.
 *
 * <p>A mob's {@link PositionComponent} is the center of its
 * {@link BodyComponent}, the 16x8 px collision box at its feet. Spells hit a
 * taller box: the body's width, from the feet up {@link #HURT_HEIGHT} pixels.
 */
public final class MobComponent implements Component, Pool.Poolable {
    /** Reads this component from an entity. */
    public static final ComponentMapper<MobComponent> MAPPER = ComponentMapper.getFor(MobComponent.class);

    /** The height of the body that spells can hit, from the feet up, in pixels. */
    public static final int HURT_HEIGHT = 24;

    /** The kind of mob, which the renderer and the score use. */
    public EntityKind kind;
    /** The direction the mob faces, which picks its walking animation. */
    public Facing     facing = Facing.DOWN;
    /** Whether the mob asked to move this tick. */
    public boolean    walking;
    /** The seconds this mob has been walking; 0 while it stands still. */
    public float      animationTime;

    /** Gets the y coordinate of the mob's feet. */
    public static double feetY(PositionComponent position, BodyComponent body) {
        return body.bottom(position);
    }

    /** Gets the y coordinate of the center of the body that spells hit, where the mob casts from. */
    public static double bodyCenterY(PositionComponent position, BodyComponent body) {
        return feetY(position, body) + HURT_HEIGHT / 2.0;
    }

    @Override
    public void reset() {
        kind          = null;
        facing        = Facing.DOWN;
        walking       = false;
        animationTime = 0f;
    }
}
