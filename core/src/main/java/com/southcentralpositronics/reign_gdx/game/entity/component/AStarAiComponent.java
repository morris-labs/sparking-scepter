package com.southcentralpositronics.reign_gdx.game.entity.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Pool;
import com.southcentralpositronics.reign_gdx.engine.util.Vector2i;

import java.util.List;

/** Follows an A* path to the player, around tiles that block movement. */
public final class AStarAiComponent implements Component, Pool.Poolable {
    /** Reads this component from an entity. */
    public static final ComponentMapper<AStarAiComponent> MAPPER = ComponentMapper.getFor(AStarAiComponent.class);

    /** The walking speed along each axis, in pixels per second. */
    public double         speed;
    /** The seconds between path searches. */
    public float          refreshSeconds;
    /** The tiles left to visit, from the next step to the player; null before the first search. */
    public List<Vector2i> path;
    /** The seconds since the last path search. */
    public float          secondsSinceSearch;

    @Override
    public void reset() {
        speed              = 0;
        refreshSeconds     = 0f;
        path               = null;
        secondsSinceSearch = 0f;
    }
}
