package com.southcentralpositronics.reign_gdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.southcentralpositronics.reign_gdx.engine.input.Keyboard;
import com.southcentralpositronics.reign_gdx.engine.input.Mouse;
import com.southcentralpositronics.reign_gdx.engine.input.PlayerInput;
import com.southcentralpositronics.reign_gdx.engine.level.Arena;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;
import com.southcentralpositronics.reign_gdx.engine.time.FixedTimestep;
import com.southcentralpositronics.reign_gdx.game.render.WorldRenderer;
import com.southcentralpositronics.reign_gdx.game.sim.EntityState;
import com.southcentralpositronics.reign_gdx.game.sim.Simulation;
import com.southcentralpositronics.reign_gdx.game.ui.Hud;

/**
 * The game application. It wraps a {@link Simulation}: each frame it turns
 * keyboard and mouse input into a {@link PlayerInput}, runs as many fixed ticks
 * as the frame time calls for, and draws the result.
 */
public class Game extends ApplicationAdapter {
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
    /** Draws a red outline around the player's collision box. For debugging. */
    private static final boolean DRAW_HITBOX   = false;
    /** The width and height of a mob's collision box, in pixels. */
    private static final int     HITBOX_WIDTH  = 16;
    private static final int     HITBOX_HEIGHT = 8;

    private final FixedTimestep      timestep = new FixedTimestep();
    private final Vector2            aim      = new Vector2();
    private       ShapeRenderer      shapeRenderer;
    private       SpriteBatch        batch;
    private       Viewport           viewport;
    private       TextureAtlas       mobAtlas, tileAtlas;
    private       Keyboard           keyboard;
    private       Mouse              mouse;
    private       Simulation         simulation;
    private       WorldRenderer      worldRenderer;
    private       Hud                hud;
    private       OrthographicCamera camera = new OrthographicCamera();

    @Override
    public void create() {
        mobAtlas      = new TextureAtlas(Gdx.files.internal("atlas/mobs.atlas"));
        tileAtlas     = new TextureAtlas(Gdx.files.internal("atlas/Tiles.atlas"));
        shapeRenderer = new ShapeRenderer();
        batch         = new SpriteBatch();
        keyboard      = new Keyboard();
        mouse         = new Mouse();
        // The world is always VIEWPORT_HEIGHT pixels tall; its width follows the
        // game area's aspect ratio, so the side panel doesn't squash the view.
        viewport = new ExtendViewport(VIEWPORT_HEIGHT, VIEWPORT_HEIGHT, camera);
        viewport.apply();
        camera = (OrthographicCamera) viewport.getCamera();
        camera.position.set(VIEWPORT_WIDTH / 2f, VIEWPORT_HEIGHT / 2f, 0);
        camera.update();

        simulation    = new Simulation(Arena.fromPng(Gdx.files.internal(MAP_PATH)), System.nanoTime());
        worldRenderer = new WorldRenderer(mobAtlas, tileAtlas);
        hud           = new Hud(simulation, PLAYER_NAME);
        // The panel comes first so its buttons take clicks before the spell input.
        Gdx.input.setInputProcessor(new InputMultiplexer(hud.getStage(), keyboard, mouse));
    }

    @Override
    public void resize(int width, int height) {
        // The game draws left of the side panel.
        viewport.update(Math.max(1, width - Hud.PANEL_WIDTH), height);
        hud.resize(width, height);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

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

        if (DRAW_HITBOX) drawPlayerHitbox(player);
        hud.draw();
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

    /** Outlines the player's collision box in red. */
    private void drawPlayerHitbox(EntityState player) {
        float thickness = 1.5f;
        float hitboxX   = (float) player.x() - HITBOX_WIDTH / 2f;
        float hitboxY   = (float) player.y() - HITBOX_HEIGHT / 2f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(hitboxX, hitboxY, thickness, HITBOX_HEIGHT);
        shapeRenderer.rect(hitboxX + HITBOX_WIDTH - thickness, hitboxY, thickness, HITBOX_HEIGHT);
        shapeRenderer.rect(hitboxX, hitboxY, HITBOX_WIDTH, thickness);
        shapeRenderer.rect(hitboxX, hitboxY + HITBOX_HEIGHT - thickness, HITBOX_WIDTH, thickness);
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        mobAtlas.dispose();
        tileAtlas.dispose();
        shapeRenderer.dispose();
        worldRenderer.dispose();
        hud.dispose();
    }
}
