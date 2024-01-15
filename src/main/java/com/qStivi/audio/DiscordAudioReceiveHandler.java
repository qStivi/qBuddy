package com.qStivi.audio;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordAudioReceiveHandler implements AudioReceiveHandler {
    private static final Logger LOGGER = JDALogger.getLog(DiscordAudioReceiveHandler.class);
    private final static long SILENCE_THRESHOLD = TimeUnit.MINUTES.toMillis(2);
    private final ConcurrentLinkedQueue<UserAudio> audioQueue = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final List<SilenceListener> silenceListeners = new ArrayList<>();
    private long lastAudioReceivedTime;

    public DiscordAudioReceiveHandler() {
        // Reset the last audio received time when the handler is created
        resetLastAudioReceivedTime();
        startAudioCheck();
        LOGGER.debug("Created DiscordAudioReceiveHandler");
    }

    public void addSilenceListener(SilenceListener listener) {
        silenceListeners.add(listener);
    }

    public void removeSilenceListener(SilenceListener listener) {
        silenceListeners.remove(listener);
    }

    private void notifySilenceDetected() {
        for (SilenceListener listener : silenceListeners) {
            listener.onSilenceDetected();
        }
    }

    public UserAudio getUserAudio() {
        return audioQueue.poll();
    }

    @Override
    public boolean canReceiveUser() {
        return System.currentTimeMillis() - lastAudioReceivedTime > 1000;
    }

    @Override
    public void handleUserAudio(@NotNull UserAudio userAudio) {
        if (userAudio.getUser().isBot()) {
            LOGGER.debug("Ignoring audio from bot");
            return; // ignore bots
        }

        // Offer the user audio to the queue and reset the last audio received time
        audioQueue.offer(userAudio);
        resetLastAudioReceivedTime();
    }

    public void startAudioCheck() {
        executorService.scheduleAtFixedRate(() -> {
            if (System.currentTimeMillis() - lastAudioReceivedTime > SILENCE_THRESHOLD) {
                LOGGER.debug("Silence detected");
                silence();
            }
        }, 0, SILENCE_THRESHOLD, TimeUnit.SECONDS);
    }

    private void silence() {
        notifySilenceDetected();
        lastAudioReceivedTime = System.currentTimeMillis();
    }

    public void resetLastAudioReceivedTime() {
        LOGGER.debug("Resetting last audio received time");
        this.lastAudioReceivedTime = System.currentTimeMillis();
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
