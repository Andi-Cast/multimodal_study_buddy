package rag.study.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rag.study.application.model.Document;
import rag.study.application.repository.DocumentRepository;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentProcessingService {

    private final DocumentRepository documentRepository;
    private final VectorStoreService vectorStoreService;
    private final Tika tika = new Tika();

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_TYPES = {"pdf", "pptx", "ppt", "jpg", "jpeg", "png"};

    public Document processDocument(MultipartFile file) {
        log.info("Starting to process document: {}", file.getOriginalFilename());

        // Validate file
        validateFile(file);

        try {
            // Extract text using Apache Tika
            String extractedText = extractText(file);
            log.info("Successfully extracted {} characters from {}", extractedText.length(), file.getOriginalFilename());

            // Create Document entity
            Document document = new Document();
            document.setFilename(file.getOriginalFilename());
            document.setFileType(getFileExtension(file.getOriginalFilename()));
            document.setFileSize(file.getSize());
            document.setContentText(extractedText);

            // Save to database
            Document savedDocument = documentRepository.save(document);
            log.info("Saved document to database with ID: {}", savedDocument.getId());

            // Create and store embeddings asynchronously
            // In production, you might want to use @Async for this
            try {
                vectorStoreService.storeDocumentEmbeddings(
                    savedDocument.getId(),
                    savedDocument.getFilename(),
                    extractedText
                );
            } catch (Exception e) {
                log.error("Failed to create embeddings for document ID: {}", savedDocument.getId(), e);
                // We don't fail the whole operation if embeddings fail
                // The document is still saved, embeddings can be retried
            }

            return savedDocument;

        } catch (IOException e) {
            log.error("Failed to process document: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to process document: " + e.getMessage(), e);
        }
    }

    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot process empty file");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 10MB");
        }

        // Check file type
        String fileExtension = getFileExtension(file.getOriginalFilename());
        boolean isAllowed = false;
        for (String allowedType : ALLOWED_TYPES) {
            if (allowedType.equalsIgnoreCase(fileExtension)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new IllegalArgumentException("File type not supported. Allowed types: pdf, pptx, ppt, jpg, jpeg, png");
        }
    }

    private String extractText(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            // Apache Tika automatically detects file type and extracts text
            // For PDFs: extracts text content
            // For PowerPoint: extracts text from slides
            // For Images: uses OCR (Tesseract) to extract text
            String text = tika.parseToString(inputStream);

            // Clean up extracted text
            text = text.trim();

            if (text.isEmpty()) {
                log.warn("No text could be extracted from file: {}", file.getOriginalFilename());
                throw new RuntimeException("No text content found in the document");
            }

            return text;
        } catch (TikaException e) {
            log.error("Tika failed to parse document: {}", file.getOriginalFilename(), e);
            throw new IOException("Failed to extract text from document", e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    public java.util.List<Document> getAllDocuments() {
        return documentRepository.findAllByOrderByUploadDateDesc();
    }

    public void deleteDocument(Long documentId) {
        log.info("Deleting document with ID: {}", documentId);
        documentRepository.deleteById(documentId);
        // Note: Vector embeddings cleanup should be handled separately
        // Spring AI VectorStore doesn't have a built-in way to delete by metadata
        // This is a known limitation we'll address later
    }
}
