package com.example.RAG;

/**
 * Incoming chat request sent by the frontend.
 *
 * <p>Encapsulates the user's question as a single {@code message} field.
 * Deserialized automatically by Spring from the {@code POST /api/chat} JSON body.</p>
 *
 * @param message the user's question text (non-null, non-blank expected)
 */
public record ChatRequest(String message) {}
