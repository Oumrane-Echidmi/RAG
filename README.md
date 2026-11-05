# 🤖 RAG Portfolio Assistant

**Retrieval-Augmented Generation (RAG) chatbot powered by Spring AI** — ingest a CV/resume PDF and ask questions about it through a sleek chat interface. Built with Spring Boot 4.0.3 and Spring AI 2.0.0-M2, running entirely on local Ollama models.

[![Java](https://img.shields.io/badge/Java-25-red.svg)](https://jdk.java.net/25/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0.0--M2-blue.svg)](https://spring.io/projects/spring-ai)
[![Ollama](https://img.shields.io/badge/Ollama-local-black.svg)](https://ollama.com)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📸 Screenshot

<!-- TODO: Replace docs/screenshot.png with a screenshot of the chat UI. -->
<img width="1912" height="856" alt="image" src="https://github.com/user-attachments/assets/9e68c0e1-d37e-486e-a203-a68fc0db9a87" />


---

## ✨ Features

- **📄 PDF Ingestion** — Parses a CV/resume PDF at startup using Apache Tika, splits it into semantic chunks, and stores vector embeddings for retrieval.
- **💬 RAG Chat** — Ask questions about the document through a modern chat UI or REST API. Answers are grounded in the ingested content, not model hallucination.
- **🔍 Semantic Search** — MariaDB vector similarity search finds the most relevant document chunks for each question.
- **🎨 Modern UI** — Responsive chat interface with typing indicators, markdown rendering, quick-action chips, and dark theme.
- **🏠 100% Local AI** — Both chat (`llama3.2`) and embeddings (`mxbai-embed-large`) run locally through Ollama. No external API keys or paid services.

---

## 🏗 Architecture

```
┌──────────────┐     POST /api/chat      ┌────────────────┐
│   Frontend   │ ────────────────────────▶│ ChatController │
│  (HTML/CSS/  │◀─────────────────────────│                │
│    JS)       │     JSON {answer}        └───────┬────────┘
└──────────────┘                                  │
                                                  │ 1. User question
                                                  ▼
                                         ┌────────────────┐
                                         │ Vector Store   │
                                         │   (MariaDB)    │
                                         │ 2. Similarity  │
                                         │    Search      │
                                         └───────┬────────┘
                                                  │
                                                  │ 3. Relevant chunks
                                                  ▼
┌──────────────┐     Startup (CLR)       ┌────────────────┐
│  document.pdf│ ───────────────────────▶│   Chat Model   │
│  (CV/Resume) │                         │    (Ollama)    │
└──────────────┘                         └───────┬────────┘
                                                  │
                                                  │ 4. Augmented prompt → answer
                                                  ▼
                                          ┌──────────────┐
                                          │   Response   │
                                          │  (grounded   │
                                          │  in CV data) │
                                          └──────────────┘
```

### RAG Pipeline

| Stage | Component | Description |
|-------|-----------|-------------|
| **Extract** | Apache Tika (`TikaDocumentReader`) | Parses the PDF into raw text |
| **Transform** | `TokenTextSplitter` | Splits text into ~100-token overlapping chunks |
| **Embed** | Ollama (`mxbai-embed-large`) | Generates 1024-dimension vector embeddings |
| **Store** | MariaDB Vector Store | Persists embeddings with similarity search index |
| **Retrieve** | `QuestionAnswerAdvisor` | Finds top-K relevant chunks for each query |
| **Generate** | Ollama (`llama3.2`) | Produces a grounded answer from retrieved context |

---

## 🚀 Quick Start

### Prerequisites

| Dependency | Version | Purpose |
|-----------|---------|---------|
| [Java](https://jdk.java.net/25/) | **25+** | Runtime |
| [MariaDB](https://mariadb.org/download/) | 11.8+ | Vector store (native `VECTOR` type) |
| [Ollama](https://ollama.com) | Latest | Local chat + embedding models |

> Docker users can skip the manual Java/MariaDB/Ollama setup and use the Compose stack described below.

### 1. Clone the Project

```bash
git clone https://github.com/YOUR_USERNAME/rag-portfolio-assistant.git
cd rag-portfolio-assistant
```

No API key is needed — the default configuration points at a local Ollama instance.

### 2. Start MariaDB

```bash
docker run -d --name mariadb-rag \
  -p 3307:3306 \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=vdtestingdb \
  mariadb:11.8
```

### 3. Start Ollama & Pull Models

```bash
ollama serve                    # Start Ollama (if not already running)
ollama pull llama3.2            # Chat model
ollama pull mxbai-embed-large   # Embedding model (1024 dims)
```

### 4. Add Your CV PDF

Place your CV/resume PDF at:
```
src/main/resources/docs/document.pdf
```

### 5. Run the Application

```bash
# Using Maven wrapper (no Maven install needed)
./mvnw spring-boot:run          # Git Bash / WSL
.\mvnw.cmd spring-boot:run      # PowerShell / cmd
```

Open **http://localhost:8080** in your browser and start asking questions!

### 6. Run with Docker (alternative)

The `compose.yaml` stack runs the application together with MariaDB and Ollama,
and automatically pulls both models on first start.

```bash
docker compose up --build -d
```

Open **http://localhost:8080**. Useful commands:

```bash
docker compose logs -f app   # application logs
docker compose down          # stop the stack (keeps data volumes)
```

Ports exposed: app `8080`, MariaDB `3307`, Ollama `11434`.

To use a different local chat model:

```bash
# PowerShell: $env:OLLAMA_CHAT_MODEL="qwen2.5:7b"
export OLLAMA_CHAT_MODEL=qwen2.5:7b
docker compose up --build -d
```

---

## 📡 API Reference

### Chat Endpoint

```http
POST /api/chat
Content-Type: application/json

{
  "message": "What technologies does Oumrane know?"
}
```

**Response:**

```json
{
  "answer": "Oumrane is proficient in Java, Spring Boot, Python..."
}
```

| Status | Meaning |
|--------|---------|
| `200` | Successful response |
| `500` | Server error (check logs) |

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/example/RAG/
│   │   ├── RagdemoApplication.java    # Spring Boot entry point
│   │   ├── AiConfig.java              # Ollama embedding model bean
│   │   ├── ChatController.java        # POST /api/chat REST endpoint
│   │   ├── ChatRequest.java           # Incoming message DTO
│   │   ├── ChatResponse.java          # Outgoing answer DTO
│   │   ├── HomeController.java        # Serves index.html
│   │   ├── IngestionService.java      # PDF ingestion pipeline (startup)
│   │   └── WebConfig.java             # Static resource mappings
│   └── resources/
│       ├── application.properties     # App configuration
│       ├── docs/
│       │   └── document.pdf           # Your CV/resume (ingested at startup)
│       └── templates/
│           ├── index.html             # Chat UI
│           ├── me.png                 # Avatar image
│           ├── css/
│           │   └── style.css          # Stylesheet (dark theme)
│           └── js/
│               └── app.js             # Chat logic (IIFE, strict mode)
└── test/java/com/example/RAG/
    └── RagApplicationTests.java       # Context-load sanity test
```

---

## ⚙️ Configuration Reference

All configuration lives in `application.properties`. Here are the key properties:

### Ollama (Chat + Embeddings)

```properties
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=llama3.2
spring.ai.ollama.chat.options.temperature=0.7
spring.ai.ollama.embedding.options.model=mxbai-embed-large
```

> Both the chat model and the embedding model are served by Ollama. To use a different chat model, pull it first (`ollama pull <model>`) and update `spring.ai.ollama.chat.options.model`.

### MariaDB (Vector Store)

```properties
spring.datasource.url=jdbc:mariadb://localhost:3307/vdtestingdb
spring.datasource.username=root
spring.datasource.password=root
spring.ai.vectorstore.mariadb.initialize-schema=true
spring.ai.vectorstore.mariadb.dimensions=1024
```

---

## 🛠 How to Customize

### Change the Chat Model

Pull the model locally and update the property (or set the `OLLAMA_CHAT_MODEL` environment variable when using Docker):

```bash
ollama pull qwen2.5:7b
```

```properties
spring.ai.ollama.chat.options.model=qwen2.5:7b
```

### Change the Embedding Model

Any model with the same dimensions works. Update both:
```properties
spring.ai.ollama.embedding.options.model=your-model
spring.ai.vectorstore.mariadb.dimensions=<matching-dimension>
```

### Customize Chunking

Edit `IngestionService.java` to adjust chunk size, overlap, or max chunks.

### Use a Remote Provider

The project is wired for local Ollama by default. To use a remote provider, add the corresponding Spring AI starter (for example `spring-ai-starter-model-openai`) and configure its `spring.ai.*` properties. No code changes are required because `ChatController` uses Spring AI's `ChatClient.Builder`.

---

## 🧪 Running Tests

```bash
./mvnw test
```

> **Note:** The context-load test requires MariaDB and Ollama to be running. It validates that all Spring beans wire up correctly.

---

## 📝 License

MIT — see the [LICENSE](LICENSE) file for details.

---

## 🙋‍♂️ About

This project serves as an interactive portfolio/resume chatbot. Upload your CV as a PDF, and visitors can ask natural-language questions about your experience, skills, education, and projects — with answers grounded in the actual document content.

Built by [Oumrane Echidmi](https://github.com/oumrane-echidmi).
