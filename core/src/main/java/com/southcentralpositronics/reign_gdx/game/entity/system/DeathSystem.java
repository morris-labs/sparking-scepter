package com.southcentralpositronics.reign_gdx.game.entity.system;

import com.badlogic.ashley.core.Entity;
import com.southcentralpositronics.reign_gdx.engine.entity.component.BodyComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.HealthComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.PositionComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.system.SystemPriority;
import com.southcentralpositronics.reign_gdx.engine.level.Level;
import com.southcentralpositronics.reign_gdx.game.entity.Families;
import com.southcentralpositronics.reign_gdx.game.entity.component.MobComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.PlayerComponent;
import com.southcentralpositronics.reign_gdx.game.sim.EventLog;
import com.southcentralpositronics.reign_gdx.game.sim.SimulationEvent;

/**
 * Removes each enemy with no health left. It bursts into particles, raises
 * {@link SimulationEvent.Type#ENEMY_DEATH}, and the player scores the kill and
 * heals by {@link #KILL_HEAL}, up to full health. Only the player's spells hurt
 * enemies, so the player always made the kill. The player doesn't die yet.
 */
public final class DeathSystem extends PlayerAwareSystem {
    /** The health the player regains per kill. The Java2D version didn't cap the healing. */
    public static final double KILL_HEAL = 0.5;

    /** The base lifetime of a death's particles, in ticks. */
    private static final int PARTICLE_TICKS  = 10;
    /** The particles an enemy bursts into when it dies. */
    private static final int DEATH_PARTICLES = 100;

    private final Level    level;
    private final EventLog events;

    /**
     * Creates the system.
     *
     * @param level  The level whose emitter bursts particles.
     * @param events The log that receives death events.
     */
    public DeathSystem(Level level, EventLog events) {
        super(Families.ENEMIES, SystemPriority.DEATH);
        this.level  = level;
        this.events = events;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (!HealthComponent.MAPPER.get(entity).isEmpty()) return;

        PositionComponent position = PositionComponent.MAPPER.get(entity);
        double            x        = position.x;
        double            y        = MobComponent.bodyCenterY(position, BodyComponent.MAPPER.get(entity));
        level.getParticleEmitter().burst(x, y, PARTICLE_TICKS, DEATH_PARTICLES);
        events.raise(SimulationEvent.Type.ENEMY_DEATH, MobComponent.MAPPER.get(entity).kind, x, y);
        getEngine().removeEntity(entity);

        for (Entity player : players()) {
            PlayerComponent.MAPPER.get(player).kills++;
            HealthComponent.MAPPER.get(player).heal(KILL_HEAL);
        }
    }
}
