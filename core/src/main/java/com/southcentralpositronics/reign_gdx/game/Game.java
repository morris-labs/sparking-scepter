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
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.southcentralpositronics.reign_gdx.game.entity.mob.PlayerMob;
import com.southcentralpositronics.reign_gdx.engine.input.Keyboard;
import com.southcentralpositronics.reign_gdx.engine.input.Mouse;
import com.southcentralpositronics.reign_gdx.game.level.ReignLevel;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;
import com.southcentralpositronics.reign_gdx.engine.level.tile.TileCoordinates;
import com.southcentralpositronics.reign_gdx.game.ui.Hud;

public class Game extends ApplicationAdapter {
    public static final int           WIDTH           = (int) (1920);
    public static final int           HEIGHT          = (int) (1080);
    public static final int           SCALE           = 4;
    public static final int           VIEWPORT_WIDTH  = WIDTH / SCALE;
    public static final int           VIEWPORT_HEIGHT = HEIGHT / SCALE;
    public static final String        TITLE           = "Reign";
    /** Draws a red outline around the player's collision box. For debugging. */
    private static final boolean      DRAW_HITBOX     = false;
    protected           ShapeRenderer shapeRenderer;
    protected           String        mapPath;


    private SpriteBatch  batch;
    private Viewport     viewport;
    private TextureAtlas mobAtlas, tileAtlas;
    private Keyboard           keyboard;
    private Mouse              mouse;
    private ReignLevel              level;
    private Hud                hud;
    private PlayerMob          player;
    private OrthographicCamera camera = new OrthographicCamera();

    @Override
    public void create() {
        String mobAtlasPath  = "atlas/mobs.atlas";
        String tileAtlasPath = "atlas/Tiles.atlas";
        // The 64x64 test map; levels/SpawnLevel.png is the Java2D spawn level.
        mapPath = "levels/TestMap.png";
        mobAtlas      = new TextureAtlas(Gdx.files.internal(mobAtlasPath));
        tileAtlas     = new TextureAtlas(Gdx.files.internal(tileAtlasPath));
        shapeRenderer = new ShapeRenderer();

        batch           = new SpriteBatch();
        keyboard        = new Keyboard();
        mouse           = new Mouse();
        // The world is always VIEWPORT_HEIGHT pixels tall; its width follows the
        // game area's aspect ratio, so the side panel doesn't squash the view.
        viewport = new ExtendViewport(VIEWPORT_HEIGHT, VIEWPORT_HEIGHT, camera);
        viewport.apply();
        camera = (OrthographicCamera) viewport.getCamera();
        camera.position.set(VIEWPORT_WIDTH / 2f, VIEWPORT_HEIGHT / 2f, 0);
        camera.update();


        level = new ReignLevel(this);
        // TileCoordinates holds pixels; placeOnTile() takes tile indexes.
        TileCoordinates spawnTile = level.getRandomNonSolidTile();
        player = new PlayerMob(this, 0, 0, "King Steve");
        player.placeOnTile(spawnTile.getX() / Tile.SIZE, spawnTile.getY() / Tile.SIZE);
        level.add(player);
        level.generateMobs();

        hud = new Hud(player, level);
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


        // Update game logic
        keyboard.update();
        level.update(delta);
        hud.update(delta);
        viewport.apply();

        // Follow the player, but stop at the map edges.
        float halfWidth  = camera.viewportWidth / 2;
        float halfHeight = camera.viewportHeight / 2;

        int mapPixelWidth  = level.getWidth() * Tile.SIZE;
        int mapPixelHeight = level.getHeight() * Tile.SIZE;

        float cameraX = MathUtils.clamp((int) player.getX(), halfWidth, mapPixelWidth - halfWidth);
        float cameraY = MathUtils.clamp((int) player.getY(), halfHeight, mapPixelHeight - halfHeight);

        // Keep the camera on whole world pixels; the clamp bounds can be fractional.
        camera.position.set(Math.round(cameraX), Math.round(cameraY), 0);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);


        batch.begin();
        batch.enableBlending();
        level.render(batch);
        batch.end();

        if (DRAW_HITBOX) drawPlayerHitbox();
        hud.draw();
    }

    /** Outlines the player's collision box in red. */
    private void drawPlayerHitbox() {
        float thickness = 1.5f;

        float hitboxX      = (float) player.getX() + player.getPaddingLeft();
        float hitboxY      = (float) player.getY() + player.getPaddingBottom();
        float hitboxWidth  = player.getWidth() - player.getPaddingLeft() - player.getPaddingRight();
        float hitboxHeight = player.getHeight() - player.getPaddingTop() - player.getPaddingBottom();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);

        // Left border
        shapeRenderer.rect(hitboxX, hitboxY, thickness, hitboxHeight);
        // Right border
        shapeRenderer.rect(hitboxX + hitboxWidth - thickness, hitboxY, thickness, hitboxHeight);
        // Bottom border
        shapeRenderer.rect(hitboxX, hitboxY, hitboxWidth, thickness);
        // Top border
        shapeRenderer.rect(hitboxX, hitboxY + hitboxHeight - thickness, hitboxWidth, thickness);
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        mobAtlas.dispose();
        tileAtlas.dispose();
        shapeRenderer.dispose();
        hud.dispose();
    }

    public ReignLevel getLevel() {
        return level;
    }

    public Mouse getMouse() {
        return mouse;
    }

    public Keyboard getKeyboard() {
        return keyboard;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public TextureAtlas getMobAtlas() {
        return mobAtlas;
    }

    public TextureAtlas getTileAtlas() {
        return tileAtlas;
    }

    public String getMapPath() {
        return mapPath;
    }
}
