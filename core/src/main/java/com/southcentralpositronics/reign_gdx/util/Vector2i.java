package com.southcentralpositronics.reign_gdx.util;

/** A pair of integers, used for tile positions in A*. */
public class Vector2i {

	public int x;
	public int y;

	public Vector2i(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/** Gets the straight-line distance between two points. */
	public static double getDistance(Vector2i start, Vector2i goal) {
		double dx = start.x - goal.x;
		double dy = start.y - goal.y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Vector2i vec)) return false;
		return vec.x == this.x && vec.y == this.y;
	}

	@Override
	public int hashCode() {
		return 31 * x + y;
	}
}
