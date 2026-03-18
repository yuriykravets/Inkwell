package com.partitionsoft.bookshelf.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReaderDocumentDao {

    @Query(
        """
        SELECT d.id, d.title, d.uri, d.format, d.mimeType, d.addedAtMillis, p.location AS lastLocation
        FROM reader_documents d
        LEFT JOIN reader_progress p ON p.documentId = d.id
        ORDER BY d.addedAtMillis DESC
        """
    )
    fun observeLibrary(): Flow<List<ReaderDocumentWithProgress>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDocument(document: ReaderDocumentEntity): Long

    @Query("SELECT * FROM reader_documents WHERE uri = :uri LIMIT 1")
    suspend fun getDocumentByUri(uri: String): ReaderDocumentEntity?

    @Query("SELECT * FROM reader_documents WHERE id = :documentId LIMIT 1")
    suspend fun getDocumentById(documentId: Long): ReaderDocumentEntity?

    @Query("SELECT location FROM reader_progress WHERE documentId = :documentId LIMIT 1")
    suspend fun getProgressLocation(documentId: Long): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: ReaderProgressEntity)
}

