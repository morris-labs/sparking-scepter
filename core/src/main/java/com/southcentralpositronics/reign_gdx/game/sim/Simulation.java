package com.southcentralpositronics.reign_gdx.game.sim;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.southcentralpositronics.reign_gdx.engine.entity.component.HealthComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.ParticleComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.component.PositionComponent;
import com.southcentralpositronics.reign_gdx.engine.entity.system.LifetimeSystem;
import com.southcentralpositronics.reign_gdx.engine.entity.system.MovementSystem;
import com.southcentralpositronics.reign_gdx.engine.entity.system.SeparationSystem;
import com.southcentralpositronics.reign_gdx.engine.entity.system.ParticleSystem;
import com.southcentralpositronics.reign_gdx.engine.input.PlayerInput;
import com.southcentralpositronics.reign_gdx.engine.level.Arena;
import com.southcentralpositronics.reign_gdx.engine.level.InvalidArenaException;
import com.southcentralpositronics.reign_gdx.engine.level.Level;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;
import com.southcentralpositronics.reign_gdx.engine.pathfinding.AStarPathfinder;
import com.southcentralpositronics.reign_gdx.engine.time.FixedTimestep;
import com.southcentralpositronics.reign_gdx.engine.util.Vector2i;
import com.southcentralpositronics.reign_gdx.game.entity.Families;
import com.southcentralpositronics.reign_gdx.game.entity.MobFactory;
import com.southcentralpositronics.reign_gdx.game.entity.SpellFactory;
import com.southcentralpositronics.reign_gdx.game.entity.component.MobComponent;
import com.southcentralpositronics.reign_gdx.game.entity.component.PlayerComponent;
import com.southcentralpositronics.reign_gdx.game.entity.system.AStarAiSystem;
import com.southcentralpositronics.reign_gdx.game.entity.system.CastingSystem;
import com.southcentralpositronics.reign_gdx.game.entity.system.ChaseAiSystem;
import com.southcentralpositronics.reign_gdx.game.entity.system.ContactDamageSystem;
import com.southcentralpositronics.reign_gdx.game.entity.system.DeathSystem;
import com.southcentralpositronics.reign_gdx.game.entity.system.MobAnimationSystem;
import com.southcentralpositronics.reign_gdx.game.entity.system.PlayerDeathSystem;
import com.southcentralpositronics.reign_gdx.game.entity.system.PlayerInputSystem;
import com.southcentralpositronics.reign_gdx.game.entity.system.ShooterAiSystem;
import com.southcentralpositronics.reign_gdx.game.entity.system.SpellSystem;
import com.southcentralpositronics.reign_gdx.game.entity.system.WanderAiSystem;
import com.southcentralpositronics.reign_gdx.game.entity.system.WaveSystem;
import com.southcentralpositronics.reign_gdx.game.upgrade.Upgrade;
import com.southcentralpositronics.reign_gdx.game.upgrade.UpgradeChoice;

import java.util.ArrayList;
import java.util.List;

/**
 * One run of Reign in one arena, advanced one fixed tick at a time.
 *
 * <p>The simulation is the seam between the game's rules and everything that
 * talks to a person: it has no dependency on OpenGL, the window, or audio. The
 * play screen turns keyboard and mouse input into a {@link PlayerInput}, runs
 * as many {@link #tick()} calls as a {@link FixedTimestep} asks for, draws
 * {@link #getEntities()}, and passes {@link #drainEvents()} to audio and the
 * HUD. Tests drive it the same way, with no window.
 *
 * <p>The seed makes every random choice in the rules repeatable: where
 * enemies spawn, how they wander, and which upgrades a break offers.
 * Particles draw from a separate random source with the same seed, so they
 * never change the rules' choices.
 *
 * <p>The simulation owns an Ashley engine (ADR 0002) and updates it once per
 * tick. Every entity is an Ashley entity, and every rule is a system, run in
 * the order that
 * {@link com.southcentralpositronics.reign_gdx.game.entity.system.GamePriority}
 * and the engine's
 * {@link com.southcentralpositronics.reign_gdx.engine.entity.system.SystemPriority}
 * set together.
 */
