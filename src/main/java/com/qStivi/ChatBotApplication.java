package com.qStivi;

import com.qStivi.audio.DiscordAudioReceiveHandler;
import com.qStivi.audio.DiscordAudioSendHandler;
import com.qStivi.chatbot.IChatBot;
import com.qStivi.chatbot.OpenAIChatBot;
import com.qStivi.config.ApiKeyManager;
import com.qStivi.listeners.TranscriptionListener;
import com.qStivi.speech.stt.ISpeechToText;
import com.qStivi.speech.stt.MicrosoftSTT;
import com.qStivi.speech.tts.ElevenLabsTTS;
import com.qStivi.speech.tts.ITextToSpeech;
import com.qStivi.speech.tts.MicrosoftTTS;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import net.dv8tion.jda.api.entities.User;

import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ChatBotApplication implements TranscriptionListener {
    private final ISpeechToText stt;
    private final IChatBot chatBot;
    private final ITextToSpeech tts;

    public ISpeechToText getStt() {
        return stt;
    }

    public IChatBot getChatBot() {
        return chatBot;
    }

    public ITextToSpeech getTts() {
        return tts;
    }

    public DiscordAudioReceiveHandler getAudioReceiveHandler() {
        return audioReceiveHandler;
    }

    public DiscordAudioSendHandler getAudioSendHandler() {
        return audioSendHandler;
    }

    private final DiscordAudioReceiveHandler audioReceiveHandler;
    private final DiscordAudioSendHandler audioSendHandler;
    public ChatBotApplication() throws ExecutionException, InterruptedException {
        var keys = ApiKeyManager.loadApiKeys();
        String apiKey = keys.getProperty("openai_apiKey");
        String subscriptionKey = keys.getProperty("microsoft_subscriptionKey");
        String region = keys.getProperty("microsoft_region");

        this.audioReceiveHandler = new DiscordAudioReceiveHandler();
        this.audioSendHandler = new DiscordAudioSendHandler();

        this.stt = new MicrosoftSTT(subscriptionKey, region, this.audioReceiveHandler);
//        this.tts = new MicrosoftTTS(subscriptionKey, region, this.audioSendHandler);
        this.tts = new ElevenLabsTTS(this.audioSendHandler);
        this.chatBot = new OpenAIChatBot(apiKey);

        this.audioReceiveHandler.addSilenceListener(() -> this.tts.speak(this.chatBot.sendMessage(ChatMessageRole.SYSTEM, "(Es hat niemand für ne Weile gesprochen)")));
        this.audioSendHandler.addAudioProvidedListener(this.audioReceiveHandler::resetLastAudioReceivedTime);

        stt.setTranscriptionListener(this);
        stt.startTranscription();
    }

    @Override
    public void onTranscription(String text, Set<User> speakers) {
        String chatBotResponse = chatBot.sendMessage(text, speakers);
        tts.speak(chatBotResponse);
    }
}
