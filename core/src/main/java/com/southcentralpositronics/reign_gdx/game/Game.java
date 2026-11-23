package com.southcentralpositronics.reign_gdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.southcentralpositronics.reign_gdx.game.score.HighScoreStore;
import com.southcentralpositronics.reign_gdx.game.score.PreferencesHighScoreStore;
import com.southcentralpositronics.reign_gdx.game.screen.GameOverScreen;
import com.southcentralpositronics.reign_gdx.game.screen.MainMenuScreen;
import com.southcentralpositronics.reign_gdx.game.screen.PlayScreen;
import com.southcentralpositronics.reign_gdx.game.screen.RunResult;

/**
 * The game application. It owns the resources that every screen shares, such
 * as the atlases, the UI skin, and the high score store, and switches between
 * the main menu, the play screen, and the game-over screen. The game opens on
 * the main menu.
 */
public class Game extends com.badlogic.gdx.Game {
    public static final int    WIDTH           = 1920;
    public static final int    HEIGHT          = 1080;
    public static final int    SCALE           = 4;
    public static final int    VIEWPORT_WIDTH  = WIDTH / SCALE;
    public static final int    VIEWPORT_HEIGHT = HEIGHT / SCALE;
    public static final String TITLE           = "Reign";
    /** The name the side panel shows for the player. */
    public static final String PLAYER_NAME     = "King Steve";
    /** The arena that every run plays: the Java2D spawn level, converted to a Tiled TMX file. */
    public static final String ARENA_PATH      = "levels/SpawnLevel.tmx";
    /**
     * The system property that turns on the debug tools: the Add Mobs and
     * Clear Mobs buttons and the hitbox outlines. They're off unless it's
     * {@code true}; {@code ./gradlew lwjgl3:run -Pdebug} sets it.
     */
    public static final String DEBUG_PROPERTY  = "reign.debug";

    private final boolean debug = Boolean.getBoolean(DEBUG_PROPERTY);

    private SpriteBatch    batch;
    private TextureAtlas   mobAtlas, tileAtlas;
    private Skin           skin;
    private HighScoreStore highScores;

    @Override
    public void create() {
        batch      = new SpriteBatch();
        mobAtlas   = new TextureAtlas(Gdx.files.internal("atlas/mobs.atlas"));
        tileAtlas  = new TextureAtlas(Gdx.files.internal("atlas/Tiles.atlas"));
        skin       = new Skin(Gdx.files.internal("ui/default/uiskin.json"));
        highScores = new PreferencesHighScoreStore(Gdx.app.getPreferences(PreferencesHighScoreStore.PREFERENCES_NAME));
        showMainMenu();
    }

    /** Gets the store that keeps the high score between runs and between launches. */
    public HighScoreStore getHighScores() {
        return highScores;
    }

    /** Shows the main menu and ends any run in progress. */
    public void showMainMenu() {
        switchTo(new MainMenuScreen(this));
    }

    /**
     * Starts a fresh run on the play screen. Every run gets its own
     * {@link PlayScreen} and {@code Simulation}, with a new seed, so nothing
     * from an earlier run carries over.
     */
    public void startRun() {
        switchTo(new PlayScreen(this, System.nanoTime()));
    }

    /**
     * Shows the game-over screen for a run that ended.
     *
     * @param result How the run ended.
     */
    public void showGameOver(RunResult result) {
        switchTo(new GameOverScreen(this, result));
    }

    /**
     * Shows a screen and disposes of the one it replaces. The switch waits for
     * the end of the frame, so a button on the old screen can call it safely.
     */
    private void switchTo(Screen next) {
        Gdx.app.postRunnable(() -> {
            Screen previous = getScreen();
            setScreen(next);
            if (previous != null) previous.dispose();
        });
    }

    /** Checks whether the debug tools are on, as {@link #DEBUG_PROPERTY} sets. */
    public boolean isDebug() {
        return debug;
    }

    /** Gets the sprite batch that the screens draw with. */
    public SpriteBatch getBatch() {
        return batch;
    }

    /** Gets the atlas of mob and spell sprites. */
    public TextureAtlas getMobAtlas() {
        return mobAtlas;
    }

    /** Gets the atlas of tile sprites. */
    public TextureAtlas getTileAtlas() {
        return tileAtlas;
    }

    /** Gets the UI skin that the menus use. */
    public Skin getSkin() {
        return skin;
    }

    @Override
    public void dispose() {
        Screen current = getScreen();
        if (current != null) current.dispose();
        batch.dispose();
        mobAtlas.dispose();
        tileAtlas.dispose();
        skin.dispose();
    }
}
