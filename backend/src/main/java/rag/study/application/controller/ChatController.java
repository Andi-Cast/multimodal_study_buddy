package rag.study.application.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rag.study.application.dto.ChatRequest;
import rag.study.application.dto.ChatResponse;
import rag.study.application.service.RAGQueryService;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final RAGQueryService ragQueryService;

    @PostMapping("/query")
    public ResponseEntity<ChatResponse> query(@RequestBody ChatRequest request) {
        log.info("Received chat query: {}", request.getQuestion());

        // Validate request
        if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            log.warn("Empty question received");
            ChatResponse errorResponse = new ChatResponse(
                "Please provide a valid question.",
                List.of()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            // Query documents using RAG
            String answer = ragQueryService.queryDocuments(request.getQuestion());

            // Get sources for the answer
            List<String> sources = ragQueryService.getSources(request.getQuestion());

            // Build response
            ChatResponse response = new ChatResponse(answer, sources);

            log.info("Successfully generated answer for query: {}", request.getQuestion());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to process chat query", e);
            ChatResponse errorResponse = new ChatResponse(
                "An error occurred while processing your question. Please try again.",
                List.of()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
