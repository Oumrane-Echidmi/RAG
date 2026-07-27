# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 4.0.3 RAG (Retrieval-Augmented Generation) application using Spring AI 2.0.0-M2. Ingests a PDF document, stores vector embeddings in MariaDB, and exposes a REST endpoint for conversational Q&A augmented with retrieved document chunks.

**Java version:** 25

## Build & Run Commands

```bash
# Build (uses Maven wrapper — no local Maven install needed)
./mvnw clean package          # (Git Bash / WSL)
.\mvnw.cmd clean package      # (PowerShell / cmd)

# Run tests
./mvnw test

# Run the application
./mvnw spring-boot:run

# Run a specific test class
./mvnw test -Dtest=RagApplicationTests
```

Maven wrapper (`mvnw` / `mvnw.cmd`) is checked in — use it instead of a system-installed `mvn`.

## Architecture

### RAG Pipeline

1. **Ingestion** (`IngestionService.java`) — runs at startup via `CommandLineRunner`. Reads `src/main/resources/docs/document.pdf` using Apache Tika (`TikaDocumentReader`), splits text into ~100-token chunks with `TokenTextSplitter`, and loads them into the MariaDB `VectorStore`.

2. **Chat** (`ChatController.java`) — `POST /api/chat` endpoint accepting JSON `{"message": "..."}`. Uses Spring AI's `ChatClient` with a `QuestionAnswerAdvisor` wired to the `VectorStore`. The advisor automatically retrieves relevant chunks from MariaDB and augments the prompt sent to the chat model (DeepSeek via OpenAI-compatible API).

### Key Components

| File                       | Role                                                              |
| -------------------------- | ----------------------------------------------------------------- |
| `RagdemoApplication.java`  | Spring Boot entry point (`@SpringBootApplication`)                |
| `IngestionService.java`    | Startup hook: PDF → Tika extraction → chunking → VectorStore load |
| `ChatController.java`      | REST controller exposing the RAG chat endpoint                    |
| `RagApplicationTests.java` | Context-load sanity test                                          |

### External Dependencies

- **MariaDB** — vector store. Must be running on `localhost:3307`, database `vdtestingdb`, user `root`/`root`. Schema auto-initialized (`spring.ai.vectorstore.mariadb.initialize-schema=true`). Vector dimensions: 1024 (matches `mxbai-embed-large`).
- **DeepSeek** — chat model (`deepseek-chat`) via OpenAI-compatible API endpoint. API key configured in `application.properties`.
- **Ollama** — local embedding model (`mxbai-embed-large`, 1024 dims). Chat auto-configuration is excluded to avoid bean conflicts.

## Configuration

All configuration is in `src/main/resources/application.properties`. The DeepSeek API key, Ollama base URL, and MariaDB credentials are stored there directly (not via environment variables).

- **Chat**: DeepSeek via `spring.ai.openai.*` properties (OpenAI-compatible base URL override).
- **Embeddings**: Ollama running `mxbai-embed-large` locally on port 11434.
- **Vector store**: MariaDB on port 3307, schema auto-initialized with 1024-dim vectors.

Spring AI 2.0.0-M2 APIs use a fluent builder style (e.g., `TokenTextSplitter.builder().withChunkSize(100)...build()`).

## Important Notes

- Spring AI 2.0.0-M2 is a milestone release — APIs may differ from GA documentation. `TokenTextSplitter` uses the builder pattern (not constructor), and `ChatClient.Builder` / `QuestionAnswerAdvisor` APIs reflect the M2 snapshot.
- The PDF document lives at `src/main/resources/docs/document.pdf` and is ingested on every startup. If the vector store already contains the same chunks, duplicates may occur unless the schema is re-initialized.
- There is a `.github/modernize/java-upgrade/` directory with hooks for an automated Java upgrade tool — this is infrastructure, not application code.
