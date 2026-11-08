package com.southcentralpositronics.reign_gdx.util.mapEditor;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

public class MapEditorApp extends Game {

    @Override
    public void create() {
        Screen editor = new MapEditorScreen();   // your existing class
        setScreen(editor);
    }

    @Override
    public void dispose() {
        super.dispose();                         // disposes the current screen
    }
}
