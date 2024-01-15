package com.qStivi.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

public class ShutdownCommand implements Command {
    private static final Logger LOGGER = JDALogger.getLog(ShutdownCommand.class);

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        LOGGER.info("Shutting down...");

        event.getJDA().getGuilds().forEach(guild -> guild.getAudioManager().closeAudioConnection());

        event.reply("Shutting down...").complete();

        event.getJDA().shutdown();
        System.exit(0);
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("shutdown", "Shuts down the bot");
    }
}
