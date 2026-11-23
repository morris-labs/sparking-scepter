package com.southcentralpositronics.reign_gdx.game.score;

/** A high score store that forgets the high score when the program ends. Tests use it. */
public final class InMemoryHighScoreStore implements HighScoreStore {
    private long highScore;

    @Override
    public long getHighScore() {
        return highScore;
    }

    @Override
    public void setHighScore(long score) {
        highScore = score;
    }
}
