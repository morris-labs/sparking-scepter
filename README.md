# Reign

Reign is a top-down 2D action game written in Java on [libGDX](https://libgdx.com/)
1.13.1. You play a king who fights waves of enemies with fireballs. It ports an
earlier Java2D version of the game to libGDX, and it includes a map editor that
paints level PNG files.

## Requirements

- JDK 17 or later to run Gradle. The build compiles for Java 23; if your
  machine doesn't have JDK 23, Gradle downloads it.
- A desktop with OpenGL support.

## Run the game

To build and start the game, run the following from the repository root:

```bash
./gradlew lwjgl3:run

```

To start the map editor:

```bash
./gradlew lwjgl3:runEditor

```

## Run the tests

From the repository root, run the JUnit 5 tests without a window:

```bash
./gradlew test

```

The tests drive the game through the `Simulation` class, with no window,
OpenGL context, or audio: they build a small arena, feed player input, run
fixed ticks, and check what happens. A level smoke test also loads a PNG
fixture and checks tile types, collision flags, image orientation, and the
fallback for an unknown color.

## Controls

- **Move**: W, A, S, D, or the arrow keys.
- **Fire**: hold the left mouse button; spells fly toward the pointer.
- **Add Mobs** and **Clear Mobs**: spawn a wave or remove every enemy. A new
  wave spawns when every enemy is dead, unless you clicked **Clear Mobs**.

## Levels

Each pixel in a level PNG file is one 16x16 tile. The following colors map to
tile types:

| Color (ARGB) | Tile | Blocks movement |
|---|---|---|
| `0xFF125B0A` | Grass | No |
| `0xFF434343` | Floor | No |
| `0xFFC4DBFF` | Water | No |
| `0xFF004B00` | Hedge | Yes |
| `0xFF665C09` | Wall | Yes |
| `0xFF6C3279` | Alternate wall | Yes |
| `0xFF280A2B` | Void | Yes |

Any other color becomes void. The game loads `assets/levels/TestMap.png`; to
load another level, change `MAP_PATH` in the `Game` class.

## How the game runs

The game's rules run in a simulation that advances in fixed ticks of 1/60 of
a second. Each frame, the game turns the keyboard and mouse into player
input, runs as many ticks as the frame's time calls for, and then draws the
result. Speeds, fire rates, and timers are per second, so the game plays at
the same speed at any frame rate.

## Project layout

- `core/src/main/java/com/southcentralpositronics/reign_gdx/engine/`: arenas,
  terrain, collision, pathfinding, entities, particles, input, the fixed
  timestep, rendering helpers, and the file chooser widget. Engine packages have no dependency on game packages.
- `core/src/main/java/com/southcentralpositronics/reign_gdx/game/`: Reign's
  application, simulation, renderer, mobs, spells, level rules, HUD, minimap,
  and map editor.
- `core/src/test/`: JUnit 5 tests and PNG fixtures.
- `lwjgl3/`: the desktop launchers.
- `assets/`: atlases, levels, sprites, and UI skins. Both run tasks use this
  folder as the working directory.

## License

Copyright (c) 2026 Isaac Morris. All rights reserved. See the `LICENSE`
file.
