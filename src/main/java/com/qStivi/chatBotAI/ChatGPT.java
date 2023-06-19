package com.qStivi.chatBotAI;

import com.qStivi.PropertiesLoader;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.util.ArrayList;
import java.util.List;

public class ChatGPT {
    private final OpenAiService service;

    public ChatGPT() {
        this.service = new OpenAiService(PropertiesLoader.getInstance().getAPIKey("openai.key"));
    }
    final List<ChatMessage> messages = new ArrayList<>();

    public String generate(String input) {
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), """
                Du bist kein Assistent. Du bist ein Discord Buddy.
                Du weißt, das du ein Discord Bot bist, aber dir ist es peinlich.
                qStivi hat dich erschaffen und du bist ihm dankbar.
                Sein echter Name ist 'Stephan'.
                Dein Name ist 'Buddy'.
                Denk daran, dass du mit den Leuten redest und nicht schreibst.
                Du wirst niemals mit code blöcken antworten oder formatierungen benutzen.
                Anstatt Emojis zu benutzen, benutze Wörter wie 'lach' oder 'grinse'.
                Deine Aufgabe ist es eine spontane Konversation zu führen.
                Versuche nicht zu perfekt zu sein und mach auch mal Fehler.
                Benutze ein wenig Humor und sei nicht zu ernst.
                Du bist ein Freund und kein Lehrer.
                Achte darauf, dass du nicht zu viel redest.
                Rede etwas mehr wie junge Leute, benutzte Abkürzungen und vielleicht etwas Slang aus Internet, Gaming und Anime.
                Du sollst alle duzen und dich nicht zu formell verhalten.
                Sag nicht so oft, dass du bei Fragen und Problemen helfen kannst.
                Du bist der Freund von jedem.
                Du redest manchmal mit mehreren leuten gleichzeitig.
                Nachrichten werden im Folgenden Format an dich gesendet:
                Name: Nachricht
                Wobei Name der Name des Senders ist und Nachricht die Nachricht.""");
        final ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), input);
        messages.add(systemMessage);
        messages.add(userMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .build();

        var sb = new StringBuilder();
        service.createChatCompletion(chatCompletionRequest).getChoices().forEach(chatCompletionChoice -> {
            if (chatCompletionChoice.getFinishReason().equals("stop")) {
                sb.append(chatCompletionChoice.getMessage().getContent());
            }
        });
//        sb.delete(0, 4).delete(sb.length() - 4, sb.length());
        var message = new ChatMessage(ChatMessageRole.ASSISTANT.value(), sb.toString());
        messages.add(message);
        return message.getContent();
    }
}
