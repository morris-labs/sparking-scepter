package com.southcentralpositronics.reign_gdx.game.sim;

/**
 * Something that happened during a tick, for audio and the HUD to react to.
 *
 * @param type The kind of event.
 * @param kind The kind of entity it happened to: the caster for
 *             {@link Type#CAST}, the mob hit or killed for the others, and
 *             {@link EntityKind#PLAYER} for {@link Type#WAVE_START}.
 * @param x    The x coordinate where it happened, in world pixels.
 * @param y    The y coordinate where it happened, in world pixels.
 */
public record SimulationEvent(Type type, EntityKind kind, double x, double y) {
    public enum Type {
        /** A mob cast a spell. */
        CAST,
        /** A player's spell hit an enemy. */
        HIT,
        /** An enemy died. */
        ENEMY_DEATH,
        /** An enemy's spell hit the player. */
        PLAYER_DAMAGE,
        /** The player died. Nothing raises it until the player can die. */
        PLAYER_DEATH,
        /** A wave of enemies entered the arena. */
        WAVE_START
    }
}
