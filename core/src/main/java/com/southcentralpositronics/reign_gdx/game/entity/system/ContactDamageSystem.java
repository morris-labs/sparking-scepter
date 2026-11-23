package com.southcentralpositronics.reign_gdx.game.entity.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.southcentralpositronics.reign_gdx.engine.entity.component.BodyComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.HealthComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.PositionComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.ContactDamageComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.MobComponent;
import com.southcentralpositronics.reign_gdx.game.sim.EntityKind;
import com.southcentralpositronics.reign_gdx.game.sim.EventLog;
import com.southcentralpositronics.reign_gdx.game.sim.SimulationEvent;

/**
 * Hurts the player when an enemy with a {@link ContactDamageComponent} touches
 * it. Bodies count as touching when the gap between their collision boxes is
 * at most {@link #REACH} on both axes, because separation keeps them from
 * overlapping. Each hurting touch raises
 * {@link SimulationEvent.Type#PLAYER_DAMAGE}. A dead player takes no more
 * damage.
 */
public final class ContactDamageSystem extends PlayerAwareSystem {
    /** The widest gap between two collision boxes that still counts as touching, in pixels. */
    public static final double REACH = 2;

    private final EventLog events;

    /**
     * Creates the system.
     *
     * @param events The log that receives damage events.
     */
    public ContactDamageSystem(EventLog events) {
        super(Family.all(ContactDamageComponent.class, PositionComponent.class, BodyComponent.class).get(),
              GamePriority.CONTACT);
        this.events = events;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        ContactDamageComponent contact = ContactDamageComponent.MAPPER.get(entity);
        if (contact.cooldown > 0f) contact.cooldown -= deltaTime;
        if (contact.cooldown > 0f) return;

        Entity player = player();
        if (player == null) return;
        HealthComponent health = HealthComponent.MAPPER.get(player);
        if (health.isEmpty()) return;

        PositionComponent playerPosition = PositionComponent.MAPPER.get(player);
        BodyComponent     playerBody     = BodyComponent.MAPPER.get(player);
        if (!touching(PositionComponent.MAPPER.get(entity), BodyComponent.MAPPER.get(entity),
                      playerPosition, playerBody)) return;

        health.damage(contact.damage);
        events.raise(SimulationEvent.Type.PLAYER_DAMAGE, EntityKind.PLAYER, playerPosition.x,
                     MobComponent.bodyCenterY(playerPosition, playerBody));
        contact.cooldown = contact.delay;
    }

    private static boolean touching(PositionComponent a, BodyComponent aBody, PositionComponent b,
                                    BodyComponent bBody) {
        double gapX = Math.abs(a.x - b.x) - (aBody.width + bBody.width) / 2.0;
        double gapY = Math.abs(a.y - b.y) - (aBody.height + bBody.height) / 2.0;
        return gapX <= REACH && gapY <= REACH;
    }
}
