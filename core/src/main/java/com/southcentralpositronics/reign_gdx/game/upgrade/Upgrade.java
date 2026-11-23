package com.southcentralpositronics.reign_gdx.game.upgrade;

import com.badlogic.ashley.core.Entity;
import com.southcentralpositronics.reign_gdx.engine.entity.component.HealthComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.CasterComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.PlayerComponent;

/**
 * The upgrade table: every upgrade a break can offer, and what each one does
 * to the player. Tune the effects here; no system needs to change.
 *
 * <p>Each upgrade can be taken more than once in a run, and the effects stack.
 * Upgrades last for the current run only, because each run builds a new
 * player.
 */
public enum Upgrade {
    /** Casts more often: each pick multiplies the seconds between casts by {@link #CASTING_DELAY_FACTOR}. */
    FASTER_CASTING("Faster casting", "Cast 25% more often") {
        @Override
        void apply(Entity player) {
            CasterComponent.MAPPER.get(player).shotDelay *= CASTING_DELAY_FACTOR;
        }
    },
    /** Raises the most health the player can have by {@link #HEALTH_BONUS}, and heals by the same amount. */
    MORE_HEALTH("More health", "Gain 25% of base maximum health") {
        @Override
        void apply(Entity player) {
            HealthComponent health = HealthComponent.MAPPER.get(player);
            health.max += HEALTH_BONUS;
            health.heal(HEALTH_BONUS);
        }
    },
    /** Walks faster: each pick multiplies the walking speed by {@link #SPEED_FACTOR}. */
    FASTER_MOVEMENT("Faster movement", "Walk 20% faster") {
        @Override
        void apply(Entity player) {
            PlayerComponent.MAPPER.get(player).speed *= SPEED_FACTOR;
        }
    },
    /** Hits harder: each pick multiplies the damage of the player's spells by {@link #SPELL_DAMAGE_FACTOR}. */
    STRONGER_SPELLS("Stronger spells", "Spells deal 25% more damage") {
        @Override
        void apply(Entity player) {
            CasterComponent.MAPPER.get(player).spellDamage *= SPELL_DAMAGE_FACTOR;
        }
    };

    /** The number of upgrades each break offers. */
    public static final int    OFFER_COUNT          = 3;
    /** What {@link #FASTER_CASTING} multiplies the seconds between casts by: 8 casts per second become 10. */
    public static final float  CASTING_DELAY_FACTOR = 0.8f;
    /** The maximum health that {@link #MORE_HEALTH} adds, where the player starts with 1. */
    public static final double HEALTH_BONUS         = 0.25;
    /** What {@link #FASTER_MOVEMENT} multiplies the walking speed by: 120 px/s becomes 144. */
    public static final double SPEED_FACTOR         = 1.2;
    /** What {@link #STRONGER_SPELLS} multiplies spell damage by: 0.1 becomes 0.125, so 8 hits kill instead of 10. */
    public static final double SPELL_DAMAGE_FACTOR  = 1.25;

    private final String title;
    private final String description;

    Upgrade(String title, String description) {
        this.title       = title;
        this.description = description;
    }

    /** Gets the name to show for the upgrade, such as "Faster casting". */
    public String getTitle() {
        return title;
    }

    /** Gets a short line that says what the upgrade does. */
    public String getDescription() {
        return description;
    }

    /** Changes the player's components by this upgrade's effect. */
    abstract void apply(Entity player);
}
