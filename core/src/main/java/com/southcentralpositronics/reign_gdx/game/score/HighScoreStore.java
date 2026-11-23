package com.southcentralpositronics.reign_gdx.game.score;

/**
 * Keeps the high score: the best score from any run, kept between runs. The
 * game saves it with {@link PreferencesHighScoreStore}, so it survives closing
 * the game; tests use {@link InMemoryHighScoreStore}.
 *
 * <p>Implementations only load and save the value; {@link #submit(long)}
 * holds the rule for what counts as a new high score.
 */
public interface HighScoreStore {
    /** Gets the high score, or 0 when no run has scored yet. */
    long getHighScore();

    /**
     * Saves a high score, replacing the one kept.
     *
     * @param score The score to keep.
     */
    void setHighScore(long score);

    /**
     * Records the score of a run that ended, and keeps it when it beats the
     * high score. A tie isn't a new high score.
     *
     * @param score The run's score.
     * @return True when the score is a new high score.
     */
    default boolean submit(long score) {
        if (score <= getHighScore()) return false;
        setHighScore(score);
        return true;
    }
}
