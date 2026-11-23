package com.southcentralpositronics.reign_gdx.engine.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Disposable;

/**
 * Plays one music track at a time, streamed from a file. Starting a track
 * stops and frees the one before it.
 */
public final class MusicPlayer implements Disposable {
    private Music   music;
    private String  path;
    /** Whether {@link #pause()} stopped the current track partway through. */
    private boolean paused;

    /**
     * Starts a track from the beginning, in place of the current one.
     *
     * @param path    The internal asset path of the music file.
     * @param looping Whether the track starts again when it ends.
     * @param volume  The volume, from 0 to 1.
     */
    public void play(String path, boolean looping, float volume) {
        stop();
        this.path = path;
        music     = Gdx.audio.newMusic(Gdx.files.internal(path));
        music.setLooping(looping);
        music.setVolume(volume);
        music.play();
    }

    /** Gets the path of the current track, or null when none is loaded. */
    public String getPath() {
        return path;
    }

    /** Pauses the current track, if any, so {@link #resume()} picks it up where it stopped. */
    public void pause() {
        if (music == null || !music.isPlaying()) return;
        music.pause();
        paused = true;
    }

    /** Resumes a paused track. A track that already ended doesn't start again. */
    public void resume() {
        if (music == null || !paused) return;
        paused = false;
        music.play();
    }

    /** Stops and frees the current track, if any. */
    public void stop() {
        if (music == null) return;
        music.stop();
        music.dispose();
        music  = null;
        path   = null;
        paused = false;
    }

    @Override
    public void dispose() {
        stop();
    }
}
