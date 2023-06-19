package com.qStivi.speechToText;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.PushAudioInputStream;
import com.qStivi.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MicrosoftSTT {

    private static final Logger logger = LoggerFactory.getLogger(MicrosoftSTT.class);
    private static final String speechKey = PropertiesLoader.getInstance().getAPIKey("microsoft.speech.key1");
    private static final String speechRegion = PropertiesLoader.getInstance().getAPIKey("microsoft.speech.region");

    public static String recognize(PushAudioInputStream audioInputStream) {
        try {
            logger.info("Recognizing...");

            SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);
            speechConfig.setSpeechRecognitionLanguage("de-DE");

            AudioConfig audioConfig = AudioConfig.fromStreamInput(audioInputStream);
            SpeechRecognizer speechRecognizer = new SpeechRecognizer(speechConfig, audioConfig);

            Future<SpeechRecognitionResult> task = speechRecognizer.recognizeOnceAsync();
            SpeechRecognitionResult result = task.get();

            return result.getText();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
            // TODO handle exception
        }
    }
    public static String recognize(File file) {
        try {
            logger.info("Recognizing...");

            SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);
            speechConfig.setSpeechRecognitionLanguage("de-DE");

            AudioConfig audioConfig = AudioConfig.fromWavFileInput(file.getPath());
            SpeechRecognizer speechRecognizer = new SpeechRecognizer(speechConfig, audioConfig);

            Future<SpeechRecognitionResult> task = speechRecognizer.recognizeOnceAsync();
            SpeechRecognitionResult result = task.get();

            return result.getText();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
            // TODO handle exception
        }
    }


}
