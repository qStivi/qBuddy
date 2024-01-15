package com.qStivi.audio;

import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AudioConverter {
    private static final Logger LOGGER = JDALogger.getLog(AudioConverter.class);

    public static byte[] convert(byte[] audioData, AudioFormat sourceFormat, AudioFormat targetFormat) {
        try {
            // Create an AudioInputStream from the byte array
            AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(audioData), sourceFormat, audioData.length);

            // Create an AudioInputStream for the converted audio data
            AudioInputStream convertedAis = AudioSystem.getAudioInputStream(targetFormat, ais);

            // Create a ByteArrayOutputStream and write the converted audio data to it
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = convertedAis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            // Return the converted audio data
            return baos.toByteArray();

        } catch (IOException e) {
            LOGGER.error("Error converting audio data", e);
            return null;
        }
    }
}
