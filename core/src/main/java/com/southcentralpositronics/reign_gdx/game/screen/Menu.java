package com.southcentralpositronics.reign_gdx.game.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/** Builds the centered title-and-buttons layout that the main menu and pause screen share. */
final class Menu {
    /** One menu button: its text and what it does when clicked. */
    record Item(String text, Runnable action) {
    }

    private static final float TITLE_SCALE   = 4f;
    private static final float BUTTON_SCALE  = 1.75f;
    private static final int   BUTTON_WIDTH  = 260;
    private static final int   BUTTON_HEIGHT = 56;

    private Menu() {
    }

    /**
     * Builds a table that fills its parent, with the title above a column of
     * buttons.
     *
     * @param skin  The skin for the labels and buttons.
     * @param title The text at the top of the menu.
     * @param items The buttons, from top to bottom.
     * @return The table, ready to add to a stage.
     */
    static Table build(Skin skin, String title, Item... items) {
        Table table = new Table(skin);
        table.setFillParent(true);
        table.center();

        Label titleLabel = new Label(title, skin);
        titleLabel.setFontScale(TITLE_SCALE);
        table.add(titleLabel).padBottom(48).row();

        for (Item item : items) {
            TextButton button = new TextButton(item.text(), skin);
            button.getLabel().setFontScale(BUTTON_SCALE);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    item.action().run();
                }
            });
            table.add(button).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(16).row();
        }
        return table;
    }
}
