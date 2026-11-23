package com.southcentralpositronics.reign_gdx.engine.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * A set of short sound effects, one per key, loaded into memory once and
 * played on demand. Each key has its own volume.
 *
 * <p>It needs a running application with audio, so the simulation never
 * holds one: the screen that drains the simulation's events plays them.
 *
 * @param <K> The enum whose constants name the sounds.
 */
public final class SoundBoard<K extends Enum<K>> implements Disposable {
    private final Map<K, Sound> sounds;
    private final Map<K, Float> volumes;

    /**
     * Loads one sound for each entry.
     *
     * @param type    The enum class of the keys.
     * @param paths   The internal asset path of each key's sound file.
     * @param volumes The volume of each key, from 0 to 1. A key without a
     *                volume plays at full volume.
     */
    public SoundBoard(Class<K> type, Map<K, String> paths, Map<K, Float> volumes) {
        this.sounds  = new EnumMap<>(type);
        this.volumes = new EnumMap<>(type);
        this.volumes.putAll(volumes);
        // Load each file once, even when several keys share it.
        Map<String, Sound> byPath = new HashMap<>();
        for (Map.Entry<K, String> entry : paths.entrySet()) {
            Sound sound = byPath.computeIfAbsent(entry.getValue(),
                                                 path -> Gdx.audio.newSound(Gdx.files.internal(path)));
            sounds.put(entry.getKey(), sound);
        }
    }

    /** Plays a key's sound once at its volume. A key with no sound does nothing. */
    public void play(K key) {
        Sound sound = sounds.get(key);
        if (sound != null) sound.play(volumes.getOrDefault(key, 1f));
    }

    @Override
    public void dispose() {
        sounds.values().stream().distinct().forEach(Sound::dispose);
        sounds.clear();
    }
}
