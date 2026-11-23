package com.southcentralpositronics.reign_gdx.engine.pathfinding;

import com.southcentralpositronics.reign_gdx.engine.level.Level;
import com.southcentralpositronics.reign_gdx.engine.util.Vector2i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Finds tile paths through a level with A*, ported from the Java2D
 * {@code Level.findPath()}. AI systems call it; it keeps no state between
 * calls.
 *
 * <p>A mob's collision box fits in one tile, so any tile that doesn't block
 * movement is a valid step. Diagonal steps cost the square root of two and
 * can't cut a wall corner. The Java2D version costed diagonals at 0.95, which
 * made paths zigzag, and let them cut corners.
 */
public final class AStarPathfinder {
    private final Level level;

    /**
     * Creates a pathfinder for one level.
     *
     * @param level The level whose walkable tiles paths cross. The pathfinder
     *              reads {@link Level#isWalkable(int, int)} on every search,
     *              so a change to the tiles takes effect on the next path.
     */
    public AStarPathfinder(Level level) {
        this.level = level;
    }

    /**
     * Finds the shortest path between two tiles.
     *
     * @param start The tile to start from.
     * @param goal  The tile to reach.
     * @return The tiles to visit in order, from the first step to the goal. The
     *     start tile isn't included. The list is empty when no path exists or
     *     when the start is the goal.
     */
    public List<Vector2i> findPath(Vector2i start, Vector2i goal) {
        int width  = level.getWidth();
        int height = level.getHeight();
        List<Vector2i> path = new ArrayList<>();
        if (!inBounds(start.x, start.y, width, height) || !level.isWalkable(goal.x, goal.y)) return path;

        double[] bestCost = new double[width * height];
        Arrays.fill(bestCost, Double.POSITIVE_INFINITY);
        boolean[]           closed = new boolean[width * height];
        PriorityQueue<Node> open   = new PriorityQueue<>(Comparator.comparingDouble(Node::fCost));

        open.add(new Node(start, null, 0, Vector2i.getDistance(start, goal)));
        bestCost[start.y * width + start.x] = 0;

        while (!open.isEmpty()) {
            Node current = open.poll();
            int  index   = current.tile.y * width + current.tile.x;
            if (closed[index]) continue;    // A cheaper copy of this tile was expanded already.
            closed[index] = true;

            if (current.tile.equals(goal)) {
                for (Node node = current; node.parent != null; node = node.parent) path.add(node.tile);
                Collections.reverse(path);
                return path;
            }

            for (int i = 0; i < 9; i++) {
                if (i == 4) continue;       // The current tile.
                int xDir = (i % 3) - 1;
                int yDir = (i / 3) - 1;
                int nx   = current.tile.x + xDir;
                int ny   = current.tile.y + yDir;

                if (!level.isWalkable(nx, ny)) continue;
                if (xDir != 0 && yDir != 0) {
                    // Don't cut corners: both orthogonal neighbours must be open too.
                    if (!level.isWalkable(current.tile.x + xDir, current.tile.y)) continue;
                    if (!level.isWalkable(current.tile.x, current.tile.y + yDir)) continue;
                }

                int nIndex = ny * width + nx;
                if (closed[nIndex]) continue;
                double gCost = current.gCost + (xDir != 0 && yDir != 0 ? Math.sqrt(2) : 1);
                if (gCost >= bestCost[nIndex]) continue;
                bestCost[nIndex] = gCost;
                Vector2i next = new Vector2i(nx, ny);
                open.add(new Node(next, current, gCost, Vector2i.getDistance(next, goal)));
            }
        }
        return path;
    }

    private static boolean inBounds(int tx, int ty, int width, int height) {
        return tx >= 0 && ty >= 0 && tx < width && ty < height;
    }

    /** One tile on the open list, with the cost to reach it and an estimate of the cost to the goal. */
    private record Node(Vector2i tile, Node parent, double gCost, double hCost) {
        double fCost() {
            return gCost + hCost;
        }
    }
}
