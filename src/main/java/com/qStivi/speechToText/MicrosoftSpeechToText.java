package com.qStivi.speechToText;

import com.qStivi.PropertiesLoader;
import net.dv8tion.jda.api.audio.UserAudio;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicrosoftSpeechToText implements ISpeechToText {
    private static final Logger logger = LoggerFactory.getLogger(MicrosoftSpeechToText.class);
    private static final String speechKey = PropertiesLoader.getInstance().getAPIKey("microsoft.speech.key1");
    private static final String speechRegion = PropertiesLoader.getInstance().getAPIKey("microsoft.speech.region");

    public MicrosoftSpeechToText() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public void sendData(@NotNull UserAudio userAudio) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @NotNull
    @Override
    public String getData() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
