package com.partitionsoft.bookshelf.di

import com.partitionsoft.bookshelf.data.repository.BookRepositoryImpl
import com.partitionsoft.bookshelf.data.repository.ReadingStatsRepositoryImpl
import com.partitionsoft.bookshelf.data.repository.ReaderRepositoryImpl
import com.partitionsoft.bookshelf.data.repository.RecommendationRepositoryImpl
import com.partitionsoft.bookshelf.domain.repository.BookRepository
import com.partitionsoft.bookshelf.domain.repository.ReadingStatsRepository
import com.partitionsoft.bookshelf.domain.repository.ReaderRepository
import com.partitionsoft.bookshelf.domain.repository.RecommendationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBookRepository(
        repository: BookRepositoryImpl
    ): BookRepository

    @Binds
    @Singleton
    abstract fun bindReaderRepository(
        repository: ReaderRepositoryImpl
    ): ReaderRepository

    @Binds
    @Singleton
    abstract fun bindRecommendationRepository(
        repository: RecommendationRepositoryImpl
    ): RecommendationRepository

    @Binds
    @Singleton
    abstract fun bindReadingStatsRepository(
        repository: ReadingStatsRepositoryImpl
    ): ReadingStatsRepository
}