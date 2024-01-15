package com.qStivi.chatbot;

import com.qStivi.Main;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.messages.MessageRequest;
import com.theokanning.openai.runs.Run;
import com.theokanning.openai.runs.RunCreateRequest;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.threads.ThreadRequest;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.RichPresence;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class OpenAIChatBot implements IChatBot {
    private static final Logger LOGGER = JDALogger.getLog(OpenAIChatBot.class);
    private final static Random RANDOM = new Random();
    private final OpenAiService service;
    private final List<String> moods = List.of("(Bitte antworte auf eine freche Art und Weise)", "(Bitte antworte sarkastisch)", "(Führe mich mit deiner Antwort an der Nase herum)", "(Bitte antworte wütend)", "(Die Blähungen setzen ein!)");
    private static final String ASSISTANT_ID = "asst_DY0gEKhlE9E4pdQvmQhF2Qxq";
    private final String threadID;

    public OpenAIChatBot(String apiKey) {
        this.service = new OpenAiService(apiKey);
        threadID = getThreadID();
//        addToMemory(new ChatMessage(ChatMessageRole.SYSTEM.value(), INITIAL_MESSAGE));
    }

    /**
     * Retrieves the thread ID from the file "threadID.txt" in the current working directory.
     * @return The thread ID.
     */
    private String getThreadID() {
        try {
        var fileReader = new FileReader("threadID.txt");
        var bufferedReader = new java.io.BufferedReader(fileReader);
            return bufferedReader.readLine();
        } catch (FileNotFoundException ignored) {
            LOGGER.warn("File threadID.txt not found!");
            var id = service.createThread(ThreadRequest.builder().build()).getId();
            LOGGER.info("Creating file threadID.txt with ID: {}", id);
            try {
                var fileWriter = new java.io.FileWriter("threadID.txt");
                fileWriter.write(id);
                fileWriter.close();
                return id;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void addMoodPrompt() {
        if (RANDOM.nextInt(10) < 10) { // 100% chance to append a keyword
            var message = moods.get(RANDOM.nextInt(moods.size()));
            message += " (If you can't do anything with this message, just ignore it)";
//            addToMemory(new ChatMessage(ChatMessageRole.SYSTEM.value(), message));
        }
    }

    @Override
    public String sendMessage(String message) {
        return sendMessage(ChatMessageRole.USER, message);
    }

    @Override
    public String sendMessage(String message, Set<User> speakers) {
//        addToMemory(createChatMessage(ChatMessageRole.SYSTEM, String.format("Currently talking are %s", speakers.stream().map(User::getName).reduce((a, b) -> a + ", " + b).orElse("no one"))));
        return sendMessage(ChatMessageRole.USER, message);
    }

    @Override
    public String sendMessage(ChatMessageRole role, String message) {
        addUserActivityInformation();

        addMoodPrompt();

//        addToMemory(createChatMessage(role, message));

        var botMessage = getResponse(message);

//        purgeMemory();

        return botMessage;
    }

    private String getResponse(String message) {

        service.createMessage(threadID, MessageRequest.builder().role("user").content(message).build());

        var run = service.createRun(threadID, RunCreateRequest.builder().assistantId(ASSISTANT_ID).build());

        waitForRunToComplete(run);

        var response = getNewestMessageInThread();

        LOGGER.info("Response: {}", response);
        return response.toString();
    }

    @NotNull
    private StringBuilder getNewestMessageInThread() {
        var response = new StringBuilder();
        var messages = service.listMessages(threadID);
        var retrieveMessage = service.retrieveMessage(threadID, messages.getFirstId());
        for (var messageRequest : retrieveMessage.getContent()) {
            response.append(messageRequest.getText().getValue()).append("\n");
        }
        return response;
    }

    private void waitForRunToComplete(Run run) {
        var lastRunStatus = "";
        while (run.getStatus() == null || !run.getStatus().equals("completed")) {
            if (run.getStatus() != null && !run.getStatus().equals(lastRunStatus)) {
                LOGGER.info("Run status: {}", run.getStatus());
                lastRunStatus = run.getStatus();
            }
            run = service.retrieveRun(threadID, run.getId());
        }
    }

    private void addUserActivityInformation() {
        // Add user activity of all users in channel to memory
        StringBuilder userActivities = new StringBuilder();

        Main.jda.getGuilds().forEach(guild -> guild.getVoiceChannels().forEach(voiceChannel -> {
            if (voiceChannel.getMembers().stream().map(ISnowflake::getId).anyMatch(id -> id.equals(Main.jda.getSelfUser().getId()))) {
                voiceChannel.getMembers().forEach(member -> {
                    if (!member.getActivities().isEmpty()) {
                        var activities = member.getActivities();
                        for (var activity : activities) {
                            RichPresence richPresence = activity.asRichPresence();
                            String details = richPresence != null ? richPresence.getDetails() : "";
                            String state = activity.getState();
                            if (state == null) {
                                state = "";
                            } else {
                                state = state + " on ";
                            }
                            userActivities.append("\n").append(member.getEffectiveName()) // qStivi
                                    .append(" is ") // is
                                    .append(activity.getType().name()).append(" ") // listening
                                    .append(details).append(" ") // beat it
                                    .append(state) // Michael Jackson on
                                    .append(activity.getName()); // Spotify
                        }
                    }
                });
            }
        }));

        String activityMessage = """
                (Du sieht, die Discord activity der Benutzer mit denen du chattest.
                """ + userActivities + ")";

//        addToMemory(createChatMessage(ChatMessageRole.SYSTEM, activityMessage));
    }
}
