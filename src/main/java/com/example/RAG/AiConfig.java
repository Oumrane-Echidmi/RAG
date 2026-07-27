package com.example.RAG;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * AI configuration that wires up the embedding model.
 *
 * <p>Provides a {@code @Primary} {@link EmbeddingModel} backed by a local Ollama
 * instance running {@code mxbai-embed-large}. This ensures the {@code VectorStore}
 * and {@code QuestionAnswerAdvisor} use local embeddings instead of calling an
 * external API that may not support embeddings (e.g., DeepSeek).</p>
 *
 * <p>Chat remains on the remote provider configured via {@code spring.ai.openai.*}
 * properties (pointing at DeepSeek in this project).</p>
 */
@Configuration
public class AiConfig {

    /**
     * Creates the primary {@link EmbeddingModel} bean backed by Ollama.
     *
     * <p>The {@code @Primary} annotation ensures this bean is injected wherever
     * an {@code EmbeddingModel} is required by Spring AI auto-configuration
     * (vector store, advisors, etc.).</p>
     *
     * <p>Model: {@code mxbai-embed-large} — produces 1024-dimensional vectors
     * that match the MariaDB vector store schema.</p>
     *
     * @param ollamaApi the auto-configured Ollama API client (reads
     *                  {@code spring.ai.ollama.base-url} from properties)
     * @return a configured {@code OllamaEmbeddingModel}
     */
    @Bean
    @Primary
    EmbeddingModel primaryEmbeddingModel(OllamaApi ollamaApi) {
        return OllamaEmbeddingModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(OllamaEmbeddingOptions.builder()
                        .model("mxbai-embed-large")         // 1024-dim vectors
                        .build())
                .build();
    }
}
