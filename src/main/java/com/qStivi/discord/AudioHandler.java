package com.qStivi.discord;

import com.microsoft.cognitiveservices.speech.audio.AudioInputStream;
import com.microsoft.cognitiveservices.speech.audio.AudioStreamFormat;
import com.microsoft.cognitiveservices.speech.audio.PushAudioInputStream;
import com.qStivi.audio.AudioConverter;
import com.qStivi.stt.MicrosoftSTT;
import net.dv8tion.jda.api.audio.*;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

public class AudioHandler implements AudioReceiveHandler, AudioSendHandler {
    private static final Logger logger = LoggerFactory.getLogger(AudioHandler.class);

    private final AudioManager audioManager;
    private final SlashCommandInteractionEvent event;
    private final MicrosoftSTT stt;
    private final PushAudioInputStream audioInputStream;
    AudioConverter audioConverter;
    public AudioHandler(AudioManager audioManager, SlashCommandInteractionEvent event) {
        logger.info("Creating AudioHandler...");
        this.audioManager = audioManager;
        var microsoftAudioFormat = new AudioFormat(16000, 16, 1, true, true);
        this.audioConverter = new AudioConverter(AudioReceiveHandler.OUTPUT_FORMAT, microsoftAudioFormat);;
        this.event = event;
        this.audioInputStream = AudioInputStream.createPushStream();
        this.stt = new MicrosoftSTT(audioInputStream);
        logger.info("AudioHandler created.");
    }

    @Override
    public boolean canProvide() {
        return audioManager.isConnected();
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        return null;
    }

    @Override
    public boolean isOpus() {
        return false;
    }

    @Override
    public boolean canReceiveCombined() {
        return false;
    }

    @Override
    public boolean canReceiveUser() {
        return true;
    }

    @Override
    public boolean canReceiveEncoded() {
        return false;
    }

    @Override
    public void handleEncodedAudio(OpusPacket packet) {
    }

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
    }

    @Override
    public void handleUserAudio(UserAudio userAudio) {
        logger.info("handling combined audio...");
//        this.audioInputStream.write(audioConverter.convert(userAudio.getAudioData(1.0f)));
    }

    @Override
    public boolean includeUserInCombinedAudio(User user) {
        return false;
    }
}
