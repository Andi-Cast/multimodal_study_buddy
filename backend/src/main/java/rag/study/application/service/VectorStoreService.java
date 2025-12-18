package rag.study.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorStoreService {

    private final VectorStore vectorStore;

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 50;

    public void storeDocumentEmbeddings(Long documentId, String filename, String content) {
        log.info("Starting to create embeddings for document ID: {}", documentId);

        // Split content into chunks
        List<String> chunks = splitIntoChunks(content, CHUNK_SIZE, CHUNK_OVERLAP);
        log.info("Split document into {} chunks", chunks.size());

        // Create Document objects for each chunk with metadata
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);

            // Create metadata for this chunk
            Map<String, Object> metadata = Map.of(
                "document_id", documentId,
                "filename", filename,
                "chunk_index", i,
                "total_chunks", chunks.size()
            );

            // Create Spring AI Document (not our entity)
            Document doc = new Document(chunk, metadata);
            documents.add(doc);
        }

        // Store all documents in vector store
        // Spring AI will automatically create embeddings using OpenAI
        vectorStore.add(documents);
        log.info("Successfully stored {} embeddings for document ID: {}", documents.size(), documentId);
    }

    private List<String> splitIntoChunks(String content, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();

        if (content == null || content.isEmpty()) {
            return chunks;
        }

        int start = 0;
        int previousStart = -1;

        while (start < content.length()) {
            // Prevent infinite loops - ensure we're making progress
            if (start == previousStart) {
                log.warn("Chunking stuck at position {}. Breaking to prevent infinite loop.", start);
                break;
            }
            previousStart = start;

            int end = Math.min(start + chunkSize, content.length());

            // Try to break at word boundary if not at the end
            if (end < content.length()) {
                int lastSpace = content.lastIndexOf(' ', end);
                if (lastSpace > start) {
                    end = lastSpace;
                }
            }

            String chunk = content.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            // Move start forward, accounting for overlap
            // Ensure we always move forward by at least 1 character
            int newStart = end - overlap;
            start = Math.max(newStart, start + 1);
        }

        return chunks;
    }
}
