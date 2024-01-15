package com.qStivi.speech.stt;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioInputStream;
import com.microsoft.cognitiveservices.speech.audio.AudioStreamFormat;
import com.microsoft.cognitiveservices.speech.audio.PushAudioInputStream;
import com.qStivi.audio.AudioConverter;
import com.qStivi.audio.DiscordAudioReceiveHandler;
import com.qStivi.listeners.TranscriptionListener;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import javax.sound.sampled.AudioFormat;
import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.*;

public class MicrosoftSTT implements ISpeechToText {
    private static final Logger LOGGER = JDALogger.getLog(MicrosoftSTT.class);
    private static Semaphore stopTranslationWithFileSemaphore;
    private final String subscriptionKey;
    private final String region;
    private final DiscordAudioReceiveHandler receiveHandler;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    SpeechRecognizer speechRecognizer;
    private TranscriptionListener listener;
    private LinkedBlockingQueue<UserAudio> AudioQueue;
    private PushAudioInputStream pushStream;
    private ScheduledFuture<?> scheduledTask = null;
    private Set<User> speakers = ConcurrentHashMap.newKeySet();
    public MicrosoftSTT(String subscriptionKey, String region, DiscordAudioReceiveHandler receiveHandler) {
        this.subscriptionKey = subscriptionKey;
        this.region = region;
        this.receiveHandler = receiveHandler;
    }

    public SpeechRecognizer getSpeechRecognizer() {
        return speechRecognizer;
    }
    @Override
    public void setTranscriptionListener(TranscriptionListener listener) {
        this.listener = listener;
    }
    @Override
    public void startTranscription() throws InterruptedException, ExecutionException {
        LOGGER.info("startTranscription: Starting transcription");
        stopTranslationWithFileSemaphore = new Semaphore(0);

        // Create a SpeechConfig object with the specified subscription key and service region as well as the language and profanity settings

        String v2EndpointUrl = "wss://" + region + ".stt.speech.microsoft.com/speech/universal/v2";
        SpeechConfig config = SpeechConfig.fromEndpoint(URI.create(v2EndpointUrl), subscriptionKey);
//        SpeechConfig config = SpeechConfig.fromSubscription(subscriptionKey, region);
//        config.setSpeechRecognitionLanguage("de-DE");
        config.setProfanity(ProfanityOption.Raw);
        config.setProperty(PropertyId.SpeechServiceConnection_LanguageIdMode, "Continuous");
        AutoDetectSourceLanguageConfig autoDetectSourceLanguageConfig = AutoDetectSourceLanguageConfig.fromLanguages(Arrays.asList("de-DE", "en-US", "pt-BR"));

        // Create a PushAudioInputStream with the correct audio format
        AudioStreamFormat format = AudioStreamFormat.getWaveFormatPCM(48000, (short) 16, (short) 1);
        pushStream = AudioInputStream.createPushStream(format);

        // Create a SpeechRecognizer with the PushAudioInputStream
        AudioConfig audioInput = AudioConfig.fromStreamInput(pushStream);
        speechRecognizer = new SpeechRecognizer(config, autoDetectSourceLanguageConfig, audioInput);

        // Set up the speech recognizer
        setupSpeechRecognizer(speechRecognizer);

//        audioHandler.setSpeechRecognizer(speechRecognizer);
        LOGGER.info("startTranscription: Speech recognizer set up");
        startSpeechRecognition(speechRecognizer);

        // Waits for completion, then stops recognition
//        stopTranslationWithFileSemaphore.acquire();
//        LOGGER.info("startTranscription: Transcription completed");
//        speechRecognizer.stopContinuousRecognitionAsync().get();

        new Thread(() -> {
            while (true) {
                var userAudio = receiveHandler.getUserAudio();
                if (userAudio == null) {
                    continue;
                }
                pushAudioData(userAudio);
            }
        }).start();
    }

    public void pushAudioData(UserAudio userAudio) {
        // Write audio data to the PushAudioInputStream
        var audio = userAudio.getAudioData(1);
        var converted = AudioConverter.convert(audio, AudioReceiveHandler.OUTPUT_FORMAT, new AudioFormat(48000, 16, 1, true, false));
        LOGGER.debug("pushAudioData: Pushing audio data");
        pushStream.write(converted);
        speakers.add(userAudio.getUser());

        // Cancel the previous task if it exists
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }

