package com.qStivi.stt;

import com.microsoft.cognitiveservices.speech.CancellationReason;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioInputStream;
import com.qStivi.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

public class MicrosoftSTT {

    private static final Logger logger = LoggerFactory.getLogger(MicrosoftSTT.class);

    // This example requires environment variables named "SPEECH_KEY" and "SPEECH_REGION"
    private static final String speechKey = PropertiesLoader.getInstance().getAPIKey("microsoft.speech.key1");
    private static final String speechRegion = PropertiesLoader.getInstance().getAPIKey("microsoft.speech.region");
    private static Semaphore stopTranslationWithFileSemaphore;

    public MicrosoftSTT(AudioInputStream audioInputStream) {
        logger.info("Creating MicrosoftSTT...");
        try {
            SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);
            speechConfig.setSpeechRecognitionLanguage("de-DE");

            AudioConfig audioConfig = AudioConfig.fromStreamInput(audioInputStream);
            SpeechRecognizer speechRecognizer = new SpeechRecognizer(speechConfig, audioConfig);

            // First initialize the semaphore.
            stopTranslationWithFileSemaphore = new Semaphore(0);

            speechRecognizer.recognizing.addEventListener((s, e) -> {
                logger.debug("RECOGNIZING: Text=" + e.getResult().getText());
            });

            speechRecognizer.recognized.addEventListener((s, e) -> {
                if (e.getResult().getReason() == ResultReason.RecognizedSpeech) {
                    logger.info("RECOGNIZED: Text=" + e.getResult().getText());
                } else if (e.getResult().getReason() == ResultReason.NoMatch) {
                    logger.warn("NOMATCH: Speech could not be recognized.");
                }
            });

            speechRecognizer.canceled.addEventListener((s, e) -> {
                logger.error("CANCELED: Reason=" + e.getReason());

                if (e.getReason() == CancellationReason.Error) {
                    logger.error("CANCELED: ErrorCode=" + e.getErrorCode());
                    logger.error("CANCELED: ErrorDetails=" + e.getErrorDetails());
                    logger.error("CANCELED: Did you set the speech resource key and region values?");
                }

                stopTranslationWithFileSemaphore.release();
            });

            speechRecognizer.sessionStopped.addEventListener((s, e) -> {
                logger.info("\n    Session stopped event.");
                stopTranslationWithFileSemaphore.release();
            });

            // Starts continuous recognition. Uses StopContinuousRecognitionAsync() to stop recognition.
            speechRecognizer.startContinuousRecognitionAsync().get();

            // Waits for completion.
//            stopTranslationWithFileSemaphore.acquire();

            // Stops recognition.
//            speechRecognizer.stopContinuousRecognitionAsync().get();

            logger.info("Created MicrosoftSTT.");
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
            // TODO handle exception
        }
    }
}
