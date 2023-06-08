package com.qStivi;

import com.qStivi.discord.Bot;
import com.qStivi.stt.MicrosoftSTT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        logger.info("Starting...");
        new Bot();
    }
}
