package com.southcentralpositronics.reign_gdx.game.sim;

/**
 * A read-only snapshot of one entity after a tick.
 *
 * <p>The position depends on the kind:
 *
 * <ul>
 *   <li>A mob's position is the center of its collision box: the 16x8 px box
 *       at its feet. Its 32x32 px sprite's bottom-left corner is
 *       {@link #MOB_SPRITE_OFFSET_X} and {@link #MOB_SPRITE_OFFSET_Y} pixels
 *       left of and below that point.
 *   <li>A spell's position is its center.
 *   <li>A particle's position is the point to draw it at, raised by its height.
 * </ul>
 *
 * @param kind          The kind of entity.
 * @param x             The x coordinate of the position, in world pixels.
 * @param y             The y coordinate of the position, in world pixels.
 * @param health        A mob's health, from 0 to 1; 0 for other kinds.
 * @param facing        The direction a mob faces; {@link Facing#DOWN} for other kinds.
 * @param walking       Whether a mob moved this tick.
 * @param animationTime The seconds a mob has been walking, which picks its
 *                      animation frame; 0 when it stands still.
 */
public record EntityState(EntityKind kind, double x, double y, double health, Facing facing, boolean walking,
                          float animationTime) {
    /** The distance from a mob sprite's left edge to the mob's position, in pixels. */
    public static final int MOB_SPRITE_OFFSET_X = 16;
    /** The distance from a mob sprite's bottom edge to the mob's position, in pixels. */
    public static final int MOB_SPRITE_OFFSET_Y = 4;
}
