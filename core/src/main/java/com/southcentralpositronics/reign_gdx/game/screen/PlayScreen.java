package com.southcentralpositronics.reign_gdx.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.southcentralpositronics.reign_gdx.engine.input.Keyboard;
import com.southcentralpositronics.reign_gdx.engine.input.Mouse;
import com.southcentralpositronics.reign_gdx.engine.input.PlayerInput;
import com.southcentralpositronics.reign_gdx.engine.level.Arena;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;
import com.southcentralpositronics.reign_gdx.engine.time.FixedTimestep;
import com.southcentralpositronics.reign_gdx.game.Game;
import com.southcentralpositronics.reign_gdx.game.render.WorldRenderer;
import com.southcentralpositronics.reign_gdx.game.sim.EntityState;
import com.southcentralpositronics.reign_gdx.game.sim.Simulation;
import com.southcentralpositronics.reign_gdx.game.ui.Hud;

/**
 * The screen for one run. It wraps a {@link Simulation}: each frame it turns
 * keyboard and mouse input into a {@link PlayerInput}, runs as many fixed ticks
 * as the frame time calls for, and draws the result.
 *
 * <p>Escape, or the window losing focus, pauses the run. While paused, no ticks
 * run, and the pause screen draws over the frozen play screen with Resume and
 * Main menu buttons.
 */
public class PlayScreen extends ScreenAdapter {
    /** The width and height of a mob's collision box, in pixels. */
    private static final int     HITBOX_WIDTH  = 16;
    private static final int     HITBOX_HEIGHT = 8;
    /** The tint drawn over the play screen while paused. */
    private static final Color   PAUSE_DIM     = new Color(0, 0, 0, 0.6f);

