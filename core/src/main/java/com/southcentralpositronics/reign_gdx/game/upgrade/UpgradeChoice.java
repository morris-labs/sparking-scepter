package com.southcentralpositronics.reign_gdx.game.upgrade;

import com.badlogic.ashley.core.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The upgrades of one run: the offers waiting for a choice, and the upgrades
 * taken so far. The simulation owns one per run, so a new run starts with
 * none.
 */
public final class UpgradeChoice {
    private final Random        random;
    private final List<Upgrade> offers = new ArrayList<>();
    private final List<Upgrade> taken  = new ArrayList<>();

    /**
     * Creates an empty choice.
     *
     * @param random The source that draws the offers: the level's random source, so the seed decides them.
     */
    public UpgradeChoice(Random random) {
        this.random = random;
    }

    /** Draws {@link Upgrade#OFFER_COUNT} different upgrades from the table and waits for a choice. */
    public void offer() {
        List<Upgrade> table = new ArrayList<>(Arrays.asList(Upgrade.values()));
        Collections.shuffle(table, random);
        offers.clear();
        offers.addAll(table.subList(0, Math.min(Upgrade.OFFER_COUNT, table.size())));
    }

    /** Checks whether offers are waiting for a choice. */
    public boolean isPending() {
        return !offers.isEmpty();
    }

    /** Gets the upgrades on offer, or an empty list when no choice is waiting. */
    public List<Upgrade> getOffers() {
        return List.copyOf(offers);
    }

    /** Gets the upgrades taken this run, in the order they were taken. */
    public List<Upgrade> getTaken() {
        return List.copyOf(taken);
    }

    /**
     * Takes one of the offered upgrades, applies it to the player, and ends the choice.
     *
     * @param upgrade The upgrade to take.
     * @param player  The player entity to apply it to.
     * @throws IllegalArgumentException If the upgrade isn't on offer, including when no choice is waiting.
     */
    public void choose(Upgrade upgrade, Entity player) {
        if (!offers.contains(upgrade)) throw new IllegalArgumentException(upgrade + " isn't on offer");
        upgrade.apply(player);
        taken.add(upgrade);
        offers.clear();
    }
}
