package com.southcentralpositronics.reign_gdx.engine.level.tile;

/** The world pixel position of a tile's bottom-left corner. */
public class TileCoordinates {

	private final int x;
	private final int y;

	public TileCoordinates(int x, int y) {
		this.x = x * Tile.SIZE;
		this.y = y * Tile.SIZE;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
