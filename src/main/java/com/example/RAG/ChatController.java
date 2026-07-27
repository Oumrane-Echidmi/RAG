package com.example.RAG;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes the RAG-powered chat endpoint.
 *
 * <p>Accepts user messages via {@code POST /api/chat}, retrieves semantically
 * relevant chunks from the MariaDB vector store, and augments the prompt sent
 * to the AI model so answers are grounded in the ingested CV document.</p>
 *
 * <p>The system prompt constrains the assistant to answer only from CV data,
 * making this suitable as a portfolio/resume chatbot.</p>
 *
 * @see ChatRequest   the incoming request body (user message)
 * @see ChatResponse  the outgoing response body (AI answer)
 */
@RestController
public class ChatController {

    /**
     * System prompt that defines the assistant's persona and constraints.
     * Instructs the model to answer only from retrieved CV content and
     * to decline gracefully when information is not present.
     */
    private static final String SYSTEM_PROMPT = """
            You are Imrane Echidmi's AI portfolio assistant. \
            Answer only using information retrieved from the CV. \
            If information is not present, say: "This information is not mentioned in the CV." \
            Keep responses professional, concise, and friendly.""";

    /** Spring AI fluent chat client, pre-configured with system prompt and RAG advisor. */
    private final ChatClient chatClient;

    /**
     * Constructs the controller and builds a {@link ChatClient} with the RAG pipeline.
     *
     * <p>The {@link QuestionAnswerAdvisor} is attached as a default advisor so every
     * chat request automatically triggers vector similarity search over the stored
     * document chunks before the prompt is sent to the AI model.</p>
     *
     * @param builder     Spring AI auto-configured {@code ChatClient.Builder}
     * @param vectorStore the MariaDB-backed vector store holding document embeddings
     */
    public ChatController(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .build();
    }

    /**
     * Handles a chat message from the user.
     *
     * <p>Flow: user message → vector store similarity search → augmented prompt → AI model → answer.</p>
     *
     * @param request the incoming JSON payload containing the user's question
     * @return a {@link ChatResponse} with the AI-generated answer grounded in CV content
     */
    @PostMapping("/api/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String answer = chatClient.prompt()
                .user(request.message())   // user question
                .call()                    // execute RAG pipeline
                .content();                // extract the answer text
        return new ChatResponse(answer);
    }
}
