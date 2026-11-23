package com.southcentralpositronics.reign_gdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.southcentralpositronics.reign_gdx.game.screen.MainMenuScreen;
import com.southcentralpositronics.reign_gdx.game.screen.PlayScreen;

/**
 * The game application. It owns the resources that every screen shares, such
 * as the atlases and the UI skin, and switches between the main menu and the
 * play screen. The game opens on the main menu.
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
    /** The 64x64 test map; levels/SpawnLevel.png is the Java2D spawn level. */
    public static final String MAP_PATH        = "levels/TestMap.png";

    private SpriteBatch  batch;
    private TextureAtlas mobAtlas, tileAtlas;
    private Skin         skin;

    @Override
    public void create() {
        batch     = new SpriteBatch();
        mobAtlas  = new TextureAtlas(Gdx.files.internal("atlas/mobs.atlas"));
        tileAtlas = new TextureAtlas(Gdx.files.internal("atlas/Tiles.atlas"));
        skin      = new Skin(Gdx.files.internal("ui/default/uiskin.json"));
        showMainMenu();
    }

    /** Shows the main menu and ends any run in progress. */
    public void showMainMenu() {
        switchTo(new MainMenuScreen(this));
    }

    /** Starts a new run on the play screen. */
    public void startRun() {
        switchTo(new PlayScreen(this, System.nanoTime()));
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
