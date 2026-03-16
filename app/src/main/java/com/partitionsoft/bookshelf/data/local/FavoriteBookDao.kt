package com.partitionsoft.bookshelf.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteBookDao {

    @Query("SELECT * FROM favorite_books ORDER BY savedAtMillis DESC")
    fun observeFavorites(): Flow<List<FavoriteBookEntity>>

    @Query("SELECT id FROM favorite_books")
    fun observeFavoriteIds(): Flow<List<String>>

    @Query("SELECT id FROM favorite_books")
    suspend fun getFavoriteIdsOnce(): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_books WHERE id = :bookId)")
    suspend fun isFavorite(bookId: String): Boolean

    @Query("SELECT * FROM favorite_books WHERE id = :bookId LIMIT 1")
    suspend fun getFavoriteById(bookId: String): FavoriteBookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFavorite(book: FavoriteBookEntity)

    @Query("DELETE FROM favorite_books WHERE id = :bookId")
    suspend fun deleteFavoriteById(bookId: String)
}

