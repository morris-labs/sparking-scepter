package com.southcentralpositronics.reign_gdx.engine.level;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.maps.ImageResolver;
import com.badlogic.gdx.maps.tiled.BaseTmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Reads a Tiled TMX file with libGDX's TMX parser, but loads no images. Each
 * tile keeps its properties and has no texture region, so loading needs no
 * window or OpenGL context, and a test can load an arena headless.
 *
 * <p>The libGDX {@link TmxMapLoader} creates a texture for every tileset
 * image, which needs OpenGL. This loader reuses the same parsing and skips the
 * images: the game draws tiles from its own atlas by tile type.
 */
final class HeadlessTmxLoader extends BaseTmxMapLoader<TmxMapLoader.Parameters> {
    /** Resolves every image to nothing. */
    private static final ImageResolver NO_IMAGES = name -> null;

    HeadlessTmxLoader() {
        super(null);
    }

    /**
     * Parses a TMX file and its external tilesets.
     *
     * @param tmxFile The TMX file.
     * @return The map, with tile layers, object layers, and tile properties,
     *         and no textures.
     */
    TiledMap load(FileHandle tmxFile) {
        root = xml.parse(tmxFile);
        return loadTiledMap(tmxFile, new TmxMapLoader.Parameters(), NO_IMAGES);
    }

    /** Adds every tile in a tileset with no image, for either kind of tileset. */
    @Override
    protected void addStaticTiles(FileHandle tmxFile, ImageResolver imageResolver, TiledMapTileSet tileSet,
                                  Element element, Array<Element> tileElements, String name, int firstgid,
                                  int tilewidth, int tileheight, int spacing, int margin, String source,
                                  int offsetX, int offsetY, String imageSource, int imageWidth, int imageHeight,
                                  FileHandle image) {
        if (image != null) {
            // One image for the whole tileset: count the tiles from its size.
            int columns = Math.max(0, (imageWidth - 2 * margin + spacing) / (tilewidth + spacing));
            int rows    = Math.max(0, (imageHeight - 2 * margin + spacing) / (tileheight + spacing));
            for (int index = 0; index < columns * rows; index++) {
                addStaticTiledMapTile(tileSet, null, firstgid + index, offsetX, offsetY);
            }
        } else {
            // An image collection: one tile per tile element.
            for (Element tileElement : tileElements) {
                addStaticTiledMapTile(tileSet, null, firstgid + tileElement.getIntAttribute("id"), offsetX, offsetY);
            }
        }
    }

    /** Skips collision shapes drawn on tiles: they need the tile's image size, and arenas don't use them. */
    @Override
    protected void addTileObjectGroup(TiledMapTile tile, Element tileElement) {
    }

    @Override
    protected Array<AssetDescriptor> getDependencyAssetDescriptors(FileHandle tmxFile,
                                                                   TextureLoader.TextureParameter textureParameter) {
        return new Array<>();
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, TmxMapLoader.Parameters parameter) {
        throw new UnsupportedOperationException("Load arenas with Arena.fromTmx");
    }

    @Override
    public TiledMap loadSync(AssetManager manager, String fileName, FileHandle file, TmxMapLoader.Parameters parameter) {
        throw new UnsupportedOperationException("Load arenas with Arena.fromTmx");
    }
}
