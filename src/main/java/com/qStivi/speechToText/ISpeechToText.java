package com.qStivi.speechToText;

import net.dv8tion.jda.api.audio.UserAudio;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ISpeechToText {

    /**
     * Sends the audio to the speech to text service.
     * This method will be called every 20ms when someone is speaking.
     * Hence, the implementation should be as fast as possible or non-blocking.
     *
     * @param userAudio The audio data.
     */
    void sendData(@NotNull UserAudio userAudio);

    /**
     * Returns the latest recognized text.
     * While there is no recognized text, this method should be blocking.
     *
     * @return The recognized text or null if no text is available.
     */
    @NotNull String getData();
}
