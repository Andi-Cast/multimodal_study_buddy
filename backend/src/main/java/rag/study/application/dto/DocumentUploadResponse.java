package rag.study.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadResponse {

    private Long id;
    private String filename;
    private String fileType;
    private Long fileSize;
    private String status;
    private String message;
}
