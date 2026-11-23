package com.southcentralpositronics.reign_gdx.engine.graphics;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/** A sequence of atlas regions that plays as an animation. */
public class AnimatedSprite {
    private final Animation<TextureRegion> animation;
    private final boolean                  looping;

    public AnimatedSprite(TextureAtlas atlas, String regionName, float frameDuration, boolean looping) {
        Array<TextureAtlas.AtlasRegion> regions = atlas.findRegions(regionName);
        if (regions == null || regions.size == 0) {
            throw new IllegalArgumentException("No regions found for: " + regionName);
        }
        this.animation = new Animation<>(frameDuration, regions);
        this.looping   = looping;
    }

    /**
     * Gets the frame that shows after the animation has played for a time.
     *
     * @param stateTime The seconds since the animation started.
     * @return The frame.
     */
    public TextureRegion getFrameAt(float stateTime) {
        return animation.getKeyFrame(stateTime, looping);
    }

    public int getFrameCount() {
        return animation.getKeyFrames().length;
    }

    public float getAnimationDuration() {
        return animation.getAnimationDuration();
    }
}
