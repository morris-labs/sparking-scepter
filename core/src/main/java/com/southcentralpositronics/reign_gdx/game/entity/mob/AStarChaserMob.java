package com.southcentralpositronics.reign_gdx.game.entity.mob;

import com.southcentralpositronics.reign_gdx.engine.level.tile.Node;
import com.southcentralpositronics.reign_gdx.engine.time.FixedTimestep;
import com.southcentralpositronics.reign_gdx.engine.util.Vector2i;
import com.southcentralpositronics.reign_gdx.game.sim.EntityKind;

import java.util.List;

/** An enemy that follows an A* path to the player, around walls. */
public class AStarChaserMob extends Mob {
    /**
     * The walking speed along each axis, in pixels per second. The frame-based
     * version asked for 0.5 px per frame, but its pixel stepping rounded that up
     * to a whole pixel, so the A* chaser always moved 60 px per second.
     */
    public static final double SPEED                = 60;
    /** The time between path searches, in seconds. */
    private static final float PATH_REFRESH_SECONDS = 0.1f;

    private double     xa        = 0;
    private double     ya        = 0;
    private List<Node> path      = null;
    private float      pathTimer = 0f; // seconds since the last path

    public AStarChaserMob(int x, int y) {
        placeOnTile(x, y);
        this.type = Type.ENEMY;
    }

    @Override
    public EntityKind kind() {
        return EntityKind.ASTAR_CHASER;
    }

    private void calculatePath() {
        PlayerMob player = gameLevel.getClientPlayer();
        if (player == null) {
            path = null;
            return;
        }
        Vector2i startVec = new Vector2i(getTileX(), getTileY());
        Vector2i destVec  = new Vector2i(player.getTileX(), player.getTileY());
        path = level.findPath(startVec, destVec);
    }

    private void moveAlongPath() {
        double step = FixedTimestep.perTick(SPEED);
        xa = 0;
        ya = 0;

        if (path != null && !path.isEmpty()) {
            // Head for the spot where placeOnTile() would put this mob.
            Vector2i target  = path.getLast().tile;
            double   targetX = target.x * tileSize - paddingLeft;
            double   targetY = target.y * tileSize - paddingBottom;

            if (x < targetX) xa += step;
            if (x > targetX) xa -= step;
            if (y < targetY) ya += step;
            if (y > targetY) ya -= step;
        }

        if (xa != 0 || ya != 0) {
            move(xa, ya);
            walking = true;
        } else {
            walking = false;
        }
    }

    @Override
    public void update(float delta) {
        pathTimer += delta;

        if (pathTimer >= PATH_REFRESH_SECONDS) {
            calculatePath();
            pathTimer = 0f;
        }

        moveAlongPath();
        faceMovement(xa, ya);
        updateAnimation(delta);
    }
}
