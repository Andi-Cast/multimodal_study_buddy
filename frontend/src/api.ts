import axios from 'axios';
import type {StudyDocument, DocumentUploadResponse, ChatRequest, ChatResponse} from './types';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const documentApi = {
  upload: async (file: File): Promise<DocumentUploadResponse> => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await api.post<DocumentUploadResponse>('/documents/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  getAll: async (): Promise<StudyDocument[]> => {
    const response = await api.get<StudyDocument[]>('/documents');
    return response.data;
  },

  delete: async (id: number): Promise<string> => {
    const response = await api.delete<string>(`/documents/${id}`);
    return response.data;
  },
};

export const chatApi = {
  query: async (question: string): Promise<ChatResponse> => {
    const request: ChatRequest = { question };
    const response = await api.post<ChatResponse>('/chat/query', request);
    return response.data;
  },
};
