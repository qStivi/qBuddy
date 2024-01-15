package com.qStivi.audio;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DiscordAudioSendHandler implements AudioSendHandler {
    private static final Logger LOGGER = JDALogger.getLog(DiscordAudioSendHandler.class);
    private final AudioSendQueue audioQueue = new AudioSendQueue();
    private final List<SilenceListener> audioProvidedListeners = new ArrayList<>();

    public DiscordAudioSendHandler() {
    }

    @Override
    public boolean canProvide() {
        return !audioQueue.isEmpty();
    }

    public void addAudioProvidedListener(SilenceListener listener) {
        audioProvidedListeners.add(listener);
    }

    public void removeAudioProvidedListener(SilenceListener listener) {
        audioProvidedListeners.remove(listener);
    }

    private void notifyAudioProvided() {
        for (SilenceListener listener : audioProvidedListeners) {
            listener.onSilenceDetected();
        }
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        LOGGER.debug("Providing audio");
        var audio = audioQueue.poll();
        if (audio == null) {
            throw new RuntimeException("Audio is null! This should never happen!");
        }
        LOGGER.debug("Providing audio of size: " + audio.length);

        // Reset the last audio received time each time audio is provided
        // This is to ensure that the bot's own speech does not count as silence
        notifyAudioProvided();
        return ByteBuffer.wrap(audio);
    }

    public void offer(byte[] audio) {
        audioQueue.offer(audio);
    }
}
