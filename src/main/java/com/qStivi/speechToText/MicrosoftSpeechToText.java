package com.qStivi.speechToText;

import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.qStivi.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class MicrosoftSpeechToText {
    private static final Logger logger = LoggerFactory.getLogger(MicrosoftSpeechToText.class);
    private static final String speechKey = PropertiesLoader.getInstance().getAPIKey("microsoft.speech.key1");
    private static final String speechRegion = PropertiesLoader.getInstance().getAPIKey("microsoft.speech.region");
    SpeechConfig config;

    public MicrosoftSpeechToText() {
        // Create speech config
        config = SpeechConfig.fromSubscription(speechKey, speechRegion);
        config.setSpeechRecognitionLanguage("de-DE");
    }

    public String recognize(File file) throws ExecutionException, InterruptedException {

        // Create audio config from file
        AudioConfig audioInput = AudioConfig.fromWavFileInput(file.getAbsolutePath());
        // Create speech recognizer
        SpeechRecognizer recognizer = new SpeechRecognizer(config, audioInput);
        // Recognize speech
        var task = recognizer.recognizeOnceAsync();

        file.delete();

        return task.get().getText();
    }
}
