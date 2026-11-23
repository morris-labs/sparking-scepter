package com.southcentralpositronics.reign_gdx.game.entity.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Pool;
import com.southcentralpositronics.reign_gdx.game.sim.EntityKind;

/**
 * Marks an entity as a spell: it flies in a straight line until it hits a tile
 * that stops spells, a mob on the other side, or the end of its range. Its
 * position is its center.
 */
public final class SpellComponent implements Component, Pool.Poolable {
    /** Reads this component from an entity. */
    public static final ComponentMapper<SpellComponent> MAPPER = ComponentMapper.getFor(SpellComponent.class);

    /** The kind of mob that cast the spell. The player's spells hit enemies, and enemies' spells hit the player. */
    public EntityKind casterKind;
    /** The x coordinate the spell was cast from. */
    public double     originX;
    /** The y coordinate the spell was cast from. */
    public double     originY;
    /** The x part of the unit vector the spell flies along. */
    public double     directionX;
    /** The y part of the unit vector the spell flies along. */
    public double     directionY;
    /** The flight speed, in pixels per second. */
    public double     speed;
    /** The farthest the spell flies, in pixels. */
    public double     range;
    /** The health the spell takes from the mob it hits. */
    public double     damage;

    /** Checks whether the player cast the spell. */
    public boolean isPlayers() {
        return casterKind == EntityKind.PLAYER;
    }

    @Override
    public void reset() {
        casterKind = null;
        originX    = 0;
        originY    = 0;
        directionX = 0;
        directionY = 0;
        speed      = 0;
        range      = 0;
        damage     = 0;
    }
}
