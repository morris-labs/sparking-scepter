package com.southcentralpositronics.reign_gdx.game.score;

import com.badlogic.gdx.Preferences;

/**
 * A high score store that saves the high score in libGDX {@link Preferences}
 * storage, so it survives closing the game. On the desktop, libGDX keeps the
 * preferences in the {@code ~/.prefs/} folder, in a file named after the
 * preferences.
 */
public final class PreferencesHighScoreStore implements HighScoreStore {
    /** The name of the game's preferences file. */
    public static final String PREFERENCES_NAME = "reign";
    /** The key that holds the high score. */
    static final String HIGH_SCORE_KEY = "highScore";

    private final Preferences preferences;

    /**
     * Creates a store.
     *
     * @param preferences The preferences to keep the high score in; the game
     *                    passes {@code Gdx.app.getPreferences(PREFERENCES_NAME)}.
     */
    public PreferencesHighScoreStore(Preferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public long getHighScore() {
        return preferences.getLong(HIGH_SCORE_KEY, 0L);
    }

    @Override
    public void setHighScore(long score) {
        preferences.putLong(HIGH_SCORE_KEY, score);
        preferences.flush();
    }
}
