package com.example.RAG;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private static final String SYSTEM_PROMPT = """
            You are Imrane Echidmi's AI portfolio assistant. \
            Answer only using information retrieved from the CV. \
            If information is not present, say: "This information is not mentioned in the CV." \
            Keep responses professional, concise, and friendly.""";

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .build();
    }

    @PostMapping("/api/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String answer = chatClient.prompt()
                .user(request.message())
                .call()
                .content();
        return new ChatResponse(answer);
    }
}
