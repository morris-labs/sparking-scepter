package com.southcentralpositronics.reign_gdx.game.entity.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.southcentralpositronics.reign_gdx.engine.entity.component.BodyComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.HealthComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.PositionComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.system.SystemPriority;
import com.southcentralpositronics.reign_gdx.engine.level.Level;
import com.southcentralpositronics.reign_gdx.engine.time.FixedTimestep;
import com.southcentralpositronics.reign_gdx.game.entity.Families;
import com.southcentralpositronics.reign_gdx.game.entity.component.MobComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.PlayerComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.SpellComponent;
import com.southcentralpositronics.reign_gdx.game.sim.EventLog;
import com.southcentralpositronics.reign_gdx.game.sim.SimulationEvent;

/**
 * Flies each spell one tick and ends it at a tile that stops spells, at the end
 * of its range, or on the first mob on the other side that it hits. A hit takes
 * the spell's damage from the mob and raises {@link SimulationEvent.Type#HIT}
 * for an enemy or {@link SimulationEvent.Type#PLAYER_DAMAGE} for the player.
 * Every spell that ends bursts into particles.
 */
public final class SpellSystem extends IteratingSystem {
    /** The width and height of the box that tile collision checks around a spell's center, in pixels. */
    private static final int SPELL_BOX       = 4;
    /** How far outside a mob's body a spell's center still counts as a hit, in pixels. */
    private static final int HIT_RADIUS      = 4;
    /** The base lifetime of a spell's particles, in ticks. */
    private static final int PARTICLE_TICKS  = 10;
    /** The particles a spell bursts into when it hits a mob. */
    private static final int HIT_PARTICLES   = 1000;
    /** The particles a spell bursts into when it hits a tile or runs out of range. */
    private static final int FIZZLE_PARTICLES = 250;

    private final Level            level;
    private final EventLog         events;
    private ImmutableArray<Entity> mobs;

    /**
     * Creates the system.
     *
     * @param level  The level whose tiles stop spells, and whose emitter bursts particles.
     * @param events The log that receives hit events.
     */
    public SpellSystem(Level level, EventLog events) {
        super(Families.SPELLS, SystemPriority.SPELLS);
        this.level  = level;
        this.events = events;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        mobs = engine.getEntitiesFor(Families.MOBS);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        SpellComponent    spell    = SpellComponent.MAPPER.get(entity);
        PositionComponent position = PositionComponent.MAPPER.get(entity);
        double            step     = FixedTimestep.perTick(spell.speed);
        double            dx       = spell.directionX * step;
        double            dy       = spell.directionY * step;

        double half = SPELL_BOX / 2.0;
        if (level.blocksSpells(position.x - half + dx, position.y - half + dy, SPELL_BOX, SPELL_BOX)) {
            end(entity, position, FIZZLE_PARTICLES);
            return;
        }

        position.x += dx;
        position.y += dy;
        if (Math.hypot(position.x - spell.originX, position.y - spell.originY) > spell.range) {
            end(entity, position, FIZZLE_PARTICLES);
            return;
        }

        for (Entity target : mobs) {
            boolean isPlayer = PlayerComponent.MAPPER.has(target);
            if (spell.isPlayers() == isPlayer) continue;
            HealthComponent health = HealthComponent.MAPPER.get(target);
            // A mob with no health left is dead or dies this tick; later spells fly through it.
            if (health.isEmpty()) continue;
            PositionComponent targetPosition = PositionComponent.MAPPER.get(target);
            BodyComponent     body           = BodyComponent.MAPPER.get(target);
            if (!hits(position, targetPosition, body)) continue;

            health.damage(spell.damage);
            events.raise(isPlayer ? SimulationEvent.Type.PLAYER_DAMAGE : SimulationEvent.Type.HIT,
                         MobComponent.MAPPER.get(target).kind, targetPosition.x,
                         MobComponent.bodyCenterY(targetPosition, body));
            end(entity, position, HIT_PARTICLES);
            return;
        }
    }

    /**
     * Checks whether a spell's center is inside a mob's body: the collision
     * box's width, from the feet up to {@link MobComponent#HURT_HEIGHT}, grown
     * by {@link #HIT_RADIUS} on every side.
     */
    private static boolean hits(PositionComponent spell, PositionComponent mob, BodyComponent body) {
        double left = body.left(mob);
        double feet = MobComponent.feetY(mob, body);
        return spell.x >= left - HIT_RADIUS && spell.x <= left + body.width + HIT_RADIUS
            && spell.y >= feet - HIT_RADIUS && spell.y <= feet + MobComponent.HURT_HEIGHT + HIT_RADIUS;
    }

    private void end(Entity spell, PositionComponent position, int particles) {
        level.getParticleEmitter().burst(position.x, position.y, PARTICLE_TICKS, particles);
        getEngine().removeEntity(spell);
    }
}