public final class Simulation {
    private final Engine                 engine = new Engine();
    private final EventLog               events = new EventLog();
    private final RunState               run    = new RunState();
    private final Level                  level;
    private final WaveSystem             waves;
    private final UpgradeChoice          upgrades;
    private final MobFactory             mobs;
    private final ImmutableArray<Entity> particles;
    private final ImmutableArray<Entity> spells;
    private final ImmutableArray<Entity> enemies;
    private final Entity                 player;
    private       long                   ticks  = 0;

    /**
     * Creates a simulation and opens the run with the break before the first
     * wave.
     *
     * @param arena The arena to play. The player appears at its player start.
     * @param seed  The seed for every random choice in the rules.
     * @throws InvalidArenaException If the arena has no player start.
     */
    public Simulation(Arena arena, long seed) {
        Vector2i start = arena.getPlayerStart().orElseThrow(
            () -> new InvalidArenaException("The arena has no player start; give it one where the player appears"));
        level = new Level(arena, seed, engine);
        mobs  = new MobFactory(engine, level.getRandom());
        upgrades = new UpgradeChoice(level.getRandom());
        waves    = new WaveSystem(level, mobs, events, run, upgrades);

        engine.addSystem(new PlayerInputSystem());
        engine.addSystem(new ChaseAiSystem());
        engine.addSystem(new AStarAiSystem(new AStarPathfinder(level)));
        engine.addSystem(new WanderAiSystem(level));
        engine.addSystem(new ShooterAiSystem());
        engine.addSystem(new MovementSystem(level));
        engine.addSystem(new SeparationSystem(level, Families.MOBS));
        engine.addSystem(new CastingSystem(new SpellFactory(engine), events));
        engine.addSystem(new SpellSystem(level, events));
        engine.addSystem(new ContactDamageSystem(events));
        engine.addSystem(new PlayerDeathSystem(level, events, run));
        engine.addSystem(new DeathSystem(level, events));
        engine.addSystem(waves);
        engine.addSystem(new MobAnimationSystem());
        engine.addSystem(new ParticleSystem(level));
        engine.addSystem(new LifetimeSystem());

        particles = engine.getEntitiesFor(Families.PARTICLES);
        spells    = engine.getEntitiesFor(Families.SPELLS);
        enemies   = engine.getEntitiesFor(Families.ENEMIES);

        player = mobs.createPlayer(start.x, start.y);
        waves.startRun();
    }

    /**
     * Sets the player's input. The input holds for every later tick until the
     * next call, as a held key does.
     *
     * @param input What the player does.
     */
    public void setInput(PlayerInput input) {
        PlayerComponent.MAPPER.get(player).input = input;
    }

    /** Advances the simulation by one tick of {@link FixedTimestep#TICK_SECONDS}. */
    public void tick() {
        engine.update(FixedTimestep.TICK_SECONDS);
        ticks++;
    }

    /** Gets the number of ticks run so far. */
    public long getTickCount() {
        return ticks;
    }

    /** Gets a snapshot of the player. */
    public EntityState getPlayer() {
        return mobState(player);
    }

    /** Gets the most health the player can have: 1 at the start of a run, more with upgrades. */
    public double getPlayerMaxHealth() {
        return HealthComponent.MAPPER.get(player).max;
    }

    /**
     * Gets a snapshot of every entity, in drawing order: particles, spells,
     * enemies, then the player.
     */
    public List<EntityState> getEntities() {
        List<EntityState> states = new ArrayList<>(particles.size() + spells.size() + enemies.size() + 1);
        for (Entity entity : particles) {
            PositionComponent position = PositionComponent.MAPPER.get(entity);
            ParticleComponent particle = ParticleComponent.MAPPER.get(entity);
            // A particle draws at its ground point, raised by its height.
            states.add(new EntityState(EntityKind.PARTICLE, position.x, position.y + particle.z, 0, Facing.DOWN,
                                       false, 0f));
        }
        for (Entity entity : spells) {
            PositionComponent position = PositionComponent.MAPPER.get(entity);
            states.add(new EntityState(EntityKind.SPELL, position.x, position.y, 0, Facing.DOWN, false, 0f));
        }
        for (Entity entity : enemies) states.add(mobState(entity));
        states.add(mobState(player));
        return states;
    }

