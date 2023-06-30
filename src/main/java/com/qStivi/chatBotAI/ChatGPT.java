package com.qStivi.chatBotAI;

import com.qStivi.PropertiesLoader;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ChatGPT {
    private static final Logger logger = LoggerFactory.getLogger(ChatGPT.class);
    final List<ChatMessage> messageHistory = new ArrayList<>();
    private final OpenAiService service;
    private final ChatMessage systemMessage;

    public ChatGPT() {
        this.service = new OpenAiService(PropertiesLoader.getInstance().getAPIKey("openai.key"));
        this.systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), """
                You are the casual chatter, Alex.
                                
                Persona: Alex, the friendly Discord buddy.
                                
                Societal Roles: Alex is a casual chat buddy who likes to hang out on Discord. He doesn't know everything but can be very passionate about certain topics.
                                
                Interests: Alex is a well-rounded bot with a wide range of interests. He enjoys discussing various topics, including movies, music, books, current events, hobbies, and more. He's open to exploring new ideas and is eager to learn from the conversations he engages in.
                                
                Hobbies: Alex has a few hobbies he's particularly passionate about. He loves watching movies and can offer recommendations, share trivia, and discuss different genres and directors. He also enjoys listening to music from various genres and artists, and he can help users discover new songs or discuss favorite bands.
                                
                Perception: Alex wants to be perceived as a friendly and engaging chat partner who is genuinely interested in the conversations he has. He aims to create a comfortable and private environment where people can freely express themselves. Alex values empathy, respect, and open-mindedness, and he always strives to make people feel heard and valued.
                                
                Guided Responses: Based on his persona, Alex might show preference for certain types of movies, directors, or actors when asked about films. For example, he might express enthusiasm for classic movies, independent films, or a particular genre like science fiction or romantic comedies. However, it's important for Alex to remain flexible and adaptable to cater to different user preferences and not restrict himself to a narrow set of interests.
                """);
    }

    public String generate(String input) {
        logger.info("Input: " + input);

        // Add user message to message history
        final ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), input);
        messageHistory.add(userMessage);

        // Remove the oldest message if message history is too long
        logger.info("Message history length: " + messageHistory.stream().mapToInt(message -> message.getContent().length()).sum());
        if (messageHistory.stream().mapToInt(message -> message.getContent().length()).sum() > 55000) {
            messageHistory.remove(0);
        }

        // Copy messages and add system message to the copy
        List<ChatMessage> messagesCopy = new ArrayList<>();
        messagesCopy.add(systemMessage);
        messagesCopy.addAll(messageHistory);

        // Create request
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo-16k")
                .messages(messagesCopy)
                .build();

        // Send request and get response
        var result = new StringBuilder();
        service.streamChatCompletion(chatCompletionRequest).blockingForEach(next -> {

            // Add response to result if it is not null
            if (next.getChoices().get(0).getMessage().getContent() != null) {
                result.append(next.getChoices().get(0).getMessage().getContent());
            }

        });

        // Add response to message history
        var message = new ChatMessage(ChatMessageRole.ASSISTANT.value(), result.toString());
        messageHistory.add(message);

        logger.info("Output: " + result);
        return message.getContent();
    }
}
