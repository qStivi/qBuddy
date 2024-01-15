package com.qStivi.config;

import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ApiKeyManager {
    private static final Logger LOGGER = JDALogger.getLog(ApiKeyManager.class);
    private static final String CONFIG_FILE = "api_keys.properties";

    public static Properties loadApiKeys() {
        Properties prop = new Properties();
        File file = new File(CONFIG_FILE);

        if (file.exists()) {
            try (FileInputStream input = new FileInputStream(file)) {
                prop.load(input);
            } catch (IOException e) {
                LOGGER.error("Error reading API keys file!", e);
            }
        }

        checkAndSetMissingKeys(prop);

        return prop;
    }

    private static void checkAndSetMissingKeys(Properties prop) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {

            boolean keysUpdated = false;

            Map<String, String> keyPrompts = getKeyPrompts();

            for (Map.Entry<String, String> entry : keyPrompts.entrySet()) {
                if (!prop.containsKey(entry.getKey())) {
                    String key = promptForKey(reader, entry.getValue());
                    prop.setProperty(entry.getKey(), key);
                    keysUpdated = true;
                }
            }

            if (keysUpdated) {
                try (FileOutputStream output = new FileOutputStream(CONFIG_FILE)) {
                    prop.store(output, null);
                } catch (IOException e) {
                    LOGGER.error("Error writing to API keys file: ", e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error reading input: ", e);
        }
    }

    private static Map<String, String> getKeyPrompts() {
        Map<String, String> keyPrompts = new HashMap<>();
        keyPrompts.put("microsoft_subscriptionKey", "Enter Microsoft subscription key: ");
        keyPrompts.put("microsoft_region", "Enter Microsoft region: ");
        keyPrompts.put("openai_apiKey", "Enter OpenAI API key: ");
        keyPrompts.put("discord_token", "Enter Discord token: ");
        // Add more keys here...
        return keyPrompts;
    }

    private static String promptForKey(BufferedReader reader, String prompt) throws IOException {
        System.out.print(prompt);
        return reader.readLine();
    }
}
