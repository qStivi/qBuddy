package com.qStivi.discord;

import com.qStivi.speechToText.MicrosoftSpeechToText;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class AudioHandler implements AudioReceiveHandler, AudioSendHandler {
    private static final Logger logger = LoggerFactory.getLogger(AudioHandler.class);
    private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    AudioInputStream audioInputStream;
    byte[] lastFrame;
    MicrosoftSpeechToText speechToText;
    String path;
    int fileNumber = 0;
    TextChannel channel;
    private Timer timer = new Timer();

    public AudioHandler(TextChannel channel) {
        this.channel = channel;
        path = "audio/";
        this.speechToText = new MicrosoftSpeechToText();
    }

    @Override
    public boolean canProvide() {
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
        byte[] data = userAudio.getAudioData(1);
        outStream.write(data, 0, data.length);

        // Cancel the previous timer if it exists.
        timer.cancel();

        // Create a new timer.
        timer = new Timer();

        // Schedule a new task. If this task is not cancelled within 2 seconds, that means the user has stopped speaking.
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    endOfSpeech();
                } catch (IOException | LineUnavailableException | ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 2000);
    }

    public void endOfSpeech() throws IOException, LineUnavailableException, ExecutionException, InterruptedException {
        byte[] audioData = outStream.toByteArray();
        InputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
        AudioFormat audioFormat = AudioReceiveHandler.OUTPUT_FORMAT;
        AudioInputStream audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat, audioData.length / audioFormat.getFrameSize());
        File outputFile = new File(path + fileNumber++ + ".wav");
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outputFile);
        var result = speechToText.recognize(outputFile);
        channel.sendMessage(result).queue();
        outStream.reset();
    }
}
