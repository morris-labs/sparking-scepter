package com.southcentralpositronics.reign_gdx.game.entity;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.southcentralpositronics.reign_gdx.engine.entity.component.BodyComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.HealthComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.PositionComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.VelocityComponent;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;
import com.southcentralpositronics.reign_gdx.game.entity.component.AStarAiComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.CasterComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.ChaseAiComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.ContactDamageComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.EnemyComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.MobComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.PlayerComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.ShooterAiComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.WanderAiComponent;
import com.southcentralpositronics.reign_gdx.game.sim.EntityKind;

import java.util.Random;

/**
 * Builds the player and each enemy type from components, and adds them to the
 * engine. An enemy type is nothing more than the set of components its method
 * adds.
 *
 * <p>Each mob starts with its collision box in the bottom half of a tile: at
 * ({@code tx * 16 + 8}, {@code ty * 16 + 4}).
 */
public final class MobFactory {
    /** The player's walking speed along each axis, in pixels per second. */
    public static final double PLAYER_SPEED        = 120;
    /**
     * The Chaser's and A* Chaser's walking speed along each axis, in pixels per
     * second. The frame-based version asked for 0.8 and 0.5 px per frame, but
     * its pixel stepping rounded both up to a whole pixel, so they moved 60 px
     * per second.
     */
    public static final double CHASER_SPEED        = 60;
    /** The Dummy's and Shooter's wandering speed, in pixels per second. */
    public static final double WANDER_SPEED        = 60;
    /** The seconds between casts, for the player and Shooters alike: 8 casts per second. */
    public static final float  SHOT_DELAY          = 0.125f;
    /** The health that the player's spells take from an enemy, as in the Java2D ShooterMob: 10 hits kill. */
    public static final double PLAYER_SPELL_DAMAGE = 0.1;
    /** The health that an enemy's spell takes from the player, as in the Java2D version. */
    public static final double ENEMY_SPELL_DAMAGE  = 0.01;
    /** The health that a Chaser or A* Chaser takes from the player with each touch: 10 touches kill. */
    public static final double CONTACT_DAMAGE      = 0.1;
    /** The seconds between a Chaser's or A* Chaser's touches that hurt: a Chaser kills in 5 seconds. */
    public static final float  CONTACT_DELAY       = 0.5f;
    /** The farthest the player can be for a Chaser to chase, in pixels. */
    public static final double CHASE_RANGE         = 800;
    /** The farthest the player can be for a Shooter to cast, in pixels. */
    public static final double SHOOTER_RANGE       = 100;
    /** The seconds between an A* Chaser's path searches. */
    public static final float  PATH_REFRESH_SECONDS = 0.1f;
    /** The width of every mob's collision box, in pixels. */
    public static final int    BODY_WIDTH          = 16;
    /** The height of every mob's collision box, in pixels. */
    public static final int    BODY_HEIGHT         = 8;

    private final Engine engine;
    private final Random random;

    /**
     * Creates a factory.
     *
     * @param engine The engine to add mobs to.
     * @param random The source of each wandering mob's first direction: the
     *               level's random source, so the seed decides it.
     */
    public MobFactory(Engine engine, Random random) {
        this.engine = engine;
        this.random = random;
    }

    /**
     * Adds the player with its collision box on a tile.
     *
     * @param tx The tile column.
     * @param ty The tile row.
     * @return The player entity.
     */
    public Entity createPlayer(int tx, int ty) {
        Entity player = createMob(EntityKind.PLAYER, tx, ty);
        PlayerComponent control = engine.createComponent(PlayerComponent.class);
        control.speed = PLAYER_SPEED;
        player.add(control);
        player.add(caster(PLAYER_SPELL_DAMAGE));
        engine.addEntity(player);
        return player;
    }

    /**
     * Adds one enemy of a kind with its collision box on a tile.
     *
     * @param kind The kind of enemy.
     * @param tx   The tile column.
     * @param ty   The tile row.
     * @return The enemy entity.
     * @throws IllegalArgumentException If the kind isn't an enemy.
     */
    public Entity createEnemy(EntityKind kind, int tx, int ty) {
        return switch (kind) {
            case DUMMY -> createDummy(tx, ty);
            case CHASER -> createChaser(tx, ty);
            case ASTAR_CHASER -> createAStarChaser(tx, ty);
            case SHOOTER -> createShooter(tx, ty);
            default -> throw new IllegalArgumentException(kind + " isn't an enemy");
        };
    }

