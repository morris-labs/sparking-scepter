package com.southcentralpositronics.reign_gdx.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
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
import com.southcentralpositronics.reign_gdx.game.sim.Simulation;
import com.southcentralpositronics.reign_gdx.game.upgrade.Upgrade;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The side panel ported from the Java2D Reign player UI: a minimap, the
 * player's name, a health bar, the score, the wave number, the enemies left,
 * the countdown during a break, the upgrades taken this run, and an exit button. With the debug tools on,
 * it also has buttons that add and clear enemies. It fills a strip {@link #PANEL_WIDTH} pixels wide on the right of
 * the window, and the game draws in the rest.
 *
 * <p>When a break offers upgrades, a box in the middle of the game area shows
 * them as buttons; a click or the keys 1 to 3 choose one.
 */
public class Hud implements Disposable {
    /** The width of the panel, in screen pixels. */
    public static final int PANEL_WIDTH = 256;

    private static final Color PANEL_COLOR      = new Color(0x4f4f4fff);
    private static final Color HEALTH_COLOR     = new Color(0x870000ff);
    private static final Color HEALTH_BG_COLOR  = Color.BLACK;
    private static final Color NAME_COLOR       = new Color(0xbbbbbbff);
    private static final Color BREAK_COLOR      = new Color(0xffd75eff);
    private static final Color CHOICE_BG_COLOR  = new Color(0x000000cc);
    /** The font scale of the player's name and the upgrade box title. */
    private static final float NAME_SCALE       = 1.5f;
    /** The font scale of the panel's other labels. */
    private static final float LABEL_SCALE      = 1.25f;

    private final Stage       stage;
    private final Skin        skin;
    private final Texture     exitTexture;
    private final Simulation  simulation;
    private final ProgressBar healthBar;
    private final Label       scoreLabel;
    private final Label       waveLabel;
    private final Label       enemiesLabel;
    private final Label       breakLabel;
    private final Label       upgradesLabel;
    private final Table       choice;
    private final TextButton[] offerButtons = new TextButton[Upgrade.OFFER_COUNT];
    private final Cell<Actor> topSlot;
    private final Minimap     minimap;
    private       long        shownScore   = -1;
    private       String      shownWave    = null;
    private       int         shownEnemies = -1;
    private       String      shownBreak   = null;
    private       List<Upgrade> shownOffers = null;
    private       int         shownTaken   = -1;

    /**
     * Builds the panel.
     *
     * @param simulation The simulation whose player health, score, and waves the
     *                   panel shows, and that the Add Mobs and Clear Mobs buttons act on.
     * @param playerName The name to show for the player.
     * @param debug      Whether to show the Add Mobs and Clear Mobs buttons.
     */
    public Hud(Simulation simulation, String playerName, boolean debug) {
        this.simulation = simulation;
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

        addLabel(panel, playerName, NAME_SCALE).setColor(NAME_COLOR);

        healthBar = new ProgressBar(0f, 1f, 0.001f, false, healthBarStyle());
        healthBar.setAnimateDuration(0f);
        Label hpLabel = new Label(" HP", skin);
        hpLabel.setFontScale(LABEL_SCALE);
        Stack health = new Stack(healthBar, hpLabel);
        panel.add(health).growX().height(24).row();

        scoreLabel = addLabel(panel, "", LABEL_SCALE);
        panel.getCell(scoreLabel).padTop(4);
        waveLabel    = addLabel(panel, "", LABEL_SCALE);
        enemiesLabel = addLabel(panel, "", LABEL_SCALE);
        breakLabel   = addLabel(panel, "", LABEL_SCALE);
        breakLabel.setColor(BREAK_COLOR);

        upgradesLabel = new Label("", skin);
        upgradesLabel.setWrap(true);
        panel.add(upgradesLabel).growX().padTop(4).row();

        if (debug) {
            TextButton addMobs = new TextButton("Add Mobs", skin);
            addMobs.addListener(changed(simulation::addEnemies));
            panel.add(addMobs).width(140).height(40).padTop(12).row();

            TextButton clearMobs = new TextButton("Clear Mobs", skin);
            clearMobs.addListener(changed(simulation::clearEnemies));
            panel.add(clearMobs).width(140).height(40).row();
        }

        ImageButton exit = new ImageButton(new TextureRegionDrawable(exitTexture));
        exit.getImageCell().size(48);
        exit.addListener(changed(Gdx.app::exit));
        panel.add(exit).padTop(12).row();

        Table root = new Table();
        root.setFillParent(true);
        choice = buildChoice();
        root.add(choice).expand().center();
        root.add(panel).width(PANEL_WIDTH).growY();
        stage.addActor(root);
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                int index = keycode - Input.Keys.NUM_1;
                if (index < 0 || index >= Upgrade.OFFER_COUNT) return false;
                return chooseOffer(index);
            }
        });

        minimap = new Minimap(simulation);
        setTopActor(minimap);
        update(0f);
    }

    /** Builds the box that shows the upgrades on offer, one button each; it's hidden until a break offers some. */
    private Table buildChoice() {
        Table box = new Table(skin);
        box.setBackground(skin.newDrawable("white", CHOICE_BG_COLOR));
        box.setTouchable(Touchable.enabled);
        box.pad(16).defaults().width(320).padBottom(8);

        Label title = new Label("Choose an upgrade", skin);
        title.setColor(BREAK_COLOR);
        title.setFontScale(NAME_SCALE);
        box.add(title).center().padBottom(12).row();

        for (int i = 0; i < offerButtons.length; i++) {
            int index = i;
            offerButtons[i] = new TextButton("", skin);
            offerButtons[i].getLabel().setFontScale(LABEL_SCALE);
            offerButtons[i].pad(8);
            offerButtons[i].addListener(changed(() -> chooseOffer(index)));
            box.add(offerButtons[i]).height(64).row();
        }
        box.setVisible(false);
        return box;
    }

    /**
     * Takes the upgrade at a place in the offers, if a choice is waiting.
     *
     * @param index The place in the offers, from 0.
     * @return True if an upgrade was taken.
     */
    private boolean chooseOffer(int index) {
        List<Upgrade> offers = simulation.getUpgradeOffers();
        if (index >= offers.size() || simulation.isRunOver()) return false;
        simulation.chooseUpgrade(offers.get(index));
        return true;
    }

    /**
     * Adds a scaled label to a table on a row of its own.
     *
     * @param table The table to add the label to.
     * @param text  The label's first text.
     * @param scale The font scale.
     * @return The label.
     */
    private Label addLabel(Table table, String text, float scale) {
        Label label = new Label(text, skin);
        label.setFontScale(scale);
        table.add(label).row();
        return label;
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
     * Updates the health bar, score, and wave labels from the simulation and
     * runs the panel's actions.
     *
     * @param delta The time since the last frame, in seconds.
     */
    public void update(float delta) {
        healthBar.setValue((float) (simulation.getPlayer().health() / simulation.getPlayerMaxHealth()));
        if (simulation.getScore() != shownScore) {
            shownScore = simulation.getScore();
            scoreLabel.setText("Score: " + shownScore);
        }
        String wave = "Wave: " + Math.max(1, simulation.getWave());
        if (!wave.equals(shownWave)) {
            shownWave = wave;
            waveLabel.setText(wave);
        }
        if (simulation.getEnemyCount() != shownEnemies) {
            shownEnemies = simulation.getEnemyCount();
            enemiesLabel.setText("Enemies left: " + shownEnemies);
        }
        String countdown = breakText();
        if (!countdown.equals(shownBreak)) {
            shownBreak = countdown;
            breakLabel.setText(countdown);
        }
        updateUpgrades();
        stage.act(delta);
    }

    /** Shows the offers while a choice is waiting, and lists the upgrades taken. */
    private void updateUpgrades() {
        List<Upgrade> offers = simulation.isRunOver() ? List.of() : simulation.getUpgradeOffers();
        if (!offers.equals(shownOffers)) {
            shownOffers = offers;
            for (int i = 0; i < offerButtons.length; i++) {
                boolean offered = i < offers.size();
                offerButtons[i].setVisible(offered);
                if (offered) {
                    Upgrade upgrade = offers.get(i);
                    offerButtons[i].setText((i + 1) + ". " + upgrade.getTitle() + "\n" + upgrade.getDescription());
                }
            }
            choice.setVisible(!offers.isEmpty());
        }

        List<Upgrade> taken = simulation.getUpgradesTaken();
        if (taken.size() != shownTaken) {
            shownTaken = taken.size();
            upgradesLabel.setText(upgradesText(taken));
        }
    }

    /** Lists the upgrades taken, one per line, with a count for each taken more than once. */
    private static String upgradesText(List<Upgrade> taken) {
        if (taken.isEmpty()) return "";
        Map<Upgrade, Integer> counts = new EnumMap<>(Upgrade.class);
        for (Upgrade upgrade : taken) counts.merge(upgrade, 1, Integer::sum);
        StringBuilder text = new StringBuilder("Upgrades:");
        counts.forEach((upgrade, count) -> {
            text.append("\n  ").append(upgrade.getTitle());
            if (count > 1) text.append(" x").append(count);
        });
        return text.toString();
    }

    /** Gets the countdown text: whole seconds to the next wave, rounded up, or nothing outside a break. */
    private String breakText() {
        if (!simulation.isBreak()) return "";
        if (simulation.isUpgradePending()) return "Choose an upgrade";
        int seconds = (int) Math.ceil(simulation.getBreakSecondsLeft());
        return "Wave " + (simulation.getWave() + 1) + " in " + seconds;
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
