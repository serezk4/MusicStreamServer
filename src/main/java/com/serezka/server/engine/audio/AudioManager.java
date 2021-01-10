package com.serezka.server.engine.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;

public class AudioManager {
    private static final Logger logger = LoggerFactory.getLogger(AudioManager.class.getSimpleName());

    /**
     * get audio input stream from file
     * @param track - file with audio
     */
    public static AudioInputStream getAudioInputStream(File track) {
        try {
            AudioInputStream base = AudioSystem.getAudioInputStream(track);
            return AudioSystem.getAudioInputStream(getAudioFormat(base.getFormat()), base);
        } catch (Exception e) {
            logger.error("{}: {}", e.getCause(), e.getMessage());
            return null;
        }
    }

    /**
     * generate audio format from load configuration
     * @return standard audio format from config
     */
    public static AudioFormat getAudioFormat(AudioFormat base) {
        // TODO: FIX PROBLEM WITH COMPRESSION
//        return new AudioFormat(AudioFormat.Encoding.ALAW,
//                Float.parseFloat(Start.getProperties().getProperty(App.Config.AUDIO_SAMPLE_RATE.getName())),
//                Integer.parseInt(Start.getProperties().getProperty(App.Config.AUDIO_SAMPLE_SIZE.getName())),
//                Integer.parseInt(Start.getProperties().getProperty(App.Config.AUDIO_CHANNELS_COUNT.getName())),
//                Integer.parseInt(Start.getProperties().getProperty(App.Config.AUDIO_FRAME_SIZE.getName())),
//                Integer.parseInt(Start.getProperties().getProperty(App.Config.AUDIO_FRAME_RATE.getName())),
//                Boolean.parseBoolean(Start.getProperties().getProperty(App.Config.AUDIO_BIG_ENDIAN.getName())));
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                base.getSampleRate(),
                16,
                base.getChannels(),
                base.getChannels() * 2,
                base.getSampleRate(),
                false);
    }
}
