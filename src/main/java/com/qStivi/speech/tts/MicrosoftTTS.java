package com.qStivi.speech.tts;

import com.microsoft.cognitiveservices.speech.*;
import com.qStivi.audio.AudioConverter;
import com.qStivi.audio.DiscordAudioSendHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import javax.sound.sampled.AudioFormat;
import java.util.concurrent.ExecutionException;

public class MicrosoftTTS implements ITextToSpeech {

    private static final Logger LOGGER = JDALogger.getLog(MicrosoftTTS.class);
    SpeechSynthesizer synthesizer;
    DiscordAudioSendHandler sendHandler;

    public MicrosoftTTS(String speechSubscriptionKey, String serviceRegion, DiscordAudioSendHandler sendHandler) {
        // Creates an instance of a speech config with specified subscription key and service region.
        SpeechConfig config = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion);
//        config.setSpeechSynthesisVoiceName("en-US-AvaMultilingualNeural");
//        config.setSpeechSynthesisVoiceName("de-DE-KatjaNeural");
        config.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Raw48Khz16BitMonoPcm);

        // Creates a speech synthesizer for later use.
        this.synthesizer = new SpeechSynthesizer(config, null);

        // Creates a connection to microsoft, so it can cache the connection.
        var connection = Connection.fromSpeechSynthesizer(synthesizer);
        connection.openConnection(true);

        this.sendHandler = sendHandler;
    }

    public void speak(String text)  {

        try {
            // Wrap the text in SSML and slow down the speech rate
            String ssml = """
                    <speak version="1.0" xmlns="http://www.w3.org/2001/10/synthesis" xml:lang="en-US">
                        <voice name="en-US-AvaMultilingualNeural">
                            <prosody rate="-40.00%">
                                <prosody pitch="-50%">
                                    %s
                                </prosody>
                            </prosody>
                        </voice>
                    </speak>
                    """.replace("%s", text);

            var result = synthesizer.StartSpeakingSsmlAsync(ssml);

            AudioDataStream stream = AudioDataStream.fromResult(result.get());

            byte[] buffer = new byte[16000];
            while (stream.readData(buffer) != 0) {
                var converted = AudioConverter.convert(buffer, new AudioFormat(48000, 16, 1, true, false), AudioSendHandler.INPUT_FORMAT);
                sendHandler.offer(converted);
            }

//            synthesizer.close();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
