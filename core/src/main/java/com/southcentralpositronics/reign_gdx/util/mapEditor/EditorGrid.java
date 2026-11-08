package com.southcentralpositronics.reign_gdx.util.mapEditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.IntMap;
import com.southcentralpositronics.reign_gdx.graphics.TileLoader;

import java.util.Arrays;

import static com.southcentralpositronics.reign_gdx.util.mapEditor.PixelMapIO.argbToRgba;
import static com.southcentralpositronics.reign_gdx.util.mapEditor.PixelMapIO.rgbaToArgb;

public class EditorGrid extends Widget {        // <── Widget = Actor + Layout
    /* ---- constants ----------------------------------------------------- */
    private static final int   TILE = 16;
    private              float camX = 0, camY = 0;          // upper-left corner of camera
    private float camW, camH;                               // filled in layout()
    private       float   zoom     = 1f;                         // 0.5 – 4.0   (slider)
    private final float   SPEED    = 200f;
    public final  boolean showGrid = true;

    /* ---- state --------------------------------------------------------- */
    private final IntMap<TextureRegion> texByColor = new IntMap<>();

    private int     width  = 64;                        // in tiles
    private int     height = 64;
    private int     tilesWide;                          // filled in layout()
    private int     tilesHigh;
    private int[][] pixels = new int[width][height];

    public               int           currentColor = TileLoader.COLOR_GRASS;   // palette selection
    private static final TextureRegion PIXEL;

    static {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(1, 1, 1, 1);
        pm.fill();
        PIXEL = new TextureRegion(new Texture(pm));
        pm.dispose();
    }

    /* ---- ctor ---------------------------------------------------------- */
    public EditorGrid(TextureAtlas atlas) {
        setMapSize(width, height);        // alloc, clear, camera, pref size
        fillTexLookup(atlas);


        /* paint & drag ---------------------------------------------------- */
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                paintAt(x, y);
                return true;
            }

            @Override
            public void touchDragged(InputEvent e, float x, float y, int p) {
                paintAt(x, y);
            }

            private void paintAt(float localX, float localY) {
                float worldX = camX + localX / zoom;
                float worldY = camY + (getHeight() - localY) / zoom;

                int gx = (int) (worldX / TILE);
                int gy = (int) (worldY / TILE);

                if (gx >= 0 && gy >= 0 && gx < width && gy < height) pixels[gx][gy] = currentColor;
            }
        });

        /* zoom ------------------------------------------------------------ */
        addListener(new InputListener() {
            //            @Override
            public boolean scrolled(InputEvent e, float amountX, float amountY) {
                invalidate();       // ask Scene2D to relayout if needed
                return true;
            }
        });

        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent e, int keycode) {
                return true;
            }
        });
    }

    /* ---- layout & sizing ---------------------------------------------- */

