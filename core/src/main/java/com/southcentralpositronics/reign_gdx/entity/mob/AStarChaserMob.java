package com.southcentralpositronics.reign_gdx.entity.mob;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.southcentralpositronics.reign_gdx.level.tile.Node;
import com.southcentralpositronics.reign_gdx.util.Vector2i;

import java.util.List;

/** An enemy that follows an A* path to the player, around walls. */
public class AStarChaserMob extends Mob {
    private       double     xa        = 0;
    private       double     ya        = 0;
    private final double     speed     = 0.5;
    private       List<Node> path      = null;
    private       float      pathTimer = 0f; // seconds since the last path

    public AStarChaserMob(int x, int y, TextureAtlas atlas) {
        loadAnimations(atlas, ENEMY_SPRITE);
        placeOnTile(x, y);
        this.type = Type.ENEMY;
    }

    private void calculatePath() {
        PlayerMob player = level.getClientPlayer();
        if (player == null) {
            path = null;
            return;
        }
        Vector2i startVec = new Vector2i(getTileX(), getTileY());
        Vector2i destVec  = new Vector2i(player.getTileX(), player.getTileY());
        path = level.findPath(startVec, destVec);
    }

    private void moveAlongPath() {
        xa = 0;
        ya = 0;

        if (path != null && !path.isEmpty()) {
            // Head for the spot where placeOnTile() would put this mob.
            Vector2i target  = path.getLast().tile;
            double   targetX = target.x * tileSize - paddingLeft;
            double   targetY = target.y * tileSize - paddingBottom;

            if (x < targetX) xa += speed;
            if (x > targetX) xa -= speed;
            if (y < targetY) ya += speed;
            if (y > targetY) ya -= speed;
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

        // Recalculate the path 10 times per second.
        if (pathTimer >= 0.1f) {
            calculatePath();
            pathTimer = 0f;
        }

        moveAlongPath();

        if (Math.abs(ya) > Math.abs(xa)) {
            direction = ya < 0 ? Direction.UP : Direction.DOWN;
        } else if (xa != 0) {
            direction = xa < 0 ? Direction.LEFT : Direction.RIGHT;
        }

        super.updateAnimation(delta, walking, direction);
    }
}
