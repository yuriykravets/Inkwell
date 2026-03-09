package com.partitionsoft.bookshelf.data

import com.example.bookshelf.BuildConfig
import com.partitionsoft.bookshelf.network.BookService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val booksRepository: BooksRepository
}

class DefaultAppContainer : AppContainer {
    private val BASE_URL = "https://www.googleapis.com/books/v1/"

    private val apiKeyInterceptor = Interceptor { chain ->
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter("key", BuildConfig.BOOKS_API_KEY)
            .build()
        chain.proceed(original.newBuilder().url(url).build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(apiKeyInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .baseUrl(BASE_URL)
        .build()

    private val retrofitService: BookService by lazy {
        retrofit.create(BookService::class.java)
    }

    override val booksRepository: BooksRepository by lazy {
        NetworkBooksRepository(retrofitService)
    }
}