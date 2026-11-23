package com.southcentralpositronics.reign_gdx.game.score;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The high score store keeps the best score from any run, using the in-memory version. */
class HighScoreStoreTest {
    @Test
    void storeStartsWithNoHighScore() {
        assertEquals(0, new InMemoryHighScoreStore().getHighScore());
    }

    @Test
    void firstScoredRunSetsANewHighScore() {
        HighScoreStore store = new InMemoryHighScoreStore();

        assertTrue(store.submit(1200));
        assertEquals(1200, store.getHighScore());
    }

    @Test
    void lowerScoreKeepsTheBestScore() {
        HighScoreStore store = new InMemoryHighScoreStore();
        store.submit(1200);

        assertFalse(store.submit(800));
        assertEquals(1200, store.getHighScore());
    }

    @Test
    void tyingTheHighScoreIsNotANewHighScore() {
        HighScoreStore store = new InMemoryHighScoreStore();
        store.submit(1200);

        assertFalse(store.submit(1200));
        assertEquals(1200, store.getHighScore());
    }

    @Test
    void higherScoreReplacesTheHighScore() {
        HighScoreStore store = new InMemoryHighScoreStore();
        store.submit(1200);

        assertTrue(store.submit(1500));
        assertEquals(1500, store.getHighScore());
    }

    @Test
    void runWithNoScoreIsNotANewHighScore() {
        HighScoreStore store = new InMemoryHighScoreStore();

        assertFalse(store.submit(0));
        assertEquals(0, store.getHighScore());
    }
}
