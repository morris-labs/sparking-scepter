package com.southcentralpositronics.reign_gdx.game.entity.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Pool;

/**
 * Marks a mob as an enemy: it counts toward the wave, the player's spells hurt
 * it, and killing it adds its {@link #score} to the player's score.
 */
public final class EnemyComponent implements Component, Pool.Poolable {
    /** Reads this component from an entity. */
    public static final ComponentMapper<EnemyComponent> MAPPER = ComponentMapper.getFor(EnemyComponent.class);

    /** The points that killing this enemy adds to the score. */
    public long score;

    @Override
    public void reset() {
        score = 0;
    }
}
