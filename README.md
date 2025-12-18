# Study Buddy - AI-Powered Document Q&A System

A full-stack RAG (Retrieval-Augmented Generation) application that allows users to upload study documents and ask questions about them using advanced AI technology. Built with Spring Boot, PostgreSQL with PGVector, and OpenAI's GPT models.

## Overview

Study Buddy helps students and professionals get instant answers from their study materials. Simply upload your PDFs, PowerPoint presentations, or images, and ask questions in natural language. The AI will search through your documents and provide accurate, contextual answers with source citations.

## Key Features

- **Multi-Format Document Support**: Upload PDF, PPTX, and image files
- **Intelligent Text Extraction**: Uses Apache Tika for robust text extraction from various formats
- **Vector Similarity Search**: Leverages PostgreSQL's PGVector extension for fast, accurate document retrieval
- **AI-Powered Answers**: Integrates OpenAI's GPT-4o-mini for natural language understanding and response generation
- **Source Citation**: Every answer includes citations showing which documents and sections were used
- **Document Management**: Upload, list, and delete documents through a REST API
- **Optimized Chunking**: Smart text chunking with overlap for better context preservation
- **Cost-Efficient**: Uses optimized models (GPT-4o-mini, text-embedding-3-small) for minimal API costs

## Technology Stack

### Backend
- **Java 21** - Modern Java LTS version
- **Spring Boot 3.5.8** - Enterprise-grade framework
- **Spring AI 1.1.0** - AI integration framework
- **PostgreSQL 16** - Primary database
- **PGVector** - Vector similarity search extension
- **Apache Tika** - Multi-format document text extraction
- **Flyway** - Database migration management
- **Lombok** - Reduces boilerplate code
- **Maven** - Dependency management

### AI & ML
- **OpenAI GPT-4o-mini** - Language model for answering questions
- **text-embedding-3-small** - Embeddings model for vector representations
- **HNSW Index** - Hierarchical Navigable Small World algorithm for fast vector search
- **Cosine Similarity** - Distance metric for finding relevant documents

### Infrastructure
- **Docker & Docker Compose** - Containerized PostgreSQL with PGVector
- **Git** - Version control

## Architecture

This application uses a **RAG (Retrieval-Augmented Generation)** architecture:

```
┌─────────────────────────────────────────────────────────────────┐
│                         User Query                              │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│  1. Query Embedding Generation (OpenAI text-embedding-3-small)  │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│  2. Vector Similarity Search (PGVector with HNSW Index)         │
│     - Searches for top 5 most similar document chunks           │
│     - Uses cosine similarity with 0.3 threshold                 │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│  3. Context Building                                            │
│     - Retrieves relevant document chunks                        │
│     - Formats with source metadata                              │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│  4. Prompt Creation                                             │
│     - Combines context + user question                          │
│     - Adds instructions for citation                            │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│  5. LLM Response Generation (OpenAI GPT-4o-mini)                │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Answer + Sources                             │
└─────────────────────────────────────────────────────────────────┘
```

### Document Processing Flow

