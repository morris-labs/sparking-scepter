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

**Wave**:
A group of enemies that the arena releases against the player together.
_Avoid_: round, level
