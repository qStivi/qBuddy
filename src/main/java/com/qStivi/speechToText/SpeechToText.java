package com.qStivi.speechToText;

import com.qStivi.chatBotAI.IChatBotAI;

public abstract class SpeechToText implements ISpeechToText {
    private final IChatBotAI chatBotAI;

    protected SpeechToText(IChatBotAI chatBotAI) {
        this.chatBotAI = chatBotAI;
    }
}
