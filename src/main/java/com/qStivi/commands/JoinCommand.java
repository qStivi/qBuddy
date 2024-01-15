package com.qStivi.commands;

import com.qStivi.ChatBotApplication;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

public class JoinCommand implements Command {
    private static final Logger LOGGER = JDALogger.getLog(JoinCommand.class);
    private static final int MAX_CONNECTION_ATTEMPTS = 3;
    private static final long CONNECTION_WAIT_TIME = 1000;
    private final ChatBotApplication chatBotApp;

    public JoinCommand(ChatBotApplication chatBotApp) {

        this.chatBotApp = chatBotApp;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        var guild = checkNotNull(event.getGuild(), "Guild is null!", event);
        if (!openAudioConnection(event, guild)) return;

        setAudioHandlers(guild, chatBotApp);

        generateJoinMessage(event, chatBotApp);
    }

    private boolean openAudioConnection(SlashCommandInteractionEvent event, Guild guild) {
        var member = checkNotNull(event.getMember(), "You are not in a guild!", event);
        var voiceState = checkNotNull(member.getVoiceState(), "´CacheFlag.VOICE_STATE´ must be enabled!", event);
        var channel = checkNotNull(voiceState.getChannel(), "You are not in a voice channel!", event);

        LOGGER.info("Trying to join voice channel: " + channel.getName());
        guild.getAudioManager().openAudioConnection(channel);
        if (!waitForConnection(guild.getAudioManager())) {
            LOGGER.error("Failed to establish audio connection");
            return false;
        }
        LOGGER.info("Successfully joined voice channel: " + channel.getName());
        return true;
    }

    private <T> T checkNotNull(T object, String errorMessage, SlashCommandInteractionEvent event) {
        if (object == null) {
            event.getHook().sendMessage(errorMessage).queue();
            LOGGER.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        return object;
    }

    private boolean waitForConnection(AudioManager audioManager) {
        for (int i = 0; i < MAX_CONNECTION_ATTEMPTS; i++) {
            if (audioManager.isConnected()) {
                return true;
            }
            try {
                Thread.sleep(CONNECTION_WAIT_TIME);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    private void setAudioHandlers(Guild guild, ChatBotApplication chatBot) {
        var audioManager = guild.getAudioManager();
        audioManager.setReceivingHandler(chatBot.getAudioReceiveHandler());
        audioManager.setSendingHandler(chatBot.getAudioSendHandler());
    }

    private void generateJoinMessage(SlashCommandInteractionEvent event, ChatBotApplication chatBot) {
//        var joinMessage = chatBot.getChatBot().sendMessage(ChatMessageRole.SYSTEM, "Erstelle eine Begrüßungsnachricht. Sie sollte ein kurzer Satz sein, zum Beispiel: Hallo, ich bin wieder da!. Du kannst auch Referenzen aus der Popkultur verwenden. Die Anführungszeichen sollten nicht mit eingefügt werden. Du kannst auch Emojis verwenden.");
        var joinMessage = chatBot.getChatBot().sendMessage(ChatMessageRole.SYSTEM, " ");
        chatBot.getTts().speak(joinMessage);
        event.getHook().editOriginal(joinMessage).queue();
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("join", "Joins the voice channel and sets the audio handlers");
    }
}
