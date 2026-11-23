package com.southcentralpositronics.reign_gdx.game.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import java.util.List;

/** Builds the centered title-and-buttons layout that the main menu, pause, and game-over screens share. */
final class Menu {
    /** One menu button: its text and what it does when clicked. */
    record Item(String text, Runnable action) {
    }

    /** One line of text under the title, such as the score. */
    record Line(String text, Color color) {
        /** Makes a line in the skin's default white. */
        Line(String text) {
            this(text, Color.WHITE);
        }
    }

    /** The color of a line that announces something good, such as a new high score. */
    static final Color HIGHLIGHT = new Color(0xffd75eff);

    private static final float TITLE_SCALE   = 4f;
    private static final float LINE_SCALE    = 2f;
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
        return build(skin, title, List.of(), items);
    }

    /**
     * Builds a table that fills its parent, with the title above lines of text
     * and then a column of buttons.
     *
     * @param skin  The skin for the labels and buttons.
     * @param title The text at the top of the menu.
     * @param lines The lines of text under the title, from top to bottom.
     * @param items The buttons, from top to bottom.
     * @return The table, ready to add to a stage.
     */
    static Table build(Skin skin, String title, List<Line> lines, Item... items) {
        Table table = new Table(skin);
        table.setFillParent(true);
        table.center();

        Label titleLabel = new Label(title, skin);
        titleLabel.setFontScale(TITLE_SCALE);
        table.add(titleLabel).padBottom(lines.isEmpty() ? 48 : 32).row();

        for (int i = 0; i < lines.size(); i++) {
            Line  line  = lines.get(i);
            Label label = new Label(line.text(), skin);
            label.setFontScale(LINE_SCALE);
            label.setColor(line.color());
            table.add(label).padBottom(i == lines.size() - 1 ? 40 : 12).row();
        }

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
