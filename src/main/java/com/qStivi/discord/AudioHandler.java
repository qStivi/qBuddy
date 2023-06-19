package com.qStivi.discord;

import com.qStivi.speechToText.ISpeechToText;
import com.qStivi.speechToText.MicrosoftSpeechToText;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.UserAudio;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioHandler implements AudioReceiveHandler, AudioSendHandler {
    private static final Logger logger = LoggerFactory.getLogger(AudioHandler.class);
    AudioInputStream audioInputStream;
    byte[] lastFrame;
    ISpeechToText speechToText;

    public AudioHandler() {
        logger.info("Creating AudioHandler...");
        this.speechToText = new MicrosoftSpeechToText();

        logger.info("AudioHandler created.");
    }

    @Override
    public boolean canProvide() {
        if (this.audioInputStream == null) return false;
        AudioFormat audioFormat = this.audioInputStream.getFormat();

        int sampleSizeInBytes = audioFormat.getSampleSizeInBits() / 8;
        int sampleRate = (int) audioFormat.getSampleRate();
        int channels = audioFormat.getChannels();
        int bytesPerFrame = sampleSizeInBytes * channels;

        double duration = 20.0; // milliseconds

        int bytesToRead = (int) ((sampleRate * duration / 1000) * bytesPerFrame);

        byte[] buffer = new byte[bytesToRead];
        int bytesRead = 0;
        int totalBytesRead = 0;
        try {
            bytesRead = this.audioInputStream.read(buffer, totalBytesRead, bytesToRead - totalBytesRead);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bytesRead > 0) {
            lastFrame = buffer;
            return true;
        }
        return false;

    }

    @Override
    public ByteBuffer provide20MsAudio() {
        return ByteBuffer.wrap(lastFrame);
    }

    @Override
    public boolean canReceiveUser() {
        return true;
    }

    @Override
    public void handleUserAudio(@NotNull UserAudio userAudio) {
        if (userAudio.getUser().isBot()) return;
        if (userAudio.getAudioData(1).length == 0) return;

        speechToText.sendData(userAudio);
    }
}
