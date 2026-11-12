package com.southcentralpositronics.reign_gdx.util.mapEditor;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

/** The map editor application. */
public class MapEditorApp extends Game {

    @Override
    public void create() {
        Screen editor = new MapEditorScreen();
        setScreen(editor);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
