package com.partitionsoft.bookshelf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.partitionsoft.bookshelf.ui.BooksApp
import com.partitionsoft.bookshelf.ui.theme.BookShelfTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookShelfTheme {
                BooksApp()
            }
        }
    }
}