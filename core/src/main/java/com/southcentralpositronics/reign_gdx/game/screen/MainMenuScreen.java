package com.southcentralpositronics.reign_gdx.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.southcentralpositronics.reign_gdx.game.Game;

import java.util.List;

/**
 * The screen the game opens on. It shows the high score and offers Play, which
 * starts a run, and Quit.
 */
public class MainMenuScreen extends ScreenAdapter {
    private final Stage stage;

    /**
     * Builds the main menu.
     *
     * @param game The game whose skin and high score the menu shows, and that
     *             starts the run.
     */
    public MainMenuScreen(Game game) {
        stage = new Stage(new ScreenViewport(), game.getBatch());
        stage.addActor(Menu.build(game.getSkin(), Game.TITLE,
            List.of(new Menu.Line("High score: " + game.getHighScores().getHighScore())),
            new Menu.Item("Play", game::startRun),
            new Menu.Item("Quit", Gdx.app::exit)));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.12f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.getViewport().apply();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == stage) Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
