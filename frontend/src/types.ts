export interface StudyDocument {
  id: number;
  filename: string;
  fileType: string;
  fileSize: number;
  uploadDate: string;
  createdAt: string;
  updatedAt: string;
}

export interface DocumentUploadResponse {
  id: number | null;
  filename: string;
  fileType: string | null;
  fileSize: number;
  status: string;
  message: string;
}

export interface ChatRequest {
  question: string;
}

export interface ChatResponse {
  answer: string;
  sources: string[];
}
