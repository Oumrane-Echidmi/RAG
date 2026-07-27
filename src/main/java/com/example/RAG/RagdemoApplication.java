package com.example.RAG;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the RAG (Retrieval-Augmented Generation) portfolio application.
 *
 * <p>Uses Spring Boot 4.0.3 with Spring AI 2.0.0-M2 to ingest a CV/resume PDF,
 * store vector embeddings in MariaDB, and expose a chat endpoint that answers
 * questions augmented with retrieved document chunks.</p>
 *
 * <p>The {@code @SpringBootApplication} annotation enables component scanning,
 * auto-configuration, and property configuration across the entire application.</p>
 *
 * @see IngestionService  PDF ingestion pipeline (runs at startup)
 * @see ChatController    REST endpoint for conversational Q&A
 */
@SpringBootApplication
public class RagdemoApplication {

    /**
     * Bootstraps the Spring Boot application.
     *
     * @param args command-line arguments passed to the JVM
     */
    public static void main(String[] args) {
        SpringApplication.run(RagdemoApplication.class, args);
    }

}
