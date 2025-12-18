package rag.study.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rag.study.application.model.Document;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByFilename(String filename);

    List<Document> findByFileType(String fileType);

    List<Document> findAllByOrderByUploadDateDesc();
}
