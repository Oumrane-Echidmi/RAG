package com.example.RAG;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Sanity test that verifies the Spring application context loads successfully.
 *
 * <p>This test requires all external dependencies (MariaDB, Ollama) to be
 * available. It will fail if the database is unreachable, the OpenAI/DeepSeek
 * API key is invalid, or the embedding model is not running.</p>
 */
@SpringBootTest
class RagApplicationTests {

    /**
     * Loads the full Spring context and validates that all beans are created
     * without errors.
     */
    @Test
    void contextLoads() {
        // If this passes, the wiring between ChatController, IngestionService,
        // VectorStore, embedding model, and chat model is correct.
    }

}
