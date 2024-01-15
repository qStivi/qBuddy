package com.qStivi.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class CommandHandler extends ListenerAdapter {
    private static final Logger LOGGER = JDALogger.getLog(CommandHandler.class);
    private final Map<String, Command> commandMap = new ConcurrentHashMap<>();
    private final List<CommandData> commands = Collections.synchronizedList(new ArrayList<>());
    private final ThreadFactory namedThreadFactory = new CommandThreadFactory();
    private final ExecutorService executorService = Executors.newCachedThreadPool(namedThreadFactory);
    private final JDA jda;

    public CommandHandler(JDA jda) {
        this.jda = jda;
    }

    public void register(String name, Command command) {
        commandMap.put(name, command);
        commands.add(command.getCommandData());
    }

    public void registerAll() {
        LOGGER.info("Registering commands...");
        jda.updateCommands().addCommands(commands).complete();
        var jdaCommands = jda.retrieveCommands().complete();
        for (var command : jdaCommands) {
            LOGGER.info("Command registered: " + command.getName());
        }
        if (jdaCommands.size() != commands.size()) {
            LOGGER.warn("Not all commands were registered!");
        } else {
            LOGGER.info("All commands registered successfully!");
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        Command command = commandMap.get(commandName);
        if (command != null) {
            ((CommandThreadFactory) namedThreadFactory).setCommandName(commandName + " Command Thread");
            Thread task = namedThreadFactory.newThread(() -> {
                try {
                    command.execute(event);
                    LOGGER.info("Command executed: " + commandName);
                } catch (Exception e) {
                    LOGGER.error("Error executing command: " + commandName, e);
                }
            });
            executorService.submit(task);
        } else {
            LOGGER.warn("Unknown command attempted: " + commandName);
        }
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    static class CommandThreadFactory implements ThreadFactory {
        private String commandName;

        public Thread newThread(@NotNull Runnable r) {
            Thread t = new Thread(r);
            t.setName(commandName);
            return t;
        }

        public void setCommandName(String commandName) {
            this.commandName = commandName;
        }
    }
}