    /** Takes a snapshot of a mob: its position is the center of its collision box. */
    private static EntityState mobState(Entity entity) {
        PositionComponent position = PositionComponent.MAPPER.get(entity);
        MobComponent      mob      = MobComponent.MAPPER.get(entity);
        return new EntityState(mob.kind, position.x, position.y, HealthComponent.MAPPER.get(entity).health,
                               mob.facing, mob.walking, mob.animationTime);
    }

    /** Gets the number of enemies in the arena. */
    public int getEnemyCount() {
        return enemies.size();
    }

    /** Gets the number of waves that have started: 0 during the break before the first wave. */
    public int getWave() {
        return run.getWave();
    }

    /** Checks whether a break between waves is running, including the one before the first wave. */
    public boolean isBreak() {
        return run.isBreak();
    }

    /** Gets the seconds left in the break, or 0 when no break is running. */
    public float getBreakSecondsLeft() {
        return run.getBreakTicksLeft() * FixedTimestep.TICK_SECONDS;
    }

    /**
     * Gets the score: the sum of the score values of the enemies the player
     * has killed. Each enemy type has its own value, set in
     * {@link MobFactory}.
     */
    public long getScore() {
        return PlayerComponent.MAPPER.get(player).score;
    }

    /**
     * Checks whether the run is over: the player has died. The simulation
     * still ticks after that, but the player can't act, and no wave or break
     * starts.
     */
    public boolean isRunOver() {
        return run.isOver();
    }

    /**
     * Checks whether a break is waiting for the player to choose an upgrade.
     * Each break after a cleared wave waits, with its countdown held, until
     * {@link #chooseUpgrade(Upgrade)}.
     */
    public boolean isUpgradePending() {
        return upgrades.isPending();
    }

    /**
     * Gets the upgrades on offer: {@link Upgrade#OFFER_COUNT} different
     * upgrades drawn with the run's seed, or an empty list when no choice is
     * waiting.
     */
    public List<Upgrade> getUpgradeOffers() {
        return upgrades.getOffers();
    }

    /**
     * Takes one of the offered upgrades: it changes the player at once, and
     * the break's countdown resumes.
     *
     * @param upgrade The upgrade to take; it must be one of {@link #getUpgradeOffers()}.
     * @throws IllegalArgumentException If the upgrade isn't on offer.
     */
    public void chooseUpgrade(Upgrade upgrade) {
        upgrades.choose(upgrade, player);
    }

    /** Gets the upgrades taken this run, in the order they were taken. A new run has none. */
    public List<Upgrade> getUpgradesTaken() {
        return upgrades.getTaken();
    }

    /**
     * Returns the events raised since the last call, in the order they
     * happened, and forgets them.
     */
    public List<SimulationEvent> drainEvents() {
        return events.drain();
    }

    /** Gets the arena's width, in tiles. */
    public int getWidth() {
        return level.getWidth();
    }

    /** Gets the arena's height, in tiles. */
    public int getHeight() {
        return level.getHeight();
    }

    /** Gets the arena's spawn points, the tiles where enemies enter, in the order the arena lists them. */
    public List<Vector2i> getSpawnPoints() {
        return level.getSpawnPoints().stream().map(point -> new Vector2i(point.x, point.y)).toList();
    }

    /** Gets a tile, or null outside the arena. Don't change it. */
    public Tile getTileAt(int tx, int ty) {
        return level.getTileAt(tx, ty);
    }

    /**
     * Adds one enemy with its collision box on a tile, for tests and debugging.
     *
     * @param kind The kind of enemy; it must be an enemy kind.
     * @param tx   The tile column.
     * @param ty   The tile row.
     */
    public void spawnEnemy(EntityKind kind, int tx, int ty) {
        mobs.createEnemy(kind, tx, ty);
    }

    /**
     * Spawns the current wave's enemies again, at spawn points, without
     * starting a new wave, and resumes the waves and breaks that
     * {@link #clearEnemies()} paused. Before the first wave, it spawns wave 1's
     * enemies. This is the Add Mobs button.
     */
    public void addEnemies() {
        waves.addEnemies();
    }

    /**
     * Removes every enemy, with no score, and pauses waves and breaks until
     * {@link #addEnemies()}: the break stops counting down, and an empty wave
     * doesn't end. This is the Clear Mobs button.
     */
    public void clearEnemies() {
        waves.clearEnemies();
    }
}
