package com.southcentralpositronics.reign_gdx.game.sim;

/** The kinds of entity that a simulation reports. */
public enum EntityKind {
    PLAYER(Role.PLAYER),
    DUMMY(Role.ENEMY),
    CHASER(Role.ENEMY),
    ASTAR_CHASER(Role.ENEMY),
    SHOOTER(Role.ENEMY),
    SPELL(Role.OTHER),
    PARTICLE(Role.OTHER);

    /** What part a kind of entity plays in a run. */
    private enum Role {
        PLAYER, ENEMY, OTHER
    }

    private final Role role;

    EntityKind(Role role) {
        this.role = role;
    }

    /** Checks whether this kind is a mob: the player or an enemy. */
    public boolean isMob() {
        return role != Role.OTHER;
    }

    /** Checks whether this kind is an enemy: a mob that isn't the player. */
    public boolean isEnemy() {
        return role == Role.ENEMY;
    }
}