        // Schedule a new task
        scheduledTask = scheduler.schedule(this::endOfSpeech, 1000, TimeUnit.MILLISECONDS);
    }

    private void endOfSpeech() {
        // Create an array of bytes representing 500ms * 2 of silence
        byte[] silence = new byte[96000 * 2];

        // Write the silence to the PushAudioInputStream
        pushStream.write(silence);
    }
    private void setupSpeechRecognizer(SpeechRecognizer speechRecognizer) {
        LOGGER.info("setupSpeechRecognizer: Setting up speech recognizer");
//        SpeechConfig config = SpeechConfig.fromSubscription(subscriptionKey, region);
//        config.setSpeechRecognitionLanguage("de-DE");
//        config.setProfanity(ProfanityOption.Raw);
//
//        var pullStream = new DiscordAudioStreamCallback();
//        pullStream.setAudioQueue(audioQueue);
////        Thread.sleep(5000);
////        for (var userAudio : audioQueue) {
////            pullStream.addAudio(userAudio.getAudioData(1));
//
//        AudioInputStream audioStream = AudioInputStream.createPullStream(pullStream, AudioStreamFormat.getWaveFormatPCM(48000, (short) 16, (short) 2));
//        AudioConfig audioInput = AudioConfig.fromStreamInput(audioStream);
//        SpeechRecognizer speechRecognizer = new SpeechRecognizer(config, audioInput);

        speechRecognizer.recognized.addEventListener(this::handleRecognitionEvent);

        speechRecognizer.canceled.addEventListener(this::handleCancelEvent);

        speechRecognizer.sessionStopped.addEventListener(this::handleSessionStoppedEvent);

        speechRecognizer.sessionStarted.addEventListener((o, sessionEventArgs) -> LOGGER.info("Session has started"));

        speechRecognizer.speechStartDetected.addEventListener((o, speechStartDetectedEventArgs) -> LOGGER.info("Speech start detected"));

        speechRecognizer.speechEndDetected.addEventListener((o, speechEndDetectedEventArgs) -> LOGGER.info("Speech end detected"));

    }

    private void handleSessionStoppedEvent(Object o, SessionEventArgs sessionEventArgs) {
        LOGGER.info("Session has stopped");
//        audioHandler.setSessionActive(false);
        stopTranslationWithFileSemaphore.release();
    }
    private void startSpeechRecognition(SpeechRecognizer speechRecognizer) throws ExecutionException, InterruptedException {
        // Starts continuous recognition, uses StopContinuousRecognitionAsync() to stop recognition
        speechRecognizer.startContinuousRecognitionAsync().get();
    }

    /**
     * Handles the speech recognition event.
     *
     * @param s The source object that raised the event.
     * @param e The speech recognition event arguments.
     */
    private void handleRecognitionEvent(Object s, SpeechRecognitionEventArgs e) {
        if (e.getResult().getReason() == ResultReason.RecognizedSpeech) {
            LOGGER.info("Handling recognition event");
            var text = e.getResult().getText();
            LOGGER.info("RECOGNIZED: Text=" + text);
            if (listener != null) {
                listener.onTranscription(text, speakers);
            }

        } else if (e.getResult().getReason() == ResultReason.NoMatch) {
            LOGGER.info("NOMATCH: Speech could not be recognized.");
        }
    }

    /**
     * Handles the cancellation event of the speech recognition.
     *
     * @param s : The source of the event.
     * @param e : The event arguments containing the cancellation details.
     * @return
     */
    private void handleCancelEvent(Object s, SpeechRecognitionCanceledEventArgs e) {
        if (e.getErrorCode().equals(CancellationErrorCode.NoError)) return;
        LOGGER.error("CANCELED: Reason=" + e.getReason());
        LOGGER.error("CANCELED: ErrorCode=" + e.getErrorCode());
        LOGGER.error("CANCELED: ErrorDetails=" + e.getErrorDetails());
    }

}
