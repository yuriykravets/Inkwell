package com.partitionsoft.bookshelf.di

import android.content.Context
import androidx.room.Room
import com.partitionsoft.bookshelf.data.local.BooksDatabase
import com.partitionsoft.bookshelf.data.local.FavoriteBookDao
import com.partitionsoft.bookshelf.data.local.ReaderDocumentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBooksDatabase(@ApplicationContext context: Context): BooksDatabase =
        Room.databaseBuilder(
            context,
            BooksDatabase::class.java,
            "bookshelf.db"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideFavoriteBookDao(database: BooksDatabase): FavoriteBookDao =
        database.favoriteBookDao()

    @Provides
    @Singleton
    fun provideReaderDocumentDao(database: BooksDatabase): ReaderDocumentDao =
        database.readerDocumentDao()
}