    private final FixedTimestep      timestep = new FixedTimestep();
    private final Vector2            aim      = new Vector2();
    private final SpriteBatch        batch;
    private final ShapeRenderer      shapeRenderer;
    private final Viewport           viewport;
    private final OrthographicCamera camera;
    private final Keyboard           keyboard = new Keyboard();
    private final Mouse              mouse    = new Mouse();
    private final Simulation         simulation;
    /** Outlines every mob's collision box: one of the debug tools. */
    private final boolean            drawHitboxes;
    private final WorldRenderer      worldRenderer;
    private final Hud                hud;
    private final Stage              pauseStage;
    private final InputMultiplexer   input    = new InputMultiplexer();
    private final InputAdapter       pauseKey = new InputAdapter() {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode != Input.Keys.ESCAPE) return false;
            if (isPaused()) resumeRun(); else pause();
            return true;
        }
    };

    /**
     * Starts a run in the game's arena.
     *
     * @param game The game that owns the shared resources and the main menu.
     * @param seed The random seed for the run.
     */
    public PlayScreen(Game game, long seed) {
        batch         = game.getBatch();
        shapeRenderer = new ShapeRenderer();
        // The world is always VIEWPORT_HEIGHT pixels tall; its width follows the
        // game area's aspect ratio, so the side panel doesn't squash the view.
        viewport = new ExtendViewport(Game.VIEWPORT_HEIGHT, Game.VIEWPORT_HEIGHT, new OrthographicCamera());
        camera   = (OrthographicCamera) viewport.getCamera();
        camera.position.set(Game.VIEWPORT_WIDTH / 2f, Game.VIEWPORT_HEIGHT / 2f, 0);

        simulation    = new Simulation(Arena.fromTmx(Gdx.files.internal(Game.ARENA_PATH)), seed);
        worldRenderer = new WorldRenderer(game.getMobAtlas(), game.getTileAtlas());
        drawHitboxes  = game.isDebug();
        hud           = new Hud(simulation, Game.PLAYER_NAME, game.isDebug());

        Skin  skin  = game.getSkin();
        Table pause = Menu.build(skin, "Paused",
            new Menu.Item("Resume", this::resumeRun),
            new Menu.Item("Main menu", game::showMainMenu));
        pause.setBackground(skin.newDrawable("white", PAUSE_DIM));
        pauseStage = new Stage(new ScreenViewport(), batch);
        pauseStage.addActor(pause);

        routeInput();
    }

    /** Returns whether the run is paused. */
    public boolean isPaused() {
        return timestep.isPaused();
    }

    /** Pauses the run and shows the pause screen. The game calls this when the window loses focus. */
    @Override
    public void pause() {
        timestep.pause();
        routeInput();
    }

    /**
     * Does nothing. The game calls this when the window regains focus, and the
     * run stays paused until the player picks Resume or presses Escape.
     */
    @Override
    public void resume() {
    }

    /** Closes the pause screen and runs ticks again. */
    private void resumeRun() {
        timestep.resume();
        routeInput();
    }

    /**
     * Sends input to the pause screen while paused, and to the side panel and
     * the player otherwise. The panel comes before the player so its buttons
     * take clicks before the spell input. The keyboard and mouse always listen,
     * so a key released while paused doesn't stay held after resuming.
     */
    private void routeInput() {
        input.clear();
        input.addProcessor(pauseKey);
        input.addProcessor(isPaused() ? pauseStage : hud.getStage());
        input.addProcessor(keyboard);
        input.addProcessor(mouse);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(input);
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == input) Gdx.input.setInputProcessor(null);
    }

    @Override
    public void resize(int width, int height) {
        // The game draws left of the side panel.
        viewport.update(Math.max(1, width - Hud.PANEL_WIDTH), height);
        hud.resize(width, height);
        pauseStage.getViewport().update(width, height, true);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        keyboard.update();
        simulation.setInput(readInput());
        for (int ticks = timestep.advance(delta); ticks > 0; ticks--) simulation.tick();
        // Nothing listens to events yet; drain them so they don't pile up.
        simulation.drainEvents();
        hud.update(delta);
        viewport.apply();

        // Follow the player's sprite corner, as the frame-based version did, but
        // stop at the map edges.
        EntityState player     = simulation.getPlayer();
        float       halfWidth  = camera.viewportWidth / 2;
        float       halfHeight = camera.viewportHeight / 2;

        int mapPixelWidth  = simulation.getWidth() * Tile.SIZE;
        int mapPixelHeight = simulation.getHeight() * Tile.SIZE;

        float spriteX = (int) (player.x() - EntityState.MOB_SPRITE_OFFSET_X);
        float spriteY = (int) (player.y() - EntityState.MOB_SPRITE_OFFSET_Y);
        float cameraX = MathUtils.clamp(spriteX, halfWidth, mapPixelWidth - halfWidth);
        float cameraY = MathUtils.clamp(spriteY, halfHeight, mapPixelHeight - halfHeight);

        // Keep the camera on whole world pixels; the clamp bounds can be fractional.
        camera.position.set(Math.round(cameraX), Math.round(cameraY), 0);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.enableBlending();
        worldRenderer.render(batch, simulation);
        batch.end();

        if (drawHitboxes) drawHitboxes();
        hud.draw();

        if (isPaused()) {
            pauseStage.act(delta);
            pauseStage.getViewport().apply();
            pauseStage.draw();
        }
    }

    /**
     * Turns the held keys and the mouse into simulation input. Up wins over
     * down and left wins over right, as in the frame-based version.
     */
    private PlayerInput readInput() {
        float moveX = keyboard.left ? -1 : keyboard.right ? 1 : 0;
        float moveY = keyboard.up ? 1 : keyboard.down ? -1 : 0;
        viewport.unproject(aim.set(Mouse.getX(), Mouse.getY()));
        return new PlayerInput(moveX, moveY, aim.x, aim.y, mouse.isLeftPressed());
    }

    /** Outlines each mob's collision box in red. */
    private void drawHitboxes() {
        float thickness = 1.5f;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        for (EntityState mob : simulation.getEntities()) {
            if (!mob.kind().isMob()) continue;
            float hitboxX = (float) mob.x() - HITBOX_WIDTH / 2f;
            float hitboxY = (float) mob.y() - HITBOX_HEIGHT / 2f;
            shapeRenderer.rect(hitboxX, hitboxY, thickness, HITBOX_HEIGHT);
            shapeRenderer.rect(hitboxX + HITBOX_WIDTH - thickness, hitboxY, thickness, HITBOX_HEIGHT);
            shapeRenderer.rect(hitboxX, hitboxY, HITBOX_WIDTH, thickness);
            shapeRenderer.rect(hitboxX, hitboxY + HITBOX_HEIGHT - thickness, HITBOX_WIDTH, thickness);
        }
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        worldRenderer.dispose();
        hud.dispose();
        pauseStage.dispose();
    }
}
