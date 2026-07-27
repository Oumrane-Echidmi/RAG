package com.example.RAG;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiConfig {

    /**
     * Provides a {@code @Primary} {@link EmbeddingModel} backed by Ollama so the
     * {@code VectorStore} and {@code QuestionAnswerAdvisor} use the local
     * {@code mxbai-embed-large} model instead of calling DeepSeek (which has no
     * embeddings API).
     * <p>
     * Chat remains on DeepSeek via the existing {@code spring.ai.openai.*}
     * configuration pointing at {@code api.deepseek.com}.
     */
    @Bean
    @Primary
    EmbeddingModel primaryEmbeddingModel(OllamaApi ollamaApi) {
        return OllamaEmbeddingModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(OllamaEmbeddingOptions.builder()
                        .model("mxbai-embed-large")
                        .build())
                .build();
    }
}
