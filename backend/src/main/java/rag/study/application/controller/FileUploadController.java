package rag.study.application.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rag.study.application.dto.DocumentUploadResponse;
import rag.study.application.model.Document;
import rag.study.application.service.DocumentProcessingService;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final DocumentProcessingService documentProcessingService;

    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(@RequestParam("file") MultipartFile file) {
        log.info("Received upload request for file: {}", file.getOriginalFilename());

        try {
            // Process the document
            Document savedDocument = documentProcessingService.processDocument(file);

            // Build response DTO
            DocumentUploadResponse response = new DocumentUploadResponse(
                savedDocument.getId(),
                savedDocument.getFilename(),
                savedDocument.getFileType(),
                savedDocument.getFileSize(),
                "success",
                "File uploaded and processed successfully"
            );

            log.info("Successfully processed document: {} with ID: {}", savedDocument.getFilename(), savedDocument.getId());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Validation errors (bad file type, size, etc.)
            log.warn("Validation failed for file upload: {}", e.getMessage());
            DocumentUploadResponse errorResponse = new DocumentUploadResponse(
                null,
                file.getOriginalFilename(),
                null,
                file.getSize(),
                "error",
                e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            // Unexpected errors
            log.error("Failed to process document upload", e);
            DocumentUploadResponse errorResponse = new DocumentUploadResponse(
                null,
                file.getOriginalFilename(),
                null,
                file.getSize(),
                "error",
                "Failed to process document: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        log.info("Fetching all documents");
        try {
            List<Document> documents = documentProcessingService.getAllDocuments();
            log.info("Retrieved {} documents", documents.size());
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("Failed to retrieve documents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDocument(@PathVariable Long id) {
        log.info("Received delete request for document ID: {}", id);
        try {
            documentProcessingService.deleteDocument(id);
            log.info("Successfully deleted document ID: {}", id);
            return ResponseEntity.ok("Document deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete document ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to delete document: " + e.getMessage());
        }
    }
}
