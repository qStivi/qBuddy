package com.qStivi.discord;

import com.qStivi.PropertiesLoader;
import com.qStivi.audio.lavaplayer.AudioInputStreamSourceManager;
import com.qStivi.audio.lavaplayer.LoadResultHandler;
import com.qStivi.audio.lavaplayer.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
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


        DefaultAudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        AudioPlayer audioPlayer = playerManager.createPlayer();
        TrackScheduler trackScheduler = new TrackScheduler(audioPlayer);
        AudioSourceManagers.registerLocalSource(playerManager);
        audioPlayer.addListener(trackScheduler);
        playerManager.registerSourceManager(new AudioInputStreamSourceManager());

        logger.info("Registering listeners...");
        jda.addEventListener(new EventListener(playerManager, trackScheduler, audioPlayer));
        logger.info("Bot created!");
    }
}
