package com.partitionsoft.bookshelf.di

import com.partitionsoft.bookshelf.data.repository.BookRepositoryImpl
import com.partitionsoft.bookshelf.domain.repository.BookRepository
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
}