    /** Adds a Dummy: an enemy that wanders and never attacks. */
    public Entity createDummy(int tx, int ty) {
        Entity dummy = createEnemyBase(EntityKind.DUMMY, tx, ty);
        dummy.add(wander(0.5f, 1f));
        engine.addEntity(dummy);
        return dummy;
    }

    /** Adds a Chaser: an enemy that walks straight at the player, ignoring walls, and hurts it by touch. */
    public Entity createChaser(int tx, int ty) {
        Entity           chaser = createEnemyBase(EntityKind.CHASER, tx, ty);
        ChaseAiComponent chase  = engine.createComponent(ChaseAiComponent.class);
        chase.speed = CHASER_SPEED;
        chase.range = CHASE_RANGE;
        chaser.add(chase);
        chaser.add(contactDamage());
        engine.addEntity(chaser);
        return chaser;
    }

    /** Adds an A* Chaser: an enemy that follows an A* path to the player, around walls, and hurts it by touch. */
    public Entity createAStarChaser(int tx, int ty) {
        Entity           chaser = createEnemyBase(EntityKind.ASTAR_CHASER, tx, ty);
        AStarAiComponent ai     = engine.createComponent(AStarAiComponent.class);
        ai.speed          = CHASER_SPEED;
        ai.refreshSeconds = PATH_REFRESH_SECONDS;
        chaser.add(ai);
        chaser.add(contactDamage());
        engine.addEntity(chaser);
        return chaser;
    }

    /** Adds a Shooter: an enemy that wanders and casts at the player in range. */
    public Entity createShooter(int tx, int ty) {
        Entity             shooter = createEnemyBase(EntityKind.SHOOTER, tx, ty);
        ShooterAiComponent ai      = engine.createComponent(ShooterAiComponent.class);
        ai.range = SHOOTER_RANGE;
        shooter.add(wander(0.5f, 1.5f));
        shooter.add(ai);
        shooter.add(caster(ENEMY_SPELL_DAMAGE));
        engine.addEntity(shooter);
        return shooter;
    }

    private Entity createEnemyBase(EntityKind kind, int tx, int ty) {
        Entity enemy = createMob(kind, tx, ty);
        enemy.add(engine.createComponent(EnemyComponent.class));
        return enemy;
    }

    /** Builds the components that every mob has, without adding the entity to the engine. */
    private Entity createMob(EntityKind kind, int tx, int ty) {
        Entity entity = engine.createEntity();

        BodyComponent body = engine.createComponent(BodyComponent.class);
        body.width  = BODY_WIDTH;
        body.height = BODY_HEIGHT;

        PositionComponent position = engine.createComponent(PositionComponent.class);
        position.x = tx * Tile.SIZE + BODY_WIDTH / 2.0;
        position.y = ty * Tile.SIZE + BODY_HEIGHT / 2.0;

        HealthComponent health = engine.createComponent(HealthComponent.class);
        health.max    = 1.0;
        health.health = 1.0;

        MobComponent mob = engine.createComponent(MobComponent.class);
        mob.kind = kind;

        entity.add(position);
        entity.add(engine.createComponent(VelocityComponent.class));
        entity.add(body);
        entity.add(health);
        entity.add(mob);
        return entity;
    }

    private CasterComponent caster(double spellDamage) {
        CasterComponent caster = engine.createComponent(CasterComponent.class);
        caster.shotDelay   = SHOT_DELAY;
        caster.spellDamage = spellDamage;
        return caster;
    }

    private ContactDamageComponent contactDamage() {
        ContactDamageComponent contact = engine.createComponent(ContactDamageComponent.class);
        contact.damage = CONTACT_DAMAGE;
        contact.delay  = CONTACT_DELAY;
        return contact;
    }

    /** Builds a wander AI that starts in a random direction, or standing still. */
    private WanderAiComponent wander(float minTurnSeconds, float maxTurnSeconds) {
        WanderAiComponent wander = engine.createComponent(WanderAiComponent.class);
        wander.speed          = WANDER_SPEED;
        wander.minTurnSeconds = minTurnSeconds;
        wander.maxTurnSeconds = maxTurnSeconds;
        wander.directionX     = random.nextInt(3) - 1;
        wander.directionY     = random.nextInt(3) - 1;
        return wander;
    }
}
