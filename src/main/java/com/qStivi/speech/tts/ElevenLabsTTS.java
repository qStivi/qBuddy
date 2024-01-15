package com.qStivi.speech.tts;

import com.qStivi.Main;
import com.qStivi.audio.AudioConverter;
import com.qStivi.audio.DiscordAudioSendHandler;
import net.andrewcpu.elevenlabs.ElevenLabs;
import net.andrewcpu.elevenlabs.builders.SpeechGenerationBuilder;
import net.andrewcpu.elevenlabs.enums.GeneratedAudioOutputFormat;
import net.andrewcpu.elevenlabs.enums.StreamLatencyOptimization;
import net.andrewcpu.elevenlabs.model.voice.Voice;
import net.andrewcpu.elevenlabs.model.voice.VoiceSettings;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.InputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ElevenLabsTTS implements ITextToSpeech {

    private final DiscordAudioSendHandler sendHandler;

    private static final Logger LOGGER = JDALogger.getLog(ElevenLabsTTS.class);

    public ElevenLabsTTS(DiscordAudioSendHandler sendHandler) {
        this.sendHandler = sendHandler;

        ElevenLabs.setApiKey("5cdde502548c04e9497a0cb325c6ee4d");
//        Voice.getVoices().forEach(voice -> LOGGER.info(voice.toString()));
    }

    @Override
    public void speak(String text) {

        var randomVoice = Voice.getVoices().get(Math.toIntExact(Math.round(Math.random() * Voice.getVoices().size())));
        LOGGER.info("Using voice: " + randomVoice.toString());

        ElevenLabs.setDefaultModel("eleven_multilingual_v2"); // Optional, defaults to: "eleven_monolingual_v1"

        try (InputStream inputStream = SpeechGenerationBuilder.textToSpeech().streamed().setText(text).setGeneratedAudioOutputFormat(GeneratedAudioOutputFormat.PCM_24000).setLatencyOptimization(StreamLatencyOptimization.MAX_NO_TEXT_NORMALIZATION).setVoiceId(randomVoice.getVoiceId()).build()) {
            sendHandler.offer(AudioConverter.convert(inputStream.readAllBytes(), new AudioFormat(24000, 16, 1, true, false), AudioSendHandler.INPUT_FORMAT));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
