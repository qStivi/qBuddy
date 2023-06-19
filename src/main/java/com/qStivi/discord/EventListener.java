package com.qStivi.discord;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventListener extends ListenerAdapter {
    private final static Logger logger = LoggerFactory.getLogger(EventListener.class);

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        event.deferReply().complete();
        // Check if user is in a voice channel
        if (event.getMember().getVoiceState().inAudioChannel()) {
            // Join the voice channel
            var audioManager = event.getGuild().getAudioManager();
            var audioHandler = new AudioHandler();
//                            var audioHandler = new EchoHandler();
            audioManager.setReceivingHandler(audioHandler);
            audioManager.setSendingHandler(audioHandler);
            logger.info("Connecting to voice channel...");
            audioManager.openAudioConnection(event.getMember().getVoiceState().getChannel());
            event.getHook().editOriginal("Yes master!").complete();
        } else {
            // Tell the user to join a voice channel
            event.getHook().editOriginal("You need to join a voice channel first!").complete();
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        if (event.getJDA().getGuildById(703363806356701295L).getMemberById(219108246143631364L).getVoiceState().inAudioChannel()) {
            var audioManager = event.getJDA().getGuildById(703363806356701295L).getAudioManager();
            var audioHandler = new AudioHandler();
            event.getJDA().getGuildById(703363806356701295L).getChannelById(TextChannel.class, 742024523502846052L).sendMessage("I'm back!").complete();
            audioManager.setReceivingHandler(audioHandler);
            audioManager.setSendingHandler(audioHandler);
            logger.info("Connecting to voice channel...");
            audioManager.openAudioConnection(event.getJDA().getGuildById(703363806356701295L).getMemberById(219108246143631364L).getVoiceState().getChannel());
        }
    }
}
