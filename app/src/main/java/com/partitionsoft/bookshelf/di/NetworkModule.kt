package com.partitionsoft.bookshelf.di

import com.example.bookshelf.BuildConfig
import com.partitionsoft.bookshelf.data.remote.api.BookService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val url = request.url.newBuilder()
                    .addQueryParameter("key", BuildConfig.BOOKS_API_KEY)
                    .build()
                chain.proceed(request.newBuilder().url(url).build())
            }
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/books/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideBookService(retrofit: Retrofit): BookService =
        retrofit.create(BookService::class.java)


}
