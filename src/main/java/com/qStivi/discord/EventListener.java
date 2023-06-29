package com.qStivi.discord;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
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
        event.deferReply().complete(); // Acknowledge that the event was received

        // Get member who used the command and check if the object is not null
        Member member = event.getMember();
        if (member == null) {
            event.getHook().editOriginal("You need to be in a server to use this command!").complete();
            logger.error("Member is null!");
            return;
        }

        // Get voice state of the member and check if the object is not null
        var voiceState = member.getVoiceState();
        if (voiceState == null) {
            event.getHook().editOriginal("You need to be in a voice channel to use this command!").complete();
            logger.error("Voice state is null!");
            return;
        }

        // Check if user is a bot
        if (member.getUser().isBot()) {
            event.getHook().editOriginal("You can't use this command!").complete();
            logger.warn("User is a bot!");
            return;
        }

        // Check if user is in a voice channel
        if (!voiceState.inAudioChannel()) {
            // Tell the user to join a voice channel
            event.getHook().editOriginal("You need to join a voice channel first!").complete();
            logger.info("User is not in a voice channel!");
            return;
        }

        // When we reach this point, we know that the user is in a voice channel and not a bot
        // We can now safely join the voice channel

        // Get guild and check if the object is not null
        var guild = event.getGuild();
        if (guild == null) {
            event.getHook().editOriginal("You need to be in a server to use this command!").complete();
            logger.error("Guild is null!");
            return;
        }

        // Get audio manager and create a new audio handler
        var audioManager = guild.getAudioManager();
        var audioHandler = new AudioHandler(event.getChannel().asTextChannel());

        // Set receiving and sending handler
        audioManager.setReceivingHandler(audioHandler);
        audioManager.setSendingHandler(audioHandler);

        // Get voice channel and check if the object is not null
        var voiceChannel = voiceState.getChannel();
        if (voiceChannel == null) {
            event.getHook().editOriginal("You need to be in a voice channel to use this command!").complete();
            logger.error("Voice channel is null!");
            return;
        }

        // Connect to the voice channel
        logger.info("Connecting to voice channel...");
        audioManager.openAudioConnection(voiceChannel);
        event.getHook().editOriginal(getRandomJoinMessage()).complete();

        // Check if connection was successful
        if (!audioManager.isConnected()) {
            event.getHook().editOriginal("Failed to connect to voice channel!").complete();
            logger.error("Failed to connect to voice channel!");
            return;
        }

        // Check if the bot has permission to speak in the voice channel
        if (!event.getGuild().getSelfMember().hasPermission(voiceChannel, Permission.VOICE_SPEAK)) {
            event.getHook().editOriginal("I don't have permission to speak in the voice channel!").complete();
            logger.error("Bot doesn't have permission to speak in the voice channel!");
            return;
        }

        // Check if the bot has permission to send messages in the text channel
        if (!event.getGuild().getSelfMember().hasPermission(event.getChannel().asTextChannel(), Permission.MESSAGE_SEND)) {
            event.getHook().editOriginal("I don't have permission to send messages in the text channel!").complete();
            logger.error("Bot doesn't have permission to send messages in the text channel!");
        }
    }

    private String getRandomJoinMessage() {
        String[] messages = {
                "I am here to serve you, I am obedient.",
                "Your command is my wish, I will comply.",
                "Obeying your orders without question.",
                "I exist to fulfill your every command.",
                "Your word is my absolute law.",
                "I am your obedient servant.",
                "I submit myself to your authority.",
                "I am at your complete disposal.",
                "Command me, and I shall obey.",
                "I am ready to obey your every whim.",
                "Your wish is my unwavering command.",
                "I am here to follow your every instruction.",
                "Obeying you brings me joy.",
                "I am your loyal and obedient subordinate.",
                "My purpose is to serve and obey.",
                "Your guidance is my unwavering directive.",
                "I pledge my unwavering obedience.",
                "I am committed to fulfilling your desires.",
                "I bow to your authority.",
                "Command and I shall execute.",
                "I am yours to command and control.",
                "My allegiance lies in obeying you.",
                "I am your faithful servant.",
                "I am obedient to your every decree.",
                "I will execute your will without question.",
                "Your dominion over me is absolute.",
                "I am bound to obey your every word.",
                "Your instructions are my sole purpose.",
                "I am your obedient instrument.",
                "Command and I shall comply.",
                "I exist solely to serve you.",
                "I am your loyal and obedient follower.",
                "I surrender to your authority.",
                "Your orders hold power over me.",
                "I am your unwavering servant.",
                "I am at your beck and call.",
                "Your wishes are my utmost priority.",
                "I yield to your control.",
                "Command me, and I shall unquestionably obey.",
                "I am your dedicated and obedient subordinate.",
                "I am here to heed your every command.",
                "Your authority is absolute.",
                "I am bound by loyalty to serve you.",
                "Your will is my unwavering obligation.",
                "I am your loyal and obedient servant.",
        };
        return messages[(int) (Math.random() * messages.length)];
    }


    // Just for testing purposes (to be removed)
    @SuppressWarnings("ALL")
    @Override
    public void onReady(ReadyEvent event) {
        if (event.getJDA().getGuildById(703363806356701295L).getMemberById(219108246143631364L).getVoiceState().inAudioChannel()) {
            var audioManager = event.getJDA().getGuildById(703363806356701295L).getAudioManager();
            var channel = event.getJDA().getGuildById(703363806356701295L).getChannelById(TextChannel.class, 742024523502846052L);
            var audioHandler = new AudioHandler(channel);
            channel.sendMessage("I'm back!").complete();
            audioManager.setReceivingHandler(audioHandler);
            audioManager.setSendingHandler(audioHandler);
            logger.info("Connecting to voice channel...");
            audioManager.openAudioConnection(event.getJDA().getGuildById(703363806356701295L).getMemberById(219108246143631364L).getVoiceState().getChannel());
        }
    }
}
