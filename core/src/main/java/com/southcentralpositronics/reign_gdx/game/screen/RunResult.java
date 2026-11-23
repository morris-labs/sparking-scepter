package com.southcentralpositronics.reign_gdx.game.screen;

/**
 * How a run ended, for the game-over screen.
 *
 * @param score        The run's score.
 * @param wave         The wave the player reached.
 * @param newHighScore Whether the score is a new high score.
 */
public record RunResult(long score, int wave, boolean newHighScore) {
}
