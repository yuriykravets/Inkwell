package com.partitionsoft.bookshelf

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import com.partitionsoft.bookshelf.ui.BooksApp
import com.partitionsoft.bookshelf.ui.theme.BookShelfTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = true
        window.statusBarColor = Color.WHITE
        setContent {
            BookShelfTheme {
                BooksApp(
                    onBookClicked = {
                        ContextCompat.startActivity(
                            this,
                            Intent(Intent.ACTION_VIEW, it.previewLink?.toUri()),
                            null
                        )
                    }
                )
            }
        }
    }
}