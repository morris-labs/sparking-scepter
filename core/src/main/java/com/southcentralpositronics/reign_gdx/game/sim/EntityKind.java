package com.southcentralpositronics.reign_gdx.game.sim;

/** The kinds of entity that a simulation reports. */
public enum EntityKind {
    PLAYER,
    DUMMY,
    CHASER,
    ASTAR_CHASER,
    SHOOTER,
    SPELL,
    PARTICLE;

    /** Checks whether this kind is a mob: the player or an enemy. */
    public boolean isMob() {
        return this == PLAYER || isEnemy();
    }

    /** Checks whether this kind is an enemy: a mob that isn't the player. */
    public boolean isEnemy() {
        return this == DUMMY || this == CHASER || this == ASTAR_CHASER || this == SHOOTER;
    }
}