//    @Override
//    public void layout() {
//        float tilePx = TILE * zoom;
//
//        tilesWide = Math.min(width, (int) (getWidth() / tilePx));
//        tilesHigh = Math.min(height, (int) (getHeight() / tilePx));
//    }

    @Override
    public void act(float delta) {
        super.act(delta);
        float v = SPEED / zoom * delta; // move faster when zoomed-out
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) camX -= v;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) camX += v;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) camY += v;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) camY -= v;
        clampCamera();
    }

    @Override
    public void layout() {
        camW = getWidth() / zoom;
        camH = getHeight() / zoom;
        clampCamera();
    }

    @Override
    public float getPrefWidth() {
        return width * TILE;
    }

    @Override
    public float getPrefHeight() {
        return height * TILE;
    }

    /**
     * Called from the sidebar when the user creates / loads a map
     */
    public void setMapSize(int w, int h) {
        width  = w;
        height = h;
        pixels = new int[w][h];
        clear();
        invalidateHierarchy();                  // let parent recalc layout
    }

    /* ---- drawing ------------------------------------------------------- */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.setColor(1, 1, 1, parentAlpha);

        float tilePx   = TILE;
        float camRight = camX + camW;
        float camTop   = camY + camH;

        int x0 = Math.max(0, (int) (camX / tilePx));
        int x1 = Math.min(width, (int) (camRight / tilePx) + 1);
        int y0 = Math.max(0, (int) (camY / tilePx));
        int y1 = Math.min(height, (int) (camTop / tilePx) + 1);

        float screenBaseX = getX();
        float screenBaseY = getY() + getHeight();   // Scene2D origin is bottom-left

        for (int y = y0; y < y1; y++)
            for (int x = x0; x < x1; x++) {
                TextureRegion tr = texByColor.get(pixels[x][y]);
                if (tr == null) continue;

                float sx = screenBaseX + (x * tilePx - camX) * zoom;
                float sy = screenBaseY - ((y + 1) * tilePx - camY) * zoom;

                batch.draw(tr, sx, sy, tilePx * zoom, tilePx * zoom);
            }

        /* ===== grid overlay (map-bounded) ================================== */
        if (showGrid) {
            float lineW  = 1f;

            // colour: light-cyan, 25 % alpha
            batch.setColor(0.4f, 0.9f, 1f, 0.25f);

            /* vertical lines -------------------------------------------------- */
            for (int gx = x0; gx <= x1; gx++) {
                float sx = screenBaseX + (gx * TILE - camX) * zoom;

                // screen Y for top & bottom *map* row visible
                float syTop    = screenBaseY - ((y0 * TILE - camY) * zoom);
                float syBottom = screenBaseY - ((y1 * TILE - camY) * zoom);

                batch.draw(PIXEL, sx, syBottom, lineW, syTop - syBottom);
            }

            /* horizontal lines ------------------------------------------------ */
            for (int gy = y0; gy <= y1; gy++) {
                float sy = screenBaseY - ((gy * TILE - camY) * zoom);

                float sxLeft  = screenBaseX + (x0 * TILE - camX) * zoom;
                float sxRight = screenBaseX + (x1 * TILE - camX) * zoom;

                batch.draw(PIXEL, sxLeft, sy, sxRight - sxLeft, lineW);
            }

            batch.setColor(1, 1, 1, parentAlpha);   // restore tint
        }
        /* =================================================================== */
    }


    /* ---- utilities ----------------------------------------------------- */
    private void fillTexLookup(TextureAtlas atlas) {
        texByColor.put(TileLoader.COLOR_GRASS, atlas.findRegion("grass"));
        texByColor.put(TileLoader.COLOR_HEDGE, atlas.findRegion("hedge"));
        texByColor.put(TileLoader.COLOR_WATER, atlas.findRegion("water"));
        texByColor.put(TileLoader.COLOR_WALL, atlas.findRegion("wall"));
        texByColor.put(TileLoader.COLOR_WALLALT, atlas.findRegion("wall_alt"));
        texByColor.put(TileLoader.COLOR_FLOOR, atlas.findRegion("floor"));
        texByColor.put(TileLoader.COLOR_VOID, atlas.findRegion("void_tile"));
    }

    public void clear() {
        for (int[] col : pixels) Arrays.fill(col, TileLoader.COLOR_VOID);
    }

    /* ---------- SAVE --------------------------------------------------- */
    public Pixmap toPixmap () {
        Pixmap pix = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                 pix.drawPixel(x, y, argbToRgba(pixels[x][y]));   // <-- no flip
        return pix;
    }

    /* ---------- LOAD --------------------------------------------------- */
    public void loadFromPixmap (Pixmap pix) {
        setMapSize(pix.getWidth(), pix.getHeight());
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                 pixels[x][y] = rgbaToArgb(pix.getPixel(x, y));   // <-- no flip
    }

    public void setZoom(float z) {
        float oldCenterX = camX + camW * .5f;
        float oldCenterY = camY + camH * .5f;

        zoom = MathUtils.clamp(z, .5f, 8f);

        /* recompute size and re-center on same world point */
        camW = getWidth() / zoom;
        camH = getHeight() / zoom;
        camX = oldCenterX - camW * .5f;
        camY = oldCenterY - camH * .5f;

        clampCamera();
        invalidate();          // ask Scene2D to redraw
    }

    private void clampCamera() {
        float mapW = width * TILE;
        float mapH = height * TILE;

        if (camW >= mapW) {
            camX = (mapW - camW) * 0.5f;        // centre horizontally
        } else {
            camX = MathUtils.clamp(camX, 0, mapW - camW);
        }

        if (camH >= mapH) {
            camY = (mapH - camH) * 0.5f;        // centre vertically
        } else {
            camY = MathUtils.clamp(camY, 0, mapH - camH);
        }
    }
}
