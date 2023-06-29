package com.qStivi.discord;

import com.qStivi.chatBotAI.ChatGPT;
import com.qStivi.speechToText.MicrosoftSpeechToText;
import com.qStivi.textToSpeech.MicrosoftTTs;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class AudioHandler implements AudioReceiveHandler, AudioSendHandler {
    private static final Logger logger = LoggerFactory.getLogger(AudioHandler.class);
    private final Map<User, ByteArrayOutputStream> outStreamMap = new HashMap<>();
    private final Map<User, Timer> timerMap = new HashMap<>();
    private final ChatGPT chatBotAI;
    byte[] lastFrame;
    MicrosoftSpeechToText speechToText;
    String path;
    TextChannel channel;
    MicrosoftTTs tts;
    Queue<byte[]> queue = new LinkedList<>();


    public AudioHandler(TextChannel channel) {

        chatBotAI = new ChatGPT();
        this.channel = channel;
        path = "audio/";
        this.speechToText = new MicrosoftSpeechToText();
        this.tts = new MicrosoftTTs(channel.getGuild());
    }

    @Override
    public boolean canReceiveUser() {
        return true;
    }

    @Override
    public void handleUserAudio(@NotNull UserAudio userAudio) {
        var outStream = outStreamMap.get(userAudio.getUser());
        if (outStream == null) {
            outStream = new ByteArrayOutputStream();
            outStreamMap.put(userAudio.getUser(), outStream);
        }
        byte[] data = userAudio.getAudioData(1);
        outStream.write(data, 0, data.length);

        var timer = timerMap.get(userAudio.getUser());

        // Cancel the previous timer if it exists.
        if (timer != null) timer.cancel();

        // Create a new timer.
        timer = new Timer();
        timerMap.put(userAudio.getUser(), timer);

        // Schedule a new task. If this task is not cancelled within 2 seconds, that means the user has stopped speaking.
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    endOfSpeech(userAudio.getUser());
                } catch (IOException | LineUnavailableException | ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 800);
    }

    public void endOfSpeech(User user) throws IOException, LineUnavailableException, ExecutionException, InterruptedException {
        logger.info("End of speech for user: " + user.getEffectiveName());
        var outStream = outStreamMap.get(user);
        logger.info("Audio data size: " + outStream.size());
        byte[] audioData = outStream.toByteArray();
        outStream.reset();
        InputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
        AudioFormat audioFormat = AudioReceiveHandler.OUTPUT_FORMAT;
        AudioInputStream audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat, audioData.length / audioFormat.getFrameSize());
        File outputFile = new File(path + System.currentTimeMillis() + ".wav");
        outputFile.deleteOnExit();
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outputFile);
        var result = speechToText.recognize(outputFile);
        if (!result.isEmpty()) {
            result = user.getEffectiveName() + ": " + result;
//            channel.sendMessage(result).queue();
            var response = chatBotAI.generate(result);
            if (response.startsWith("Buddy: ")) {
                response = response.substring(7);
            }

            var output = new StringBuilder();
            output.append("""
                    <speak version="1.0" xmlns="http://www.w3.org/2001/10/synthesis" xml:lang="en-US">
                        <voice name="de-DE-KasperNeural">
                            <prosody rate="+20%">
                            """);
            output.append(response);
            output.append("""
                            </prosody>
                        </voice>
                    </speak>
                    """);

//            channel.sendMessage(response).queue();

            for (byte[] bytes : tts.synthesizeTextToAudio(output.toString())) {
                queue.offer(bytes);
            }
        }
    }

    @Override
    public boolean canProvide() {
        lastFrame = queue.poll();
        return lastFrame != null;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        return ByteBuffer.wrap(lastFrame);
    }

    @Override
    public boolean isOpus() {
        return false;
    }
}
