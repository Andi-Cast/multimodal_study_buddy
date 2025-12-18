# RAG Study Buddy - Architecture Overview

## Application Flow

### User Upload Flow:
```
File Upload → Parse Document → Split into Chunks → Generate Embeddings → Store in Vector DB
```

### Query/Chat Flow:
```
User Question → Generate Question Embedding → Search Similar Chunks →
Send to OpenAI with Context → Return Answer
```

---

## Classes We'll Build (Organized by Layer)

### 1. MODEL LAYER (Database Entities)

#### Document.java
**Purpose**: Represents a file in our database
**Why we need it**: Store metadata about uploaded files

```
Document
├── id (unique identifier)
├── filename (original file name)
├── fileType (pdf, pptx, jpg, png)
├── fileSize (in bytes)
├── contentText (extracted text from the file)
├── uploadDate (when it was uploaded)
└── metadata (JSON - any extra info)
```

**Example**: When you upload "biology_notes.pdf", we create a Document record with all this info.

---

### 2. REPOSITORY LAYER (Database Access)

#### DocumentRepository.java
**Purpose**: Interface to query the documents table
**Why we need it**: Spring Data JPA uses this to automatically create database queries

```java
// Instead of writing SQL like:
// SELECT * FROM documents WHERE filename = 'biology_notes.pdf'

// We just write:
findByFilename("biology_notes.pdf")
```

Spring automatically converts method names into SQL queries!

---

### 3. SERVICE LAYER (Business Logic)

This is where the actual work happens. Services contain the logic for processing data.

#### DocumentProcessingService.java
**Purpose**: Handles file uploads and document parsing
**What it does**:
1. Receives uploaded file
2. Uses Apache Tika to extract text from PDF/PowerPoint/Images
3. Saves Document entity to database
4. Triggers vector embedding creation

**Why we need it**: Different file types need different parsing strategies. Tika handles all of this for us.

**Example**:
- PDF: Extracts text content
- PowerPoint: Extracts text from slides
- Images: Uses OCR (Optical Character Recognition) to extract text

---

#### VectorStoreService.java
**Purpose**: Manages embeddings and vector storage
**What it does**:
1. Takes extracted text and splits it into chunks (e.g., paragraphs)
2. Sends chunks to OpenAI to create embeddings (vectors)
3. Stores embeddings in PGvector database

**Why we need it**:
- Long documents need to be split into chunks (OpenAI has token limits)
- Each chunk gets converted to a vector (array of 1536 numbers)
- Vectors allow us to find "similar" content mathematically

**Example**:
```
"Photosynthesis is the process..." → [0.23, -0.45, 0.12, ... 1536 numbers]
```

---

#### RAGQueryService.java
**Purpose**: Answers questions using Retrieval-Augmented Generation
**What it does**:
1. Takes user's question
2. Converts question to embedding vector
3. Searches vector DB for most similar chunks (retrieval)
4. Sends question + relevant chunks to OpenAI (augmented generation)
5. Returns AI-generated answer based on your documents

**Why we need it**: This is the "RAG" part! Instead of ChatGPT answering from general knowledge, it answers using YOUR documents.

**Example Flow**:
```
Question: "What is photosynthesis?"
↓
Convert to vector: [0.21, -0.43, 0.15, ...]
↓
Search similar chunks in DB: Find 5 most similar paragraphs
↓
Send to OpenAI: "Based on these documents: [chunks], answer: What is photosynthesis?"
↓
Return: "According to your biology notes, photosynthesis is..."
```

---

### 4. CONTROLLER LAYER (REST API Endpoints)

Controllers are the entry points - they handle HTTP requests from the frontend.

#### FileUploadController.java
**Purpose**: REST API endpoint for uploading files
**Endpoint**: `POST /api/documents/upload`

**What it does**:
1. Receives file from frontend
2. Validates file (size, type)
3. Calls DocumentProcessingService
4. Returns success/error response

**Why we need it**: Frontend needs a way to send files to backend.

**Example Request**:
```
POST /api/documents/upload
Body: FormData with file
Response: { "id": 1, "filename": "notes.pdf", "status": "uploaded" }
```

---

#### ChatController.java
**Purpose**: REST API endpoint for asking questions
**Endpoint**: `POST /api/chat/query`

**What it does**:
1. Receives question from frontend
2. Calls RAGQueryService
3. Returns AI-generated answer

**Why we need it**: Frontend needs a way to send questions and get answers.

**Example Request**:
```
POST /api/chat/query
Body: { "question": "What is mitosis?" }
Response: { "answer": "Based on your documents, mitosis is...", "sources": [...] }
```

---

### 5. DTO LAYER (Data Transfer Objects)

DTOs are simple objects for sending data between frontend and backend.

#### DocumentUploadResponse.java
**Purpose**: Format the response after file upload

```json
{
  "id": 1,
  "filename": "biology.pdf",
  "status": "success",
  "message": "File uploaded and processed"
}
```

#### ChatRequest.java & ChatResponse.java
**Purpose**: Format question requests and answer responses

