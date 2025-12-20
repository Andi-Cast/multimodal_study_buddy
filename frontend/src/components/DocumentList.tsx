import { useEffect, useState } from 'react';
import { documentApi } from '../api';
import type {StudyDocument} from '../types';
import ConfirmModal from './ConfirmModal';

interface DocumentListProps {
  refreshTrigger: number;
}

export default function DocumentList({ refreshTrigger }: DocumentListProps) {
  const [documents, setDocuments] = useState<StudyDocument[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [documentToDelete, setDocumentToDelete] = useState<StudyDocument | null>(null);

  const fetchDocuments = async () => {
    setLoading(true);
    setError(null);

    try {
      const data = await documentApi.getAll();
      setDocuments(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load documents');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDocuments();
  }, [refreshTrigger]);

  const openDeleteModal = (doc: StudyDocument) => {
    setDocumentToDelete(doc);
    setShowDeleteModal(true);
  };

  const closeDeleteModal = () => {
    setShowDeleteModal(false);
    setDocumentToDelete(null);
  };

  const handleDelete = async () => {
    if (!documentToDelete) return;

    setDeletingId(documentToDelete.id);
    setShowDeleteModal(false);

    try {
      await documentApi.delete(documentToDelete.id);
      setDocuments(documents.filter(doc => doc.id !== documentToDelete.id));
    } catch (err: any) {
      alert(err.response?.data || 'Failed to delete document');
    } finally {
      setDeletingId(null);
      setDocumentToDelete(null);
    }
  };

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
    return (bytes / 1024 / 1024).toFixed(2) + ' MB';
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  if (loading) {
    return (
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-2xl font-bold mb-4 text-gray-800">Uploaded Documents</h2>
        <p className="text-gray-500">Loading documents...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-2xl font-bold mb-4 text-gray-800">Uploaded Documents</h2>
        <div className="bg-red-50 text-red-800 p-4 rounded-md border border-red-200">
          {error}
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-2xl font-bold mb-4 text-gray-800">
        Uploaded Documents ({documents.length})
      </h2>

      {documents.length === 0 ? (
        <p className="text-gray-500 text-center py-8">
          No documents uploaded yet. Upload your first document to get started!
        </p>
      ) : (
        <div className="space-y-3">
          {documents.map((doc) => (
            <div
              key={doc.id}
              className="border border-gray-200 rounded-lg p-4 hover:bg-gray-50 transition-colors"
            >
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <h3 className="font-semibold text-gray-800 mb-1">
                    {doc.filename}
                  </h3>
                  <div className="text-sm text-gray-600 space-y-1">
                    <p>
                      <span className="font-medium">Type:</span>{' '}
                      <span className="uppercase bg-gray-100 px-2 py-0.5 rounded">
                        {doc.fileType}
                      </span>
                    </p>
                    <p>
                      <span className="font-medium">Size:</span> {formatFileSize(doc.fileSize)}
                    </p>
                    <p>
                      <span className="font-medium">Uploaded:</span> {formatDate(doc.uploadDate)}
                    </p>
                  </div>
                </div>

                <button
                  onClick={() => openDeleteModal(doc)}
                  disabled={deletingId === doc.id}
                  className={`ml-4 px-3 py-1 rounded-md text-sm font-medium transition-colors
                    ${deletingId === doc.id
                      ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                      : 'bg-red-100 text-red-700 hover:bg-red-200'
                    }`}
                >
                  {deletingId === doc.id ? 'Deleting...' : 'Delete'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      <ConfirmModal
        isOpen={showDeleteModal}
        title="Delete Document"
        message={`Are you sure you want to delete "${documentToDelete?.filename}"? This action cannot be undone.`}
        onConfirm={handleDelete}
        onCancel={closeDeleteModal}
        confirmText="Delete"
        cancelText="Cancel"
      />
    </div>
  );
}
