package com.qStivi.audio;

import com.qStivi.stt.MicrosoftSTT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;

public class AudioConverter {

    private static final Logger logger = LoggerFactory.getLogger(AudioConverter.class);
    private final AudioFormat inputFormat;
    private final AudioFormat outputFormat;

    public AudioConverter(AudioFormat inputFormat, AudioFormat outputFormat) {
        logger.info("Creating AudioConverter...");
        this.inputFormat = inputFormat;
        this.outputFormat = outputFormat;
        logger.info("AudioConverter created.");
    }

    // TODO test speed od this and if needed implement a async and / or multi threaded version
    public byte[] convert(byte[] inputData) {
        try {
            // Convert input byte array to audio input stream
            AudioInputStream inputAudioStream = new AudioInputStream(
                    new ByteArrayInputStream(inputData), inputFormat, inputData.length / inputFormat.getFrameSize());

            // Convert audio input stream to output format
            AudioInputStream convertedAudioStream = AudioSystem.getAudioInputStream(outputFormat, inputAudioStream);

            // Read the converted audio data into a byte array
            byte[] outputData = new byte[convertedAudioStream.available()];
            // noinspection ResultOfMethodCallIgnored
            convertedAudioStream.read(outputData);

            return outputData;
        } catch (Exception e) {
            e.printStackTrace();
            // TODO Handle the exception appropriately
            return null;
        }
    }
}
