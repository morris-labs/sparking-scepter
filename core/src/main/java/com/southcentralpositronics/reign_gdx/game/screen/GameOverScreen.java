package com.southcentralpositronics.reign_gdx.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.southcentralpositronics.reign_gdx.game.Game;

import java.util.ArrayList;
import java.util.List;

/**
 * The screen after the player dies. It shows the run's score, the wave
 * reached, and a notice when the score is a new high score, and offers Play
 * again, which starts a fresh run, and Main menu.
 */
public class GameOverScreen extends ScreenAdapter {
    private final Stage stage;

    /**
     * Builds the game-over screen.
     *
     * @param game   The game whose skin the screen uses, and that starts the
     *               next run or shows the main menu.
     * @param result How the run ended.
     */
    public GameOverScreen(Game game, RunResult result) {
        List<Menu.Line> lines = new ArrayList<>();
        lines.add(new Menu.Line("Score: " + result.score()));
        lines.add(new Menu.Line("Wave reached: " + result.wave()));
        if (result.newHighScore()) lines.add(new Menu.Line("New high score!", Menu.HIGHLIGHT));

        stage = new Stage(new ScreenViewport(), game.getBatch());
        stage.addActor(Menu.build(game.getSkin(), "Game over", lines,
            new Menu.Item("Play again", game::startRun),
            new Menu.Item("Main menu", game::showMainMenu)));
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
