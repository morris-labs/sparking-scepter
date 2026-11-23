package com.southcentralpositronics.reign_gdx.game.entity.mob;

import com.southcentralpositronics.reign_gdx.engine.input.PlayerInput;
import com.southcentralpositronics.reign_gdx.engine.time.FixedTimestep;
import com.southcentralpositronics.reign_gdx.game.entity.projectile.Projectile;
import com.southcentralpositronics.reign_gdx.game.sim.EntityKind;
import com.southcentralpositronics.reign_gdx.game.sim.SimulationEvent;

/** The player: moves and casts spells as its {@link PlayerInput} asks. */
public class PlayerMob extends Mob {
    /** The walking speed along each axis, in pixels per second. */
    public static final double SPEED             = 120;
    /** The health the player loses per enemy hit, as in the Java2D version. */
    private static final double PLAYER_HIT_DAMAGE = 0.01;

    private PlayerInput input = PlayerInput.NONE;
    private long        kills = 0;

    /**
     * Creates the player with its collision box on a tile.
     *
     * @param tx The tile column.
     * @param ty The tile row.
     */
    public PlayerMob(int tx, int ty) {
        placeOnTile(tx, ty);
        this.type = Type.PLAYER;
    }

    @Override
    public EntityKind kind() {
        return EntityKind.PLAYER;
    }

    /** Sets the input that the player follows from the next tick on. */
    public void setInput(PlayerInput input) {
        this.input = input;
    }

    @Override
    public void update(float delta) {
        double step = FixedTimestep.perTick(SPEED);
        double xa   = input.moveX() * step;
        double ya   = input.moveY() * step;

        walking = xa != 0 || ya != 0;
        if (walking) move(xa, ya);
        if (shootCooldown > 0f) shootCooldown -= delta;
        updateAnimation(delta);
        if (input.casting()) castAt(input.aimX(), input.aimY());
    }

    /** Casts a spell toward a world point, at most once every {@link Projectile#SHOT_DELAY} seconds. */
    private void castAt(double aimX, double aimY) {
        if (shootCooldown > 0f) return;
        shootAt(Math.atan2(aimY - getBodyCenterY(), aimX - getBodyCenterX()));
        // The player casts as often as the enemies do.
        shootCooldown = Projectile.SHOT_DELAY;
    }

    /**
     * Called when this player kills an enemy. The kill count goes up by one,
     * and the player heals by half, up to full health. The Java2D version
     * didn't cap the healing.
     */
    public void scoredKill() {
        kills++;
        health = Math.min(1.0, health + 0.5);
    }

    /**
     * Takes {@link #PLAYER_HIT_DAMAGE} health per enemy hit. As in the Java2D
     * version, health stops at zero and the player doesn't die.
     */
    @Override
    protected void onHit(Projectile projectile) {
        health = Math.max(0.0, health - PLAYER_HIT_DAMAGE);
        gameLevel.raise(SimulationEvent.Type.PLAYER_DAMAGE, kind(), getBodyCenterX(), getBodyCenterY());
    }

    /** Gets the number of enemies this player has killed. */
    public long getKills() {
        return kills;
    }
}
