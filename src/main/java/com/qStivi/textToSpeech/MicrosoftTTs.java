package com.qStivi.textToSpeech;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.qStivi.PropertiesLoader;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class MicrosoftTTs {

    private static final String speechKey = PropertiesLoader.getInstance().getAPIKey("microsoft.speech.key1");
    private static final String speechRegion = PropertiesLoader.getInstance().getAPIKey("microsoft.speech.region");
    private final Guild guild;

    public MicrosoftTTs(Guild guild) {
        this.guild = guild;
    }

    public List<byte[]> synthesizeTextToAudio(String text) {
        SpeechConfig config = SpeechConfig.fromSubscription(speechKey, speechRegion);
        config.setSpeechSynthesisLanguage("de-DE");
        var fileName = System.currentTimeMillis() + "O.wav";
        AudioConfig audioConfig = AudioConfig.fromWavFileOutput(fileName);

        SpeechSynthesizer speechSynthesizer = new SpeechSynthesizer(config, audioConfig);
        speechSynthesizer.SpeakSsml(text);
        try {
            return get20msAudioChunks(new File(fileName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<byte[]> get20msAudioChunks(File waveFile) throws Exception {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(AudioSendHandler.INPUT_FORMAT, AudioSystem.getAudioInputStream(waveFile));
        AudioFormat format = audioInputStream.getFormat();
        int bytesPerFrame = format.getFrameSize();
        float frameRate = format.getFrameRate();

        // calculate the number of bytes that represent 20ms of audio
        int numBytes = (int) (0.02f * frameRate * bytesPerFrame);
        byte[] audioBytes = new byte[numBytes];

        // read the audio data in 20ms chunks into byte arrays
        List<byte[]> chunks = new ArrayList<>();
        int numBytesRead = 0;
        while ((numBytesRead = audioInputStream.read(audioBytes)) != -1) {
            chunks.add(Arrays.copyOf(audioBytes, numBytesRead));
        }

        waveFile.delete();

        return chunks;
    }


}
