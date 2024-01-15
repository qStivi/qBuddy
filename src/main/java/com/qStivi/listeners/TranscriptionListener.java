package com.qStivi.listeners;

import net.dv8tion.jda.api.entities.User;

import java.util.Set;

public interface TranscriptionListener {
    void onTranscription(String text, Set<User> speakers);
}
