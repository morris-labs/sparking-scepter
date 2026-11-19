package com.southcentralpositronics.reign_gdx.game.entity.projectile;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.southcentralpositronics.reign_gdx.engine.entity.Entity;

/** A fireball that flies 150 px at 5 px per frame. */
public class SpellProjectile extends Projectile {

    public SpellProjectile(double x, double y, double angle, Entity firedBy, TextureAtlas atlas) {
        super(x, y, angle);
        this.firedBy = firedBy;

        texture    = atlas.findRegion("fireball");
        this.range = 150;
        this.speed = 5;
    }
}
