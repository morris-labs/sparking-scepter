package com.southcentralpositronics.reign_gdx.game.entity.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.southcentralpositronics.reign_gdx.engine.entity.component.BodyComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.PositionComponent;
import com.southcentralpositronics.reign_gdx.game.entity.SpellFactory;
import com.southcentralpositronics.reign_gdx.game.entity.component.CasterComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.MobComponent;
import com.southcentralpositronics.reign_gdx.game.sim.EventLog;
import com.southcentralpositronics.reign_gdx.game.sim.SimulationEvent;

/**
 * Counts down each caster's cooldown, and casts a spell from the middle of its
 * body toward its aim point when it wants to and the cooldown allows. Each
 * cast raises {@link SimulationEvent.Type#CAST}.
 */
public final class CastingSystem extends IteratingSystem {
    private final SpellFactory spells;
    private final EventLog     events;

    /**
     * The cooldown, in seconds, that counts as over. It absorbs float error, so
     * a delay of a whole number of ticks, such as 0.1 s, lasts exactly 6 ticks
     * instead of 7.
     */
    static final float COOLDOWN_EPSILON = 1e-5f;

    /**
     * Creates the system.
     *
     * @param spells The factory that adds each spell.
     * @param events The log that receives cast events.
     */
    public CastingSystem(SpellFactory spells, EventLog events) {
        super(Family.all(CasterComponent.class, MobComponent.class, PositionComponent.class, BodyComponent.class)
                  .get(), GamePriority.CASTING);
        this.spells = spells;
        this.events = events;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        CasterComponent caster = CasterComponent.MAPPER.get(entity);
        if (caster.cooldown > COOLDOWN_EPSILON) caster.cooldown -= deltaTime;
        if (!caster.casting || caster.cooldown > COOLDOWN_EPSILON) return;

        MobComponent      mob      = MobComponent.MAPPER.get(entity);
        PositionComponent position = PositionComponent.MAPPER.get(entity);
        double            x        = position.x;
        double            y        = MobComponent.bodyCenterY(position, BodyComponent.MAPPER.get(entity));

        spells.createSpell(mob.kind, x, y, Math.atan2(caster.aimY - y, caster.aimX - x), caster.spellDamage);
        events.raise(SimulationEvent.Type.CAST, mob.kind, x, y);
        caster.cooldown = caster.shotDelay;
    }
}
