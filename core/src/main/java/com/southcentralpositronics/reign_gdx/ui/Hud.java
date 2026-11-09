package com.southcentralpositronics.reign_gdx.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.southcentralpositronics.reign_gdx.entity.mob.PlayerMob;
import com.southcentralpositronics.reign_gdx.level.Level;

/**
 * The side panel ported from the Java2D Reign player UI: a minimap, the player's name, a
 * health bar, the score, buttons that add and clear enemies, and an exit
 * button. It fills a strip {@link #PANEL_WIDTH} pixels wide on the right of
 * the window, and the game draws in the rest.
 */
public class Hud implements Disposable {
    /** The width of the panel, in screen pixels. */
    public static final int PANEL_WIDTH = 256;

    private static final Color PANEL_COLOR      = new Color(0x4f4f4fff);
    private static final Color HEALTH_COLOR     = new Color(0x870000ff);
    private static final Color HEALTH_BG_COLOR  = Color.BLACK;
    private static final Color NAME_COLOR       = new Color(0xbbbbbbff);
    private static final int   POINTS_PER_KILL  = 100;

    private final Stage       stage;
    private final Skin        skin;
    private final Texture     exitTexture;
    private final PlayerMob   player;
    private final ProgressBar healthBar;
    private final Label       scoreLabel;
    private final Cell<Actor> topSlot;
    private final Minimap     minimap;
    private       long        shownScore = -1;

    /**
     * Builds the panel.
     *
     * @param player The player whose name, health, and score the panel shows.
     * @param level  The level that the Add Mobs and Clear Mobs buttons act on.
     */
    public Hud(PlayerMob player, Level level) {
        this.player = player;
        stage       = new Stage(new ScreenViewport());
        skin        = new Skin(Gdx.files.internal("ui/default/uiskin.json"));
        exitTexture = new Texture(Gdx.files.internal("ui/home.png"));

        Table panel = new Table(skin);
        panel.setBackground(skin.newDrawable("white", PANEL_COLOR));
        // Catch clicks on the panel background so they don't fire spells.
        panel.setTouchable(Touchable.enabled);
        panel.top().pad(10).defaults().left().padBottom(8);

        topSlot = panel.add((Actor) null).center().padBottom(12);
        panel.row();

        Label name = new Label(player.getName(), skin);
        name.setColor(NAME_COLOR);
        name.setFontScale(1.5f);
        panel.add(name).row();

        healthBar = new ProgressBar(0f, 1f, 0.001f, false, healthBarStyle());
        healthBar.setAnimateDuration(0f);
        Label hpLabel = new Label(" HP", skin);
        hpLabel.setFontScale(1.25f);
        Stack health = new Stack(healthBar, hpLabel);
        panel.add(health).growX().height(24).row();

        scoreLabel = new Label("", skin);
        scoreLabel.setFontScale(1.25f);
        panel.add(scoreLabel).padTop(4).row();

        TextButton addMobs = new TextButton("Add Mobs", skin);
        addMobs.addListener(changed(level::generateMobs));
        panel.add(addMobs).width(140).height(40).padTop(12).row();

        TextButton clearMobs = new TextButton("Clear Mobs", skin);
        clearMobs.addListener(changed(level::clearMobs));
        panel.add(clearMobs).width(140).height(40).row();

        ImageButton exit = new ImageButton(new TextureRegionDrawable(exitTexture));
        exit.getImageCell().size(48);
        exit.addListener(changed(Gdx.app::exit));
        panel.add(exit).padTop(12).row();

        Table root = new Table();
        root.setFillParent(true);
        root.right().add(panel).width(PANEL_WIDTH).growY();
        stage.addActor(root);

        minimap = new Minimap(level);
        setTopActor(minimap);
        update(0f);
    }

    private ProgressBar.ProgressBarStyle healthBarStyle() {
        Drawable background = skin.newDrawable("white", HEALTH_BG_COLOR);
        Drawable fill       = skin.newDrawable("white", HEALTH_COLOR);
        background.setMinHeight(24);
        fill.setMinHeight(24);
        fill.setMinWidth(0);
        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
        style.background = background;
        style.knobBefore = fill;
        return style;
    }

    private static ChangeListener changed(Runnable action) {
        return new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        };
    }

    /**
     * Puts an actor, such as the minimap, at the top of the panel.
     *
     * @param actor The actor to show; null clears the slot.
     */
    public void setTopActor(Actor actor) {
        topSlot.setActor(actor);
    }

    /** Gets the stage, which must come first in the input chain. */
    public Stage getStage() {
        return stage;
    }

    /**
     * Updates the health bar and score from the player and runs the panel's
     * actions.
     *
     * @param delta The time since the last frame, in seconds.
     */
    public void update(float delta) {
        healthBar.setValue((float) player.getHealth());
        if (player.getScore() != shownScore) {
            shownScore = player.getScore();
            scoreLabel.setText("Score: " + shownScore * POINTS_PER_KILL);
        }
        stage.act(delta);
    }

    /** Draws the panel. Call it after the game, because it sets its own GL viewport. */
    public void draw() {
        stage.getViewport().apply();
        stage.draw();
    }

    /**
     * Resizes the panel to the window.
     *
     * @param width  The window width, in pixels.
     * @param height The window height, in pixels.
     */
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        exitTexture.dispose();
        minimap.dispose();
    }
}
