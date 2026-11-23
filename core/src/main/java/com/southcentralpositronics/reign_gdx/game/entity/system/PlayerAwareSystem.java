package com.southcentralpositronics.reign_gdx.game.entity.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.southcentralpositronics.reign_gdx.game.entity.Families;

/** An iterating system whose rules look at the player, such as an enemy AI. */
abstract class PlayerAwareSystem extends IteratingSystem {
    private ImmutableArray<Entity> players;

    PlayerAwareSystem(Family family, int priority) {
        super(family, priority);
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        players = engine.getEntitiesFor(Families.PLAYERS);
    }

    /** Gets the player, or null when the level has none. */
    protected Entity player() {
        return players == null || players.size() == 0 ? null : players.first();
    }

    /** Gets every player; the level has at most one today. */
    protected ImmutableArray<Entity> players() {
        return players;
    }
}
