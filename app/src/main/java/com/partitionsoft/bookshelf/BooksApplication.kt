package com.partitionsoft.bookshelf

import android.app.Application
import com.partitionsoft.bookshelf.data.AppContainer
import com.partitionsoft.bookshelf.data.DefaultAppContainer

class BooksApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}