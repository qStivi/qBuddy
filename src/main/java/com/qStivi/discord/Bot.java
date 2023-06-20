package com.qStivi.discord;

import com.qStivi.PropertiesLoader;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bot {
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);


    public Bot() {
        logger.info("Creating Bot...");
        var properties = PropertiesLoader.getInstance();

        logger.info("Creating JDA instance...");
        var jda = JDABuilder.createLight(properties.getAPIKey("discord.token"))
                .enableCache(CacheFlag.VOICE_STATE)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES)
                .setMemberCachePolicy(MemberCachePolicy.VOICE)
                .build();

        logger.info("Registering commands...");
        jda.upsertCommand(
                Commands.slash("join", "Joins your voice channel")
        ).complete();

        logger.info("Registering listeners...");
        jda.addEventListener(new EventListener());
        logger.info("Bot created!");
    }
}
