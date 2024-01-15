package com.qStivi.chatbot;

import com.theokanning.openai.completion.chat.ChatMessageRole;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.User;

import java.util.Set;

public interface IChatBot {
    String sendMessage(String message);
    String sendMessage(String message, Set<User> speakers);

    String sendMessage(ChatMessageRole role, String message);
}