```json
Request: { "question": "What is DNA?" }
Response: {
  "answer": "DNA is...",
  "sources": ["biology.pdf page 5"],
  "confidence": 0.95
}
```

**Why we need DTOs**: Separate what's stored in database from what's sent to frontend. Clean separation of concerns.

---

### 6. CONFIGURATION LAYER

#### VectorStoreConfig.java
**Purpose**: Configure Spring AI's vector store connection
**What it does**: Tells Spring AI how to connect to our PGvector database

**Why we need it**: Spring AI needs configuration to know where to store embeddings.

---

## How They All Work Together

```
Frontend sends file
        ↓
FileUploadController receives it
        ↓
DocumentProcessingService:
  - Uses Tika to extract text
  - Creates Document entity
  - Saves via DocumentRepository
        ↓
VectorStoreService:
  - Splits text into chunks
  - Calls OpenAI API for embeddings
  - Stores in PGvector via Spring AI
        ↓
Return success to frontend

---

Frontend sends question
        ↓
ChatController receives it
        ↓
RAGQueryService:
  - Converts question to embedding
  - Searches vector DB for similar chunks
  - Sends question + chunks to OpenAI
  - Gets answer
        ↓
Return answer to frontend
```

---

## Summary Table

| Layer | Class | Purpose |
|-------|-------|---------|
| **Model** | Document | Database table representation |
| **Repository** | DocumentRepository | Database queries |
| **Service** | DocumentProcessingService | Parse files, extract text |
| **Service** | VectorStoreService | Create and store embeddings |
| **Service** | RAGQueryService | Answer questions using RAG |
| **Controller** | FileUploadController | Upload API endpoint |
| **Controller** | ChatController | Chat API endpoint |
| **DTO** | Various Response/Request objects | Data transfer format |
| **Config** | VectorStoreConfig | Configure vector database |

---

## Technology Stack

### Backend:
- **Spring Boot 3.5.8**: Java framework for building web applications
- **Spring AI 1.1.0**: AI integration framework
- **OpenAI API**: For embeddings and chat completions
- **Apache Tika**: Document parsing (PDF, PowerPoint, images)
- **PostgreSQL 16**: Relational database
- **PGvector**: Vector similarity search extension
- **Flyway**: Database migrations
- **Lombok**: Reduces boilerplate code

### Frontend:
- **React 19.2**: UI framework
- **TypeScript**: Type-safe JavaScript
- **Vite**: Fast build tool
- **TailwindCSS**: Utility-first CSS framework
- **Axios**: HTTP client
- **React Router**: Client-side routing

### Infrastructure:
- **Docker**: Containerization for PostgreSQL
- **Maven**: Java build tool

---

## Database Schema

### documents table:
```sql
- id: BIGSERIAL PRIMARY KEY
- filename: VARCHAR(255)
- file_type: VARCHAR(50)
- file_size: BIGINT
- upload_date: TIMESTAMP
- content_text: TEXT
- metadata: JSONB
- created_at: TIMESTAMP
- updated_at: TIMESTAMP
```

### vector_store table:
```sql
- id: UUID PRIMARY KEY
- content: TEXT (the text chunk)
- metadata: JSON (source info)
- embedding: VECTOR(1536) (OpenAI embedding)
```

---

## API Endpoints

### File Upload:
- **POST** `/api/documents/upload`
  - Request: multipart/form-data with file
  - Response: DocumentUploadResponse

### List Documents:
- **GET** `/api/documents`
  - Response: List of Document objects

### Delete Document:
- **DELETE** `/api/documents/{id}`
  - Response: Success/error message

### Chat/Query:
- **POST** `/api/chat/query`
  - Request: ChatRequest { question: string }
  - Response: ChatResponse { answer: string, sources: [] }

---

## Environment Variables Required

```properties
OPENAI_API_KEY=your-openai-api-key
```

---

## Key Concepts

### What is RAG (Retrieval-Augmented Generation)?
RAG combines two things:
1. **Retrieval**: Finding relevant information from your documents
2. **Generation**: Using AI to generate answers based on that information

Instead of the AI making up answers, it uses YOUR documents as the source of truth.

### What are Vector Embeddings?
Embeddings are numerical representations of text that capture meaning:
- Similar text has similar vectors
- "cat" and "kitten" have closer vectors than "cat" and "car"
- We use cosine similarity to find the most relevant chunks

### Why Split Documents into Chunks?
- AI models have token limits (can't process entire books)
- Smaller chunks = more precise retrieval
- Better context for answering specific questions

---

## Build Order

1. ✅ Database setup (PostgreSQL + PGvector)
2. ✅ Configuration (application.properties)
3. ✅ Database migrations (Flyway)
4. ⏳ Model layer (Document entity)
5. ⏳ Repository layer (DocumentRepository)
6. ⏳ Service layer (DocumentProcessingService, VectorStoreService, RAGQueryService)
7. ⏳ Controller layer (FileUploadController, ChatController)
8. ⏳ DTO layer (Request/Response objects)
9. ⏳ Testing and integration