```
Upload File → Text Extraction (Tika) → Chunking (500 chars, 50 overlap)
    → Generate Embeddings (OpenAI) → Store in PGVector → Success Response
```

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 21** or higher ([Download](https://adoptium.net/))
- **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- **Docker Desktop** ([Download](https://www.docker.com/products/docker-desktop))
- **OpenAI API Key** ([Get one here](https://platform.openai.com/api-keys))
- **Git** ([Download](https://git-scm.com/downloads))

## Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/multimodel_study_buddy.git
cd multimodel_study_buddy
```

### 2. Start PostgreSQL with PGVector

```bash
docker-compose up -d
```

This will start PostgreSQL 16 with PGVector extension on port 5432.

### 3. Configure Environment Variables

The application requires an OpenAI API key. You have two options:

#### Option A: Using IDE (Recommended for Development)

**IntelliJ IDEA:**
1. Go to `Run → Edit Configurations`
2. Select your Spring Boot application
3. Under `Environment Variables`, add:
   ```
   OPENAI_API_KEY=your-actual-api-key-here
   ```

**VS Code:**
1. Create/edit `.vscode/launch.json`
2. Add to the configuration:
   ```json
   "env": {
     "OPENAI_API_KEY": "your-actual-api-key-here"
   }
   ```

#### Option B: Using .env File (Alternative)

```bash
cd backend
cp .env.example .env
```

Edit `.env` and add your OpenAI API key:
```
OPENAI_API_KEY=your-actual-api-key-here
```

**Note:** The `.env` file is in `.gitignore` and will never be committed to version control.

### 4. Build the Backend

```bash
cd backend
./mvnw clean install
```

### 5. Run Database Migrations

Flyway will automatically run migrations on application startup. The migrations will:
- Create the `documents` table
- Create the `vector_store` table
- Enable the PGVector extension
- Create HNSW indexes for fast similarity search

### 6. Start the Application

```bash
./mvnw spring-boot:run
```

The backend will start on `http://localhost:8080`

### 7. Verify Installation

Check if the application is running:
```bash
curl http://localhost:8080/api/documents
```

You should receive an empty array `[]` if everything is working correctly.

## API Documentation

### Base URL
```
http://localhost:8080/api
```

### Endpoints

#### 1. Upload Document

Upload a PDF, PPTX, or image file for processing.

**Endpoint:** `POST /api/documents/upload`

**Request:**
```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@/path/to/your/document.pdf"
```

**Response:**
```json
{
  "id": 1,
  "filename": "biology_notes.pdf",
  "fileType": "pdf",
  "fileSize": 1048576,
  "status": "success",
  "message": "File uploaded and processed successfully"
}
```

**Supported File Types:**
- PDF (`.pdf`)
- PowerPoint (`.ppt`, `.pptx`)
- Images (`.jpg`, `.jpeg`, `.png`)

**Limitations:**
- Maximum file size: 50MB

---

#### 2. Get All Documents

Retrieve a list of all uploaded documents.

**Endpoint:** `GET /api/documents`

**Request:**
```bash
curl http://localhost:8080/api/documents
```

**Response:**
```json
[
  {
    "id": 1,
    "filename": "biology_notes.pdf",
    "fileType": "pdf",
    "fileSize": 1048576,
    "uploadDate": "2025-12-18T10:30:00",
    "createdAt": "2025-12-18T10:30:00",
    "updatedAt": "2025-12-18T10:30:00"
  }
]
```

---

#### 3. Delete Document

Delete a document and its associated embeddings.

**Endpoint:** `DELETE /api/documents/{id}`

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/documents/1
```

**Response:**
```
Document deleted successfully
```

---

#### 4. Ask a Question

Query your uploaded documents with a natural language question.

**Endpoint:** `POST /api/chat/query`

**Request:**
```bash
curl -X POST http://localhost:8080/api/chat/query \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is ATP and what is its function?"
  }'
```

**Response:**
```json
{
  "answer": "ATP (Adenosine Triphosphate) is the primary energy currency of cells. It functions as a molecular unit of currency for energy transfer within cells. When ATP is broken down into ADP (Adenosine Diphosphate) and inorganic phosphate, it releases energy that powers various cellular processes including muscle contraction, nerve impulse propagation, and chemical synthesis.",
  "sources": [
    "biology_notes.pdf",
    "biochemistry_chapter3.pdf"
  ]
}
```

**How it Works:**
1. Your question is converted into a vector embedding
2. The system searches for the 5 most relevant document chunks (similarity > 30%)
3. Retrieved chunks are sent to GPT-4o-mini along with your question
4. The AI generates an answer based on the context
5. Sources are extracted from the metadata of retrieved chunks

---

## Project Structure

```
multimodel_study_buddy/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/rag/study/application/
│   │   │   │   ├── controller/
│   │   │   │   │   ├── ChatController.java          # Chat API endpoints
│   │   │   │   │   └── FileUploadController.java    # Document upload endpoints
│   │   │   │   ├── service/
│   │   │   │   │   ├── RAGQueryService.java         # RAG query logic
│   │   │   │   │   ├── VectorStoreService.java      # Embedding storage
│   │   │   │   │   └── DocumentProcessingService.java # File processing
│   │   │   │   ├── model/
│   │   │   │   │   └── Document.java                # Document entity
│   │   │   │   ├── repository/
│   │   │   │   │   └── DocumentRepository.java      # JPA repository
│   │   │   │   ├── dto/
│   │   │   │   │   ├── ChatRequest.java
│   │   │   │   │   ├── ChatResponse.java
│   │   │   │   │   └── DocumentUploadResponse.java
│   │   │   │   └── BackendApplication.java          # Spring Boot main class
│   │   │   └── resources/
│   │   │       ├── application.properties           # App configuration
│   │   │       └── db/migration/
│   │   │           └── V1__init_database.sql        # Database schema
│   │   └── test/                                    # Unit and integration tests
│   ├── .env.example                                 # Environment variables template
│   ├── .gitignore
│   └── pom.xml                                      # Maven dependencies
├── docker-compose.yml                               # PostgreSQL setup
├── .gitignore
└── README.md
```

## Configuration

Key configuration options in `application.properties`:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/studybuddy
spring.datasource.username=postgres
spring.datasource.password=postgres

# File Upload
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# OpenAI
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4o-mini
spring.ai.openai.chat.options.temperature=0.7
spring.ai.openai.embedding.options.model=text-embedding-3-small

# Vector Store
spring.ai.vectorstore.pgvector.index-type=HNSW
spring.ai.vectorstore.pgvector.distance-type=COSINE_DISTANCE
spring.ai.vectorstore.pgvector.dimensions=1536
```

## Usage Examples

### Example 1: Study Biology

```bash
# Upload your biology textbook
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@biology_textbook.pdf"

# Ask questions
curl -X POST http://localhost:8080/api/chat/query \
  -H "Content-Type: application/json" \
  -d '{"question": "Explain the process of photosynthesis"}'
```

### Example 2: Prepare for Exam

```bash
# Upload multiple study materials
curl -X POST http://localhost:8080/api/documents/upload -F "file=@chapter1.pdf"
curl -X POST http://localhost:8080/api/documents/upload -F "file=@chapter2.pdf"
curl -X POST http://localhost:8080/api/documents/upload -F "file=@lecture_slides.pptx"

# Ask exam questions
curl -X POST http://localhost:8080/api/chat/query \
  -H "Content-Type: application/json" \
  -d '{"question": "What are the key differences between mitosis and meiosis?"}'
```

### Example 3: Quick Review

```bash
# Check what documents you have
curl http://localhost:8080/api/documents

# Ask for a summary
curl -X POST http://localhost:8080/api/chat/query \
  -H "Content-Type: application/json" \
  -d '{"question": "Give me a summary of the main concepts in these documents"}'
```

## How It Works - Technical Deep Dive

### 1. Document Upload & Processing

When a file is uploaded:
1. **Validation**: File type and size are validated
2. **Text Extraction**: Apache Tika extracts text from the document
3. **Database Storage**: Document metadata is saved to PostgreSQL
4. **Chunking**: Text is split into 500-character chunks with 50-character overlap
5. **Embedding Generation**: Each chunk is sent to OpenAI's embedding API
6. **Vector Storage**: Embeddings are stored in PGVector with metadata

**Chunking Example:**
```
Original Text: "This is a long document about biology... [2000 characters]"

Chunk 1 (chars 0-500):   "This is a long document about biology..."
Chunk 2 (chars 450-950): "...biology. The cell is the basic unit..."
Chunk 3 (chars 900-1400): "...basic unit of life. Mitochondria..."
```

The 50-character overlap ensures context isn't lost between chunks.

### 2. Query Processing

When a question is asked:
1. **Query Embedding**: Question is converted to a 1536-dimensional vector
2. **Similarity Search**: PGVector finds the top 5 most similar chunks using cosine similarity
   - Similarity threshold: 0.3 (30%)
   - Uses HNSW index for speed (millisecond search times)
3. **Context Assembly**: Retrieved chunks are formatted with source information
4. **Prompt Engineering**: Context + question + instructions are combined
5. **LLM Generation**: GPT-4o-mini generates an answer based on the context
6. **Response**: Answer and sources are returned to the client

### 3. Vector Similarity Search

The application uses **cosine similarity** to find relevant documents:

```
similarity = (A · B) / (||A|| × ||B||)
```

- **1.0**: Identical vectors (perfect match)
- **0.7-0.9**: Very similar content
- **0.3-0.7**: Somewhat related
- **< 0.3**: Not relevant (filtered out)

The HNSW (Hierarchical Navigable Small World) index enables approximate nearest neighbor search in logarithmic time, making searches extremely fast even with thousands of chunks.

## Cost Estimation

For personal use with moderate activity:

| Component | Model | Pricing | Est. Monthly Cost |
|-----------|-------|---------|-------------------|
| Embeddings | text-embedding-3-small | $0.02 / 1M tokens | $0.50 - $2.00 |
| Chat Completions | gpt-4o-mini | $0.15 / 1M input, $0.60 / 1M output | $2.00 - $10.00 |
| **Total** | | | **$2.50 - $12.00/month** |

**Cost-Saving Features:**
- Uses gpt-4o-mini instead of GPT-4 (60x cheaper)
- Uses text-embedding-3-small (smallest embedding model)
- Efficient chunking reduces redundant embeddings
- Results caching reduces duplicate API calls

## Testing

### Manual Testing with cURL

```bash
# Test file upload
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@test_document.pdf"

# Test query
curl -X POST http://localhost:8080/api/chat/query \
  -H "Content-Type: application/json" \
  -d '{"question": "Test question"}'

# Test list documents
curl http://localhost:8080/api/documents

# Test delete
curl -X DELETE http://localhost:8080/api/documents/1
```

### Health Checks

```bash
# Check database connection
docker exec study_buddy_postgres pg_isready -U postgres

# Check if PGVector is enabled
docker exec study_buddy_postgres psql -U postgres -d studybuddy -c "SELECT * FROM pg_extension WHERE extname = 'vector';"

# View application logs
./mvnw spring-boot:run
```

## Troubleshooting

### Common Issues

**1. "Connection refused" error**
- Ensure Docker is running: `docker ps`
- Start PostgreSQL: `docker-compose up -d`

**2. "OpenAI API key invalid"**
- Verify your API key at https://platform.openai.com/api-keys
- Ensure environment variable is set correctly
- Restart the application after setting the variable

**3. "No results found" for queries**
- Check if documents were successfully uploaded and processed
- Look for embedding creation logs in the console
- Verify embeddings exist: `docker exec study_buddy_postgres psql -U postgres -d studybuddy -c "SELECT COUNT(*) FROM vector_store;"`

**4. "Insufficient quota" error**
- Add credits to your OpenAI account at https://platform.openai.com/account/billing

**5. Flyway migration errors**
- Clean the database: `docker-compose down -v && docker-compose up -d`
- Restart the application

### Logs

Enable detailed logging by editing `application.properties`:
```properties
logging.level.rag.study.application=DEBUG
logging.level.org.springframework.ai=DEBUG
```

## Future Enhancements

Potential improvements for this project:

- [ ] **Frontend UI**: React-based web interface for easier interaction
- [ ] **Multi-user Support**: User authentication and document isolation
- [ ] **Conversation History**: Save and retrieve past Q&A sessions
- [ ] **Advanced Chunking**: Semantic chunking based on document structure
- [ ] **Multiple LLM Support**: Add support for Claude, Gemini, or local models
- [ ] **Document OCR**: Better support for scanned documents and images
- [ ] **Export Functionality**: Export Q&A sessions to PDF or Markdown
- [ ] **Batch Upload**: Upload multiple files at once
- [ ] **Smart Caching**: Cache frequently asked questions
- [ ] **Analytics Dashboard**: Track usage, popular questions, and sources
- [ ] **Mobile App**: iOS and Android applications
- [ ] **Integration Testing**: Comprehensive test suite
- [ ] **CI/CD Pipeline**: Automated testing and deployment

## Security Considerations

This project implements several security best practices:

- ✅ **Environment Variables**: API keys stored in environment variables, not hardcoded
- ✅ **.gitignore**: Sensitive files (.env, .idea, credentials) excluded from version control
- ✅ **File Validation**: Upload size limits and file type restrictions
- ✅ **Error Handling**: Detailed error messages for debugging, generic messages for clients
- ✅ **CORS Configuration**: Currently allows all origins (configure for production)

**Before Production Deployment:**
- [ ] Configure CORS for specific frontend domains
- [ ] Add rate limiting to prevent API abuse
- [ ] Implement user authentication (Spring Security + JWT)
- [ ] Use HTTPS/TLS for all communications
- [ ] Move database credentials to environment variables
- [ ] Set up proper logging and monitoring
- [ ] Implement input sanitization for user queries
- [ ] Add API request/response validation

## Contributing

Contributions are welcome! Here's how you can help:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Spring AI](https://docs.spring.io/spring-ai/reference/) - AI integration framework
- [OpenAI](https://openai.com/) - GPT and embedding models
- [PGVector](https://github.com/pgvector/pgvector) - Vector similarity search
- [Apache Tika](https://tika.apache.org/) - Document text extraction

## Contact & Support

- **Author**: Your Name
- **Email**: your.email@example.com
- **GitHub**: [@yourusername](https://github.com/yourusername)
- **LinkedIn**: [Your LinkedIn](https://linkedin.com/in/yourprofile)
- **Portfolio**: [yourportfolio.com](https://yourportfolio.com)

---

**Built with ❤️ for students and learners everywhere**

*Last Updated: December 2025*
