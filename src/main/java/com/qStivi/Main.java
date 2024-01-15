package com.qStivi;

import com.qStivi.commands.CommandHandler;
import com.qStivi.commands.JoinCommand;
import com.qStivi.commands.PingCommand;
import com.qStivi.commands.ShutdownCommand;
import com.qStivi.config.ApiKeyManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class Main extends ListenerAdapter {

    // Add the ability for the bot to use user activity as input for the chatbot.
    private static final Logger LOGGER = JDALogger.getLog(Main.class);
    public static JDA jda;

    public static void main(String[] args) {
        try {
            Properties apiKeys = loadApiKeys();


            jda = initializeJDA(apiKeys);

            var chatBotApp = new ChatBotApplication();
            registerCommands(jda, chatBotApp);
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Exception occurred: " + e.getMessage());
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static void registerCommands(JDA jda, ChatBotApplication chatBotApp) throws IOException {
        CommandHandler commandHandler = new CommandHandler(jda);
        jda.addEventListener(commandHandler);
        commandHandler.register("ping", new PingCommand());
        commandHandler.register("join", new JoinCommand(chatBotApp));
        commandHandler.register("shutdown", new ShutdownCommand());
        commandHandler.registerAll();
    }

    private static JDA initializeJDA(Properties apiKeys) throws InterruptedException {
        String token = apiKeys.getProperty("discord_token");
        var jda = JDABuilder.createDefault(token).enableIntents(GatewayIntent.GUILD_PRESENCES).enableCache(CacheFlag.ACTIVITY).build();
        jda.awaitReady();
        LOGGER.info("JDA Initialized Successfully");
        return jda;
    }

    private static Properties loadApiKeys() throws IOException {
        Properties apiKeys = ApiKeyManager.loadApiKeys();
        LOGGER.info("API Keys Loaded Successfully");
        return apiKeys;
    }
}
