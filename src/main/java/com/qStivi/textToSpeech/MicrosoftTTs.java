package com.qStivi.textToSpeech;

import com.microsoft.cognitiveservices.speech.*;
import com.qStivi.PropertiesLoader;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;


public class MicrosoftTTs {
    public AudioInputStream speak(String text) {
        SpeechConfig speechConfig = SpeechConfig.fromSubscription(PropertiesLoader.getInstance().getAPIKey("microsoft.speech.key1"), PropertiesLoader.getInstance().getAPIKey("microsoft.speech.region"));
        speechConfig.setSpeechSynthesisLanguage("de-DE");
        SpeechSynthesizer speechSynthesizer = new SpeechSynthesizer(speechConfig, null);

        speechConfig.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Audio24Khz16Bit48KbpsMonoOpus);

        SpeechSynthesisResult result = speechSynthesizer.SpeakText(text);

        var bytes = result.getAudioData();

        return new AudioInputStream(new ByteArrayInputStream(bytes), new AudioFormat(24000, 16, 1, true, false), bytes.length);
    }


}
