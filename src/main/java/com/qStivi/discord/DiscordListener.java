package com.qStivi.discord;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscordListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DiscordListener.class);

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply().complete();
        // Check if user is in a voice channel
        var member = event.getMember();
        assert member != null;
        var state = member.getVoiceState();
        assert state != null;
        if (state.inAudioChannel()) {
            // Join the voice channel
            var guild = event.getGuild();
            assert guild != null;
            var audioManager = guild.getAudioManager();
            AudioHandler audioHandler = null;
            audioHandler = new AudioHandler();
            // var audioHandler = new EchoHandler();
            audioManager.setReceivingHandler(audioHandler);
            audioManager.setSendingHandler(audioHandler);
            logger.info("Connecting to voice channel...");
            audioManager.openAudioConnection(state.getChannel());
            event.getHook().editOriginal("Yes master!").complete();
        } else {
            // Tell the user to join a voice channel
            event.getHook().editOriginal("You need to join a voice channel first!").complete();
        }
    }
}
