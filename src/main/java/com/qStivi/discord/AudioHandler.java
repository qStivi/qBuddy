package com.qStivi.discord;

import com.qStivi.speechToText.ISpeechToText;
import com.qStivi.speechToText.MicrosoftSpeechToText;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.UserAudio;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioInputStream;
import java.nio.ByteBuffer;

public class AudioHandler implements AudioReceiveHandler, AudioSendHandler {
    private static final Logger logger = LoggerFactory.getLogger(AudioHandler.class);
    AudioInputStream audioInputStream;
    byte[] lastFrame;
    ISpeechToText speechToText;

    public AudioHandler() {
        this.speechToText = new MicrosoftSpeechToText();
        startAsyncSpeechToText();
        startAsyncChatBot();
        startAsyncTextToSpeech();
    }

    @Override
    public boolean canProvide() {
        throw new UnsupportedOperationException("Not implemented yet!");
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
