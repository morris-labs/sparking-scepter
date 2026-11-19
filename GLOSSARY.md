# Reign

Reign is a top-down arena survival game. This glossary covers the game and
the engine that runs it.

## Structure

**Engine**:
The reusable core that runs a game: timing, screens, levels, entities,
collision, rendering, input, and audio. It knows nothing about Reign's rules.
_Avoid_: framework, core

**Game**:
Reign's own rules and content, built on the Engine: its mobs, spells, waves,
and arenas.
_Avoid_: app

## World

**Arena**:
A level built for survival play, where waves of mobs attack the player.
_Avoid_: map, stage

**Level**:
A grid of tiles plus the entities in it. Every arena is a level.
_Avoid_: world, map

**Spawn point**:
A place in an arena where enemies enter at the start of a wave.
_Avoid_: spawner

**Player start**:
The place in an arena where the player appears when a run begins.
_Avoid_: spawn (for the player)

**Tile**:
One 16x16 cell of a level. A tile is either solid or walkable.
_Avoid_: block, cell

## Entities

**Entity**:
Anything in a level that updates over time: mobs, spells, and particles.
_Avoid_: object, actor

**Mob**:
An entity with health that moves and fights. The player is a mob.
_Avoid_: enemy (an enemy is a mob that isn't the player), creature, unit

**Player**:
The mob that the person playing controls.
_Avoid_: hero, character

**Spell**:
A projectile that a mob casts and that damages the mobs it hits.
_Avoid_: bullet, shot

**Particle**:
A short-lived visual effect with no effect on play.
_Avoid_: debris, spark

## Play

**Run**:
One play of an arena, from the first wave until the player dies.
_Avoid_: game, session, match

**Wave**:
A group of enemies that the arena releases against the player together. The
next wave starts only after the player clears the current one.
_Avoid_: round, level

**Break**:
The pause between waves, after the player clears one and before the next
starts.
_Avoid_: intermission, rest

**Score**:
The points the player earns in one run.

**High score**:
The best score from any run, kept between runs.
_Avoid_: record, best
