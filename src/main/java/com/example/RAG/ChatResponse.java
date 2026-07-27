package com.example.RAG;

/**
 * Outgoing chat response returned to the frontend.
 *
 * <p>Wraps the AI-generated answer in a simple record. Serialized automatically
 * by Spring to JSON for the {@code POST /api/chat} response body.</p>
 *
 * @param answer the AI-generated answer text grounded in retrieved CV content
 */
public record ChatResponse(String answer) {}
