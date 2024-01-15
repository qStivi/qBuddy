package com.qStivi.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class PingCommand implements Command {
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long ping = event.getJDA().getGatewayPing();
        event.reply("Ping: " + ping + "ms").queue();
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("ping", "Calculate ping of the bot");
    }
}
