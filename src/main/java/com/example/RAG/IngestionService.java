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

@Component
public class IngestionService implements CommandLineRunner {

    private final VectorStore vectorStore;

    @Value("classpath:/docs/document.pdf")
    private Resource pdfResource;

    public IngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== DEBUT INGESTION ===");
        System.out.println("PDF path : " + pdfResource.getFilename());
        System.out.println("PDF exists : " + pdfResource.exists());

        try {
            // EXTRACT
            TikaDocumentReader documentReader = new TikaDocumentReader(pdfResource);
            List<Document> documents = documentReader.get();
            System.out.println("✅ Texte extrait : " + documents.size() + " document(s).");

            // TRANSFORM - Builder API (Spring AI 2.0.0-M2)
            TokenTextSplitter textSplitter = TokenTextSplitter.builder()
                    .withChunkSize(100) // taille réduite pour mxbai-embed-large
                    .withMinChunkSizeChars(30)
                    .withMinChunkLengthToEmbed(5)
                    .withMaxNumChunks(2000)
                    .withKeepSeparator(true)
                    .build();

            List<Document> splitDocuments = textSplitter.apply(documents);
            System.out.println("✅ Découpé en " + splitDocuments.size() + " segments.");

            // LOAD
            vectorStore.add(splitDocuments);
            System.out.println("✅ Segments chargés dans le Vector Store.");
            System.out.println("=== INGESTION TERMINÉE ===");

        } catch (Exception e) {
            System.err.println("❌ ERREUR : " + e.getMessage());
            e.printStackTrace();
        }
    }
}