package com.southcentralpositronics.reign_gdx.game.entity.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Pool;
import com.southcentralpositronics.reign_gdx.engine.input.PlayerInput;

/** Marks the mob that the person playing controls, with the input it follows, its kills, and its score. */
public final class PlayerComponent implements Component, Pool.Poolable {
    /** Reads this component from an entity. */
    public static final ComponentMapper<PlayerComponent> MAPPER = ComponentMapper.getFor(PlayerComponent.class);

    /** The input the player follows on every tick until it changes. */
    public PlayerInput input = PlayerInput.NONE;
    /** The walking speed along each axis, in pixels per second. */
    public double      speed;
    /** The number of enemies killed. */
    public long        kills;
    /** The points earned this run: the sum of the score values of the enemies killed. */
    public long        score;

    @Override
    public void reset() {
        input = PlayerInput.NONE;
        speed = 0;
        kills = 0;
        score = 0;
    }
}
