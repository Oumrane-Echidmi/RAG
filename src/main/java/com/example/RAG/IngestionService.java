package com.example.RAG;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Startup service that ingests the CV PDF into the vector store.
 *
 * <p>Implements {@link CommandLineRunner} so it runs automatically at application
 * startup, after the Spring context is fully initialized. The pipeline has three stages:</p>
 *
 * <ol>
 *   <li><strong>Extract</strong> — parse the PDF with Apache Tika via
 *       {@link TikaDocumentReader}.</li>
 *   <li><strong>Transform</strong> — split the extracted text into ~100-token
 *       chunks using {@link TokenTextSplitter} so embeddings fit the model's
 *       context window.</li>
 *   <li><strong>Load</strong> — store the chunk embeddings into the MariaDB
 *       {@link VectorStore}.</li>
 * </ol>
 *
 * <p><strong>Note:</strong> Ingestion runs on every startup. If the vector store
 * already contains the same chunks, duplicates may occur unless the schema is
 * re-initialized via {@code spring.ai.vectorstore.mariadb.initialize-schema=true}.</p>
 *
 * @see AiConfig  configures the embedding model used during ingestion
 */
@Component
public class IngestionService implements CommandLineRunner {

    private final VectorStore vectorStore;

    /** Injected PDF resource from the classpath ({@code src/main/resources/docs/document.pdf}). */
    @Value("classpath:/docs/document.pdf")
    private Resource pdfResource;

    /**
     * Constructs the ingestion service.
     *
     * @param vectorStore the MariaDB-backed vector store (auto-configured by Spring AI)
     */
    public IngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Runs the full ingestion pipeline at application startup.
     *
     * <p>Extracts text from the PDF, splits it into chunks, generates embeddings
     * via Ollama, and stores them in MariaDB. Detailed progress messages are
     * printed to the console.</p>
     *
     * @param args command-line arguments (unused)
     * @throws Exception if any stage of the ingestion pipeline fails
     */
    @Override
    public void run(String... args) throws Exception {
        logSeparator("DEBUT INGESTION");
        System.out.println("PDF path : " + pdfResource.getFilename());
        System.out.println("PDF exists : " + pdfResource.exists());

        try {
            // ── Stage 1: Extract text from PDF ──────────────────────────
            TikaDocumentReader documentReader = new TikaDocumentReader(pdfResource);
            List<Document> documents = documentReader.get();
            System.out.println("✅ Texte extrait : " + documents.size() + " document(s).");

            // ── Stage 2: Split into chunks ──────────────────────────────
            // Using TokenTextSplitter builder API (Spring AI 2.0.0-M2)
            // Chunk size of 100 tokens is suitable for mxbai-embed-large (1024 dims)
            TokenTextSplitter textSplitter = TokenTextSplitter.builder()
                    .withChunkSize(100)             // tokens per chunk
                    .withMinChunkSizeChars(30)      // discard chunks shorter than 30 chars
                    .withMinChunkLengthToEmbed(5)   // minimum tokens to embed
                    .withMaxNumChunks(2000)         // safety cap on total chunks
                    .withKeepSeparator(true)        // preserve whitespace/separators
                    .build();

            List<Document> splitDocuments = textSplitter.apply(documents);
            System.out.println("✅ Découpé en " + splitDocuments.size() + " segments.");

            // ── Stage 3: Generate embeddings & store ────────────────────
            vectorStore.add(splitDocuments);
            System.out.println("✅ Segments chargés dans le Vector Store.");
            logSeparator("INGESTION TERMINÉE");

        } catch (Exception e) {
            System.err.println("❌ ERREUR : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Prints a visually distinct separator line with a label for log readability.
     *
     * @param label the text to display between the separator bars
     */
    private void logSeparator(String label) {
        System.out.println("=== " + label + " ===");
    }
}
