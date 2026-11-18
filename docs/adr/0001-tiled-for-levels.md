# Tiled for levels, and the custom map editor retires

Levels were single-layer PNG files, one pixel per tile, painted in a custom
Scene2D map editor. An arena needs layers and placed objects, such as spawn
points and the player start, which a PNG file can't hold. Levels move to the
Tiled map editor's TMX format, which libGDX loads natively, and the custom
editor is deleted rather than extended. A one-time converter turns the
existing PNG levels into TMX files.

## Considered options

- **Keep PNG files and add a JSON file for objects**: two files per level to
  keep in sync, and the custom editor would need an object tool.
- **Extend the custom editor**: more editor code to maintain, for features
  that Tiled already has.
