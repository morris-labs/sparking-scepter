# Reign

Reign is a top-down 2D action game written in Java on [libGDX](https://libgdx.com/)
1.13.1. You play a king who fights waves of enemies with fireballs. It ports an
earlier Java2D version of the game to libGDX. Arenas are built in the
[Tiled](https://www.mapeditor.org/) map editor.

## Requirements

- JDK 17 or later to run Gradle. The build compiles for Java 23; if your
  machine doesn't have JDK 23, Gradle downloads it.
- A desktop with OpenGL support.

## Run the game

To build and start the game, run the following from the repository root:

```bash
./gradlew lwjgl3:run

```

## Run the tests

From the repository root, run the JUnit 5 tests without a window:

```bash
./gradlew test

```

The tests drive the game through the `Simulation` class, with no window,
OpenGL context, or audio: they build a small arena, feed player input, run
fixed ticks, and check what happens. Arena tests load TMX files the same way
the game does, including the small test arena in
`core/src/test/arenas/test-arena.tmx` and the shipped spawn level.

## Controls

The game opens on the main menu. Click **Play** to start a run, or **Quit**
to close the game.

- **Pause**: press Escape. The run also pauses when the window loses focus.
  While paused, nothing moves; click **Resume** or press Escape to continue,
  or click **Main menu** to abandon the run.
- **Move**: W, A, S, D, or the arrow keys.
- **Fire**: hold the left mouse button; spells fly toward the pointer.

## Waves

A run opens with a 3-second break, then wave 1 enters at the arena's spawn
points. Each enemy enters at a spawn point at least 6 tiles from you, or at the
farthest one if none is that far. When you defeat the last enemy of a wave, a
5-second break starts, and the next wave enters when it ends. Each wave is
bigger than the last, and later waves bring more A* Chasers and Shooters. The
side panel shows the wave number, the enemies left, and the countdown during a
break.

To tune the waves, edit the table in the `WavePlan` class
(`core/src/main/java/com/southcentralpositronics/reign_gdx/game/wave/WavePlan.java`).

## Debug tools

The debug tools are off by default. To turn them on, start the game with the
`debug` Gradle property:

```bash
./gradlew lwjgl3:run -Pdebug

```

To turn them on for the JAR, pass `-Dreign.debug=true` to `java`. With the
debug tools on, the side panel has two more buttons, and each mob's collision
box has a red outline:

- **Add Mobs**: spawns the current wave's enemies again, without starting a
  new wave, and resumes waves after **Clear Mobs**.
- **Clear Mobs**: removes every enemy and holds the run: a break stops
  counting down, and an emptied wave doesn't end, until **Add Mobs**.

## Arenas

An arena is a Tiled TMX file in `assets/levels/` (see
`docs/adr/0001-tiled-for-levels.md`). The game plays
`assets/levels/SpawnLevel.tmx`, the Java2D spawn level converted to TMX. To
play another arena, change `ARENA_PATH` in the `Game` class.
`assets/levels/TestMap.tmx` is the earlier 64x64 test map, converted too, and
the game doesn't load it.

To build an arena in Tiled 1.10 or later:

1. Create an orthogonal map with 16x16 px tiles, in `assets/levels/`.
1. Add the shared tileset, `assets/levels/tiles.tsx`, to the map.
1. Paint the tiles on one or more tile layers. Where layers overlap, the
   highest layer wins; a cell that no layer fills is void.
1. Add an object layer, and in it place one point object with the class
   `PlayerStart` and at least one point object with the class `SpawnPoint`.
   An object's name works in place of its class. Each object marks the tile
   under it, which must not block movement.

Each tile in the tileset has three custom properties, which the game reads
instead of the tile images:

| Tile | `tileType` | `blocksMovement` | `blocksSpells` |
|---|---|---|---|
| Grass | `grass` | No | No |
| Floor | `floor` | No | No |
| Water | `water` | Yes | No |
| Hedge | `hedge` | Yes | Yes |
| Wall | `wall` | Yes | Yes |
| Alternate wall | `wall_alt` | Yes | Yes |
| Void | `void_tile` | Yes | Yes |

A new tile type needs only a tileset entry and a sprite of the same name in
the tile atlas, plus a value in the `Tile.TileType` enum. If an arena has no
player start, no spawn points, an object on a blocking tile, or a tile without
a `tileType`, loading it fails with an error that names the file and the
problem.

## How the game runs

The game's rules run in a simulation that advances in fixed ticks of 1/60 of
a second. Each frame, the play screen turns the keyboard and mouse into
player input, runs as many ticks as the frame's time calls for, and then draws
the result. While the run is paused, no ticks run, but the screen still
draws. Speeds, fire rates, and timers are per second, so the game plays at the
same speed at any frame rate.

Every entity is an [Ashley](https://github.com/libgdx/ashley) entity, libGDX's
entity component system (see `docs/adr/0002-ashley-for-entities.md`). The
simulation owns an Ashley engine and updates it each tick. Each behavior, such
as a position, a collision box, health, or a chase AI, is a component, and
each rule, such as movement, casting, or spell hits, is a system. The player
and each enemy type are built by factories from components, and A* paths come
from an engine pathfinding service that the A* AI system calls.

## Project layout

- `core/src/main/java/com/southcentralpositronics/reign_gdx/engine/`: arenas,
  terrain, collision, pathfinding, entities, Ashley components and systems,
  particles, input, the fixed
  timestep, Tiled arena loading, and rendering helpers. Engine packages have
  no dependency on game packages.
- `core/src/main/java/com/southcentralpositronics/reign_gdx/game/`: Reign's
  application, main menu and play screens, simulation, renderer, mob and
  spell factories, Reign's components and systems (enemy AI, casting, spells,
  death, and waves), HUD, and minimap.
- `core/src/test/`: JUnit 5 tests and the test arena.
- `lwjgl3/`: the desktop launchers.
- `assets/`: atlases, arenas and their tileset, sprites, UI skins, and
  audio. The run task uses this folder as the working directory.

## License

Copyright (c) 2026 Isaac Morris. All rights reserved. See the `LICENSE`
file.

The sound effects and music in `assets/audio/` are public domain under CC0
1.0 and aren't covered by the `LICENSE` file. The
`assets/audio/CREDITS.md` file lists their sources.
