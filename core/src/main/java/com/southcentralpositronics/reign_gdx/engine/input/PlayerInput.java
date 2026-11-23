package com.southcentralpositronics.reign_gdx.engine.input;

/**
 * What the person playing asks the player to do during one tick.
 *
 * @param moveX   The horizontal move direction, from -1 (left) to 1 (right).
 * @param moveY   The vertical move direction, from -1 (down) to 1 (up).
 * @param aimX    The x coordinate of the aim point, in world pixels.
 * @param aimY    The y coordinate of the aim point, in world pixels.
 * @param casting Whether the player casts spells toward the aim point.
 */
public record PlayerInput(float moveX, float moveY, float aimX, float aimY, boolean casting) {
    /** No movement and no casting. */
    public static final PlayerInput NONE = new PlayerInput(0, 0, 0, 0, false);

    /** Clamps each move component to the range from -1 to 1. */
    public PlayerInput {
        moveX = Math.max(-1f, Math.min(1f, moveX));
        moveY = Math.max(-1f, Math.min(1f, moveY));
    }

    /** Creates input that moves without casting. */
    public static PlayerInput move(float moveX, float moveY) {
        return new PlayerInput(moveX, moveY, 0, 0, false);
    }

    /** Creates input that casts toward a world point without moving. */
    public static PlayerInput castAt(float aimX, float aimY) {
        return new PlayerInput(0, 0, aimX, aimY, true);
    }
}
