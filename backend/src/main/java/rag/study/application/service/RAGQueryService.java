package rag.study.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RAGQueryService {

    private final VectorStore vectorStore;
    private final ChatClient.Builder chatClientBuilder;

    private static final int TOP_K_RESULTS = 5;

    private static final String PROMPT_TEMPLATE = """
            You are a helpful study assistant. Answer the question based on the context provided from the user's study documents.

            Context from documents:
            {context}

            Question: {question}

            Instructions:
            - Answer the question based on the context provided above
            - If the context doesn't contain enough information, say so
            - Cite which document the information came from when possible
            - Be concise but thorough

            Answer:
            """;

    public String queryDocuments(String question) {
        log.info("Processing query: {}", question);

        // Step 1: Search for similar documents in vector store
        List<Document> similarDocuments = searchSimilarDocuments(question);

        if (similarDocuments.isEmpty()) {
            log.warn("No similar documents found for query: {}", question);
            return "I couldn't find any relevant information in your uploaded documents to answer this question. Please try uploading more documents or rephrasing your question.";
        }

        log.info("Found {} similar document chunks", similarDocuments.size());

        // Step 2: Build context from retrieved documents
        String context = buildContext(similarDocuments);

        // Step 3: Create prompt with context and question
        PromptTemplate promptTemplate = new PromptTemplate(PROMPT_TEMPLATE);
        Prompt prompt = promptTemplate.create(Map.of(
            "context", context,
            "question", question
        ));

        // Step 4: Send to OpenAI and get response
        ChatClient chatClient = chatClientBuilder.build();
        String answer = chatClient.prompt(prompt)
            .call()
            .content();

        log.info("Generated answer for query: {}", question);
        return answer;
    }

    private List<Document> searchSimilarDocuments(String query) {
        try {
            // Create search request with top K results
            SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(TOP_K_RESULTS)
                .similarityThreshold(0.3) // Accept results with >30% similarity (balanced approach)
                .build();

            // Search vector store
            List<Document> results = vectorStore.similaritySearch(searchRequest);
            log.info("Vector search returned {} documents for query: {}", results.size(), query);
            return results;
        } catch (Exception e) {
            // Handle case when vector store is empty or search fails
            log.error("Vector store search failed: {}", e.getMessage(), e);
            return List.of(); // Return empty list instead of throwing
        }
    }

    private String buildContext(List<Document> documents) {
        // Build a formatted context string from the retrieved documents
        StringBuilder contextBuilder = new StringBuilder();

        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            String content = doc.getText();
            Map<String, Object> metadata = doc.getMetadata();

            // Extract metadata
            String filename = metadata.getOrDefault("filename", "Unknown").toString();
            Integer chunkIndex = metadata.containsKey("chunk_index")
                ? (Integer) metadata.get("chunk_index")
                : i;

            // Format: [Source: filename, Chunk: 1]
            contextBuilder.append(String.format("[Source: %s, Chunk: %d]\n", filename, chunkIndex));
            contextBuilder.append(content);
            contextBuilder.append("\n\n");
        }

        return contextBuilder.toString().trim();
    }

    public List<String> getSources(String question) {
        // Helper method to get just the sources without generating an answer
        List<Document> similarDocuments = searchSimilarDocuments(question);

        return similarDocuments.stream()
            .map(doc -> {
                Map<String, Object> metadata = doc.getMetadata();
                return metadata.getOrDefault("filename", "Unknown").toString();
            })
            .distinct()
            .collect(Collectors.toList());
    }
}
