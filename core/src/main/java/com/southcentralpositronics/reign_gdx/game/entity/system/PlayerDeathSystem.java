package com.southcentralpositronics.reign_gdx.game.entity.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.systems.IteratingSystem;
import com.southcentralpositronics.reign_gdx.engine.entity.component.BodyComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.HealthComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.PositionComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.system.SystemPriority;
import com.southcentralpositronics.reign_gdx.engine.level.Level;
import com.southcentralpositronics.reign_gdx.game.entity.Families;
import com.southcentralpositronics.reign_gdx.game.entity.component.MobComponent;
import com.southcentralpositronics.reign_gdx.game.sim.EntityKind;
import com.southcentralpositronics.reign_gdx.game.sim.EventLog;
import com.southcentralpositronics.reign_gdx.game.sim.RunState;
import com.southcentralpositronics.reign_gdx.game.sim.SimulationEvent;

/**
 * Ends the run when the player has no health left. The player bursts into
 * particles and {@link SimulationEvent.Type#PLAYER_DEATH} fires once. The
 * player's entity stays in the level, so the play screen can still draw it.
 *
 * <p>The simulation adds this system before {@link DeathSystem}, at the same
 * priority, so a kill on the tick the player dies can't heal the player back.
 */
public final class PlayerDeathSystem extends IteratingSystem {
    /** The base lifetime of the death's particles, in ticks. */
    private static final int PARTICLE_TICKS  = 10;
    /** The particles the player bursts into when it dies. */
    private static final int DEATH_PARTICLES = 300;

    private final Level    level;
    private final EventLog events;
    private final RunState run;

    /**
     * Creates the system.
     *
     * @param level  The level whose emitter bursts particles.
     * @param events The log that receives the death event.
     * @param run    The run that the death ends.
     */
    public PlayerDeathSystem(Level level, EventLog events, RunState run) {
        super(Families.PLAYERS, SystemPriority.DEATH);
        this.level  = level;
        this.events = events;
        this.run    = run;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (run.isOver() || !HealthComponent.MAPPER.get(entity).isEmpty()) return;

        PositionComponent position = PositionComponent.MAPPER.get(entity);
        double            y        = MobComponent.bodyCenterY(position, BodyComponent.MAPPER.get(entity));
        level.getParticleEmitter().burst(position.x, y, PARTICLE_TICKS, DEATH_PARTICLES);
        events.raise(SimulationEvent.Type.PLAYER_DEATH, EntityKind.PLAYER, position.x, y);
        run.end();
    }
}
