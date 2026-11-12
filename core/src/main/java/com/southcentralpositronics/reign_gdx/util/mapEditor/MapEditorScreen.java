package com.southcentralpositronics.reign_gdx.util.mapEditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/** The editor screen: the grid on the left and the sidebar on the right. */
public class MapEditorScreen extends ScreenAdapter {
    private final Skin          skin;
    private final Stage         uiStage;
    private final EditorGrid    grid;
    private final EditorSidebar sidebar;

    public MapEditorScreen() {
        uiStage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/default/uiskin.json"));

        TextureAtlas tileAtlas = new TextureAtlas(Gdx.files.internal("atlas/Tiles.atlas"));

        grid = new EditorGrid(tileAtlas);

        sidebar = new EditorSidebar(skin, tileAtlas, grid);
        sidebar.setBackground(skin.newDrawable("white", 0.2f, 0.2f, 0.2f, 1f));

        SplitPane split = new SplitPane(grid, sidebar, false, skin); // false: side by side
        split.setSplitAmount(0.66f);

        Table root = new Table();
        root.setFillParent(true);
        root.add(split).grow().top();
        uiStage.addActor(root);

        Gdx.input.setInputProcessor(uiStage);
    }

    @Override
    public void render(float dt) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        uiStage.act(dt);
        uiStage.draw();
    }

    @Override
    public void resize(int w, int h) {
        uiStage.getViewport().update(w, h, true);
    }

    @Override
    public void dispose() {
        uiStage.dispose();
    }
}
