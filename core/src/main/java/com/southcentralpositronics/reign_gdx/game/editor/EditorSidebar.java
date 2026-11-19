package com.southcentralpositronics.reign_gdx.game.editor;

import com.southcentralpositronics.reign_gdx.engine.ui.FileChooser;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.Scaling;
import com.southcentralpositronics.reign_gdx.engine.graphics.TileLoader;

import static com.southcentralpositronics.reign_gdx.game.editor.PixelMapIO.writePNG;

/**
 * The editor's controls: map size, New, Clear, Save PNG, Load PNG, zoom, and
 * the tile palette. Save JSON, Load JSON, Save, and Exit aren't wired yet.
 */
public class EditorSidebar extends Table {

    public EditorSidebar(Skin skin, TextureAtlas atlas, EditorGrid grid) {
        super(skin);
        skin.getFont("default-font").getData().setScale(3f);
        defaults().pad(6).left().top();

        // Map size inputs.
        TextField widthField = new TextField("64", skin);
        widthField.setAlignment(Align.center);
        TextField heightField = new TextField("64", skin);
        heightField.setAlignment(Align.center);

        Label widthLbl  = new Label("Map Width:", skin);
        Label heightLbl = new Label("Map Height:", skin);

        add(widthLbl).left().padRight(100).padLeft(20);
        add(widthField).left().padRight(800f);
        row().padBottom(8);

        add(heightLbl).left().padRight(100).padLeft(20);
        add(heightField).left().padRight(800f);
        row().space(50);

        // Buttons.
        TextButton savePNG  = new TextButton("Save PNG", skin);
        TextButton loadPNG  = new TextButton("Load PNG", skin);
        TextButton saveJSON = new TextButton("Save JSON", skin);
        TextButton loadJSON = new TextButton("Load JSON", skin);
        TextButton clear    = new TextButton("Clear", skin);
        TextButton newMap   = new TextButton("New", skin);
        TextButton save     = new TextButton("Save", skin);
        TextButton exit     = new TextButton("Exit", skin);
        Table      table    = new Table();

        int hPad = 100;
        int vPad = 5;
        table.defaults().pad(vPad, hPad, vPad, hPad).width(300); // outer gap around every button

        table.add(newMap);
        table.add(clear);
        table.row();
        table.add(savePNG);
        table.add(loadPNG);
        table.row();
        table.add(saveJSON);
        table.add(loadJSON);
        table.row();
        table.add(save);
        table.add(exit);
        add(table).grow().center().colspan(2);
        row();

        // Zoom slider.
        Label  zoomLbl    = new Label("Zoom: 1.00x", skin);
        Slider zoomSlider = new Slider(.5f, 8f, 0.1f, false, skin);
        zoomSlider.setValue(1f);

        zoomSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float z = zoomSlider.getValue();
                grid.setZoom(z);
                zoomLbl.setText(String.format("Zoom: %.2fx", z));
            }
        });

        add(zoomLbl).left().row();
        add(zoomSlider).grow().colspan(2);
        row().space(10);

        // Tile palette.
        add(new Label("Tiles:", skin)).left().row();
        Table      palette = buildPaletteTable(skin, atlas, grid);
        ScrollPane pane    = new ScrollPane(palette, skin);
        add(pane).grow().colspan(2).row();

        // Button actions.
        newMap.addListener(click(() -> {
            int w = parseInt(widthField.getText(), 64), h = parseInt(heightField.getText(), 64);
            grid.setMapSize(w, h);
        }));
        clear.addListener(click(grid::clear));
        savePNG.addListener(click(() -> {
            FileHandle fh = FileChooser.desktopSave("png");
            if (fh != null) writePNG(fh, grid.toPixmap());
        }));
        loadPNG.addListener(click(() -> {
            FileHandle fh = FileChooser.desktopOpen("png");
            if (fh != null) {
                Pixmap p = new Pixmap(fh);
                grid.loadFromPixmap(p);
                p.dispose();
            }
        }));
    }

    private Table buildPaletteTable(Skin skin, TextureAtlas atlas, EditorGrid grid) {
        Table t = new Table();
        t.defaults().pad(2);
        IntIntMap map = new IntIntMap();                      // color to column index
        map.put(TileLoader.COLOR_GRASS, 0);
        map.put(TileLoader.COLOR_HEDGE, 1);
        map.put(TileLoader.COLOR_WATER, 2);
        map.put(TileLoader.COLOR_WALL, 3);
        map.put(TileLoader.COLOR_WALLALT, 4);
        map.put(TileLoader.COLOR_FLOOR, 5);
        map.put(TileLoader.COLOR_VOID, 6);
        for (IntIntMap.Entry e : map.entries()) {
            int           color = e.key;
            TextureRegion tr    = atlas.findRegion(color == TileLoader.COLOR_WALLALT ? "wall_alt" : color == TileLoader.COLOR_WALL ? "wall" : color == TileLoader.COLOR_GRASS ? "grass" : color == TileLoader.COLOR_HEDGE ? "hedge" : color == TileLoader.COLOR_WATER ? "water" : color == TileLoader.COLOR_FLOOR ? "floor" : "void_tile");
            Image         img   = new Image(tr);
            img.setScaling(Scaling.stretch);
            img.addListener(click(() -> grid.currentColor = color));

            t.add(img).size(16 * 12).pad(40);

            // Two icons per row.
            if ((e.value + 1) % 2 == 0) t.row();
        }
        return t;
    }

    private ClickListener click(Runnable r) {
        return new ClickListener() {
            public void clicked(InputEvent e, float x, float y) {
                r.run();
            }
        };
    }

    private int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException ignore) {
            return fallback;
        }
    }

    @Override
    public float getMinWidth() {
        return 250f;
    }

    @Override
    public float getPrefWidth() {
        return 250f;
    }
}
