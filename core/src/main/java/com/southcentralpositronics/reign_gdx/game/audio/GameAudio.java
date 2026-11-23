package com.southcentralpositronics.reign_gdx.game.audio;

import com.badlogic.gdx.utils.Disposable;
import com.southcentralpositronics.reign_gdx.engine.audio.MusicPlayer;
import com.southcentralpositronics.reign_gdx.engine.audio.SoundBoard;
import com.southcentralpositronics.reign_gdx.game.sim.SimulationEvent;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reign's sound effects and music. The play screen hands it the events it
 * drains from the simulation each frame, and it plays a sound for each kind
 * of event, plus the run music and the game-over jingle. The simulation never
 * touches it.
 *
 * <p>The files live in {@code assets/audio/}, and {@code assets/audio/CREDITS.md}
 * records where each one comes from.
 */
public final class GameAudio implements Disposable {
    /** The music that loops during a run. */
    public static final String RUN_MUSIC       = "audio/run-music.ogg";
    /** The jingle that replaces the run music when the player dies. */
    public static final String GAME_OVER_MUSIC = "audio/game-over.ogg";
    private static final float MUSIC_VOLUME    = 0.4f;

    /** A sound effect, its file, and its volume from 0 to 1. */
    public enum Cue {
        PLAYER_CAST("audio/cast.ogg", 0.5f),
        /** Enemy casts share the player's sound, quieter, so a room of Shooters doesn't drown the rest. */
        ENEMY_CAST("audio/cast.ogg", 0.2f),
        HIT("audio/hit.ogg", 0.6f),
        ENEMY_DEATH("audio/enemy-death.ogg", 0.8f),
        PLAYER_DAMAGE("audio/player-damage.ogg", 0.8f),
        PLAYER_DEATH("audio/player-death.ogg", 1f),
        WAVE_START("audio/wave-start.ogg", 0.9f);

        private final String path;
        private final float  volume;

        Cue(String path, float volume) {
            this.path   = path;
            this.volume = volume;
        }

        /** Gets the cue for an event. */
        public static Cue of(SimulationEvent event) {
            return switch (event.type()) {
                case CAST -> event.kind().isEnemy() ? ENEMY_CAST : PLAYER_CAST;
                case HIT -> HIT;
                case ENEMY_DEATH -> ENEMY_DEATH;
                case PLAYER_DAMAGE -> PLAYER_DAMAGE;
                case PLAYER_DEATH -> PLAYER_DEATH;
                case WAVE_START -> WAVE_START;
            };
        }
    }

    private final SoundBoard<Cue> sounds;
    private final MusicPlayer     music = new MusicPlayer();

    /** Loads every sound effect. It needs a running application. */
    public GameAudio() {
        Map<Cue, String> paths   = new EnumMap<>(Cue.class);
        Map<Cue, Float>  volumes = new EnumMap<>(Cue.class);
        for (Cue cue : Cue.values()) {
            paths.put(cue, cue.path);
            volumes.put(cue, cue.volume);
        }
        sounds = new SoundBoard<>(Cue.class, paths, volumes);
    }

    /**
     * Plays the sounds for one frame's events. Each cue plays at most once a
     * frame, however many events call for it, so a burst of hits doesn't stack
     * into one loud sound. The player's death replaces the damage that caused
     * it and switches the run music to the game-over jingle.
     *
     * @param events The events drained from the simulation this frame.
     */
    public void play(List<SimulationEvent> events) {
        if (events.isEmpty()) return;
        Set<Cue> cues = EnumSet.noneOf(Cue.class);
        for (SimulationEvent event : events) cues.add(Cue.of(event));
        if (cues.contains(Cue.PLAYER_DEATH)) {
            cues.remove(Cue.PLAYER_DAMAGE);
            music.play(GAME_OVER_MUSIC, false, MUSIC_VOLUME);
        }
        for (Cue cue : cues) sounds.play(cue);
    }

    /** Starts the run music from the beginning, looping. */
    public void startRunMusic() {
        music.play(RUN_MUSIC, true, MUSIC_VOLUME);
    }

    /**
     * Stops the run music, as when a run is abandoned for the main menu. The
     * game-over jingle keeps playing, so it carries over to the game-over
     * screen.
     */
    public void stopRunMusic() {
        if (RUN_MUSIC.equals(music.getPath())) music.stop();
    }

    /** Pauses whatever music is playing. */
    public void pauseMusic() {
        music.pause();
    }

    /** Resumes music that {@link #pauseMusic()} paused. */
    public void resumeMusic() {
        music.resume();
    }

    @Override
    public void dispose() {
        music.dispose();
        sounds.dispose();
    }
}
