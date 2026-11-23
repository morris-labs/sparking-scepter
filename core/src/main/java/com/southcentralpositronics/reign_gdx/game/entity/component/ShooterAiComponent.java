package com.southcentralpositronics.reign_gdx.game.entity.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Pool;

/** Casts at the player whenever the player is in range. The mob also needs a {@link CasterComponent}. */
public final class ShooterAiComponent implements Component, Pool.Poolable {
    /** Reads this component from an entity. */
    public static final ComponentMapper<ShooterAiComponent> MAPPER = ComponentMapper.getFor(ShooterAiComponent.class);

    /** The farthest the player can be for the mob to cast, in pixels. */
    public double range;

    @Override
    public void reset() {
        range = 0;
    }
}
