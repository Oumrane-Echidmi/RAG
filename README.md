# RAG Application

Spring Boot 4.0.3 RAG (Retrieval-Augmented Generation) application using Spring AI 2.0.0-M2. Ingests a PDF document, stores vector embeddings in MariaDB, and exposes a REST endpoint for conversational Q&A augmented with retrieved document chunks.

**Java version:** 25

## Prerequisites

- **Java 25**
- **MariaDB** running on `localhost:3307`, database `vdtestingdb`, user `root`/`root`
- **OpenAI API key** configured in `application.properties`

## Build & Run

```bash
# Build (uses Maven wrapper — no local Maven install needed)
./mvnw clean package          # Git Bash / WSL
.\mvnw.cmd clean package      # PowerShell / cmd

# Run tests
./mvnw test

# Run the application
./mvnw spring-boot:run

# Run a specific test class
./mvnw test -Dtest=RagApplicationTests
```

## Architecture

### RAG Pipeline

1. **Ingestion** (`IngestionService.java`) — runs at startup via `CommandLineRunner`. Reads `src/main/resources/docs/document.pdf` using Apache Tika (`TikaDocumentReader`), splits text into ~100-token chunks with `TokenTextSplitter`, and loads them into the MariaDB `VectorStore`.

2. **Chat** (`ChatController.java`) — single `GET /{message}` endpoint. Uses Spring AI's `ChatClient` with a `QuestionAnswerAdvisor` wired to the `VectorStore`. The advisor automatically retrieves relevant chunks from MariaDB and augments the prompt sent to OpenAI.

### Key Components

| File | Role |
| ---- | ---- |
| `RagdemoApplication.java` | Spring Boot entry point |
| `IngestionService.java` | PDF → Tika extraction → chunking → VectorStore load |
| `ChatController.java` | REST controller exposing the RAG chat endpoint |
| `RagApplicationTests.java` | Context-load sanity test |

### External Dependencies

- **MariaDB** — vector store, schema auto-initialized, vector dimensions: 1024 (matches `mxbai-embed-large`)
- **OpenAI** — chat model (`gpt-4o`) and embeddings via `mxbai-embed-large`

## Configuration

All configuration is in `src/main/resources/application.properties`. Spring AI 2.0.0-M2 APIs use a fluent builder style.
