package com.southcentralpositronics.reign_gdx.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.southcentralpositronics.reign_gdx.engine.graphics.AnimatedSprite;
import com.southcentralpositronics.reign_gdx.engine.level.tile.Tile;
import com.southcentralpositronics.reign_gdx.game.sim.EntityState;
import com.southcentralpositronics.reign_gdx.game.sim.Facing;
import com.southcentralpositronics.reign_gdx.game.sim.Simulation;

import java.util.EnumMap;
import java.util.Map;

/**
 * Draws a simulation: the arena's tiles, then every entity from
 * {@link Simulation#getEntities()}. The simulation holds no textures, so this
 * class owns every texture lookup for the play screen.
 */
public final class ArenaRenderer implements Disposable {
    /** The seconds each walking frame shows. */
    private static final float  FRAME_SECONDS = 0.2f;
    /** The atlas region prefix of the player sprite. */
    private static final String PLAYER_SPRITE = "King_Cherno";
    /** The atlas region prefix of the enemy sprite, ported from the Java2D dummy sheet. */
    private static final String ENEMY_SPRITE  = "King_Berno";

    private final Map<Tile.TileType, TextureRegion> tileRegions = new EnumMap<>(Tile.TileType.class);
    private final Map<Facing, AnimatedSprite>       playerWalk;
    private final Map<Facing, AnimatedSprite>       enemyWalk;
    private final TextureRegion                     spell;
    /**
     * One red pixel that every particle draws. A texture per particle leaked
     * GPU memory and broke sprite batching: a single fireball spawns up to
     * 1,000 particles.
     */
    private final Texture                           pixelTexture;
    private final TextureRegion                     pixel;

    /**
     * Looks up every region the play screen draws.
     *
     * @param mobAtlas  The atlas that holds the walking animations.
     * @param tileAtlas The atlas that holds the tiles and the fireball. Each
     *                  tile region is named after its {@link Tile.TileType}.
     */
    public ArenaRenderer(TextureAtlas mobAtlas, TextureAtlas tileAtlas) {
        for (Tile.TileType type : Tile.TileType.values()) tileRegions.put(type, tileAtlas.findRegion(type.name()));
        playerWalk = loadWalk(mobAtlas, PLAYER_SPRITE);
        enemyWalk  = loadWalk(mobAtlas, ENEMY_SPRITE);
        spell      = tileAtlas.findRegion("fireball");

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.RED);
        pixmap.fill();
        pixelTexture = new Texture(pixmap);
        pixmap.dispose();
        pixel = new TextureRegion(pixelTexture);
    }

    private static Map<Facing, AnimatedSprite> loadWalk(TextureAtlas atlas, String prefix) {
        Map<Facing, AnimatedSprite> walk = new EnumMap<>(Facing.class);
        walk.put(Facing.UP, new AnimatedSprite(atlas, prefix + "_Up", FRAME_SECONDS, true));
        walk.put(Facing.DOWN, new AnimatedSprite(atlas, prefix + "_Down", FRAME_SECONDS, true));
        walk.put(Facing.LEFT, new AnimatedSprite(atlas, prefix + "_Left", FRAME_SECONDS, true));
        walk.put(Facing.RIGHT, new AnimatedSprite(atlas, prefix + "_Right", FRAME_SECONDS, true));
        return walk;
    }

    /**
     * Draws the tiles and entities. Call it between {@code batch.begin()} and
     * {@code batch.end()}, with the camera's projection set.
     */
    public void render(SpriteBatch batch, Simulation simulation) {
        renderTiles(batch, simulation);
        for (EntityState entity : simulation.getEntities()) {
            switch (entity.kind()) {
                case PARTICLE -> batch.draw(pixel, (float) entity.x(), (float) entity.y());
                case SPELL -> batch.draw(spell, (float) entity.x() - spell.getRegionWidth() / 2f,
                                         (float) entity.y() - spell.getRegionHeight() / 2f);
                case PLAYER -> drawMob(batch, entity, playerWalk);
                default -> drawMob(batch, entity, enemyWalk);
            }
        }
    }

    private void renderTiles(SpriteBatch batch, Simulation simulation) {
        for (int x = 0; x < simulation.getWidth(); x++) {
            for (int y = 0; y < simulation.getHeight(); y++) {
                Tile tile = simulation.getTileAt(x, y);
                if (tile == null) continue;
                TextureRegion region = tileRegions.get(tile.type);
                if (region != null) batch.draw(region, x * Tile.SIZE, y * Tile.SIZE, Tile.SIZE, Tile.SIZE);
            }
        }
    }

    private static void drawMob(SpriteBatch batch, EntityState mob, Map<Facing, AnimatedSprite> walk) {
        TextureRegion frame = walk.get(mob.facing()).getFrameAt(mob.animationTime());
        batch.draw(frame, (float) mob.x() - EntityState.MOB_SPRITE_OFFSET_X,
                   (float) mob.y() - EntityState.MOB_SPRITE_OFFSET_Y);
    }

    @Override
    public void dispose() {
        pixelTexture.dispose();
    }
}
