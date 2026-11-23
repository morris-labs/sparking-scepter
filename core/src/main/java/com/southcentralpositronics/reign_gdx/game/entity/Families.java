package com.southcentralpositronics.reign_gdx.game.entity;

import com.badlogic.ashley.core.Family;
import com.southcentralpositronics.reign_gdx.engine.entity.component.BodyComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.HealthComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.PositionComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.EnemyComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.MobComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.PlayerComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.SpellComponent;

/** The Ashley families that several of Reign's systems and the simulation query. */
public final class Families {
    /** Every mob: the player and the enemies. */
    public static final Family MOBS    = Family.all(MobComponent.class, PositionComponent.class, BodyComponent.class,
                                                    HealthComponent.class).get();
    /** The player. */
    public static final Family PLAYERS = Family.all(PlayerComponent.class, MobComponent.class,
                                                    PositionComponent.class, BodyComponent.class,
                                                    HealthComponent.class).get();
    /** Every enemy. */
    public static final Family ENEMIES = Family.all(EnemyComponent.class, MobComponent.class,
                                                    PositionComponent.class, BodyComponent.class,
                                                    HealthComponent.class).get();
    /** Every spell in flight. */
    public static final Family SPELLS  = Family.all(SpellComponent.class, PositionComponent.class).get();

    private Families() {
    }
}
