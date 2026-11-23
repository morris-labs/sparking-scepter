package com.southcentralpositronics.reign_gdx.game.entity;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.southcentralpositronics.reign_gdx.engine.entity.component.PositionComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.SpellComponent;
import com.southcentralpositronics.reign_gdx.game.sim.EntityKind;

/** Builds spells from components and adds them to the engine. */
public final class SpellFactory {
    /** The flight speed of a spell, in pixels per second: 5 px per tick. */
    public static final double SPEED = 300;
    /** The farthest a spell flies, in pixels. */
    public static final double RANGE = 150;

    private final Engine engine;

    /**
     * Creates a factory.
     *
     * @param engine The engine to add spells to.
     */
    public SpellFactory(Engine engine) {
        this.engine = engine;
    }

    /**
     * Adds a fireball that flies {@link #RANGE} px at {@link #SPEED} px per
     * second.
     *
     * @param casterKind The kind of mob that cast it, which decides whom it hurts.
     * @param x          The x coordinate of the spell's center.
     * @param y          The y coordinate of the spell's center.
     * @param angle      The direction, in radians, counterclockwise from +x.
     * @param damage     The health it takes from the mob it hits.
     * @return The spell entity.
     */
    public Entity createSpell(EntityKind casterKind, double x, double y, double angle, double damage) {
        Entity entity = engine.createEntity();

        PositionComponent position = engine.createComponent(PositionComponent.class);
        position.x = x;
        position.y = y;

        SpellComponent spell = engine.createComponent(SpellComponent.class);
        spell.casterKind = casterKind;
        spell.originX    = x;
        spell.originY    = y;
        spell.directionX = Math.cos(angle);
        spell.directionY = Math.sin(angle);
        spell.speed      = SPEED;
        spell.range      = RANGE;
        spell.damage     = damage;

        entity.add(position);
        entity.add(spell);
        engine.addEntity(entity);
        return entity;
    }
}
