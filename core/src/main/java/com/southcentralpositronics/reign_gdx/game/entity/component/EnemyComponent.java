package com.southcentralpositronics.reign_gdx.game.entity.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Pool;

/** Marks a mob as an enemy: it counts toward the wave, and the player's spells hurt it. */
public final class EnemyComponent implements Component, Pool.Poolable {
    /** Reads this component from an entity. */
    public static final ComponentMapper<EnemyComponent> MAPPER = ComponentMapper.getFor(EnemyComponent.class);

    @Override
    public void reset() {
    }
}
