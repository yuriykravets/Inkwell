package com.partitionsoft.bookshelf.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.partitionsoft.bookshelf.ui.ReaderUiState
import com.partitionsoft.bookshelf.ui.ReaderViewModel

@Composable
fun ReaderRoute(
    onBackClicked: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        topBar = {
            TopAppBar(
                title = { Text(text = "Reader") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            ReaderUiState.Loading -> LoadingScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            ReaderUiState.Error -> ErrorScreen(
                retryAction = viewModel::loadReader,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            ReaderUiState.Unavailable -> ReaderUnavailable(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            is ReaderUiState.Ready -> ReaderWebView(
                url = state.readerUrl,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onCloseRequested = onBackClicked
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ReaderWebView(
    url: String,
    modifier: Modifier = Modifier,
    onCloseRequested: () -> Unit
) {
    var webView: WebView? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    BackHandler(enabled = webView?.canGoBack() == true) {
        webView?.goBack()
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webView = this
                    CookieManager.getInstance().setAcceptCookie(true)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                            hasError = false
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            if (request?.isForMainFrame == true) {
                                hasError = true
                                isLoading = false
                            }
                        }
                    }
                    loadUrl(url)
                }
            },
            update = { existing ->
                if (existing.url != url) {
                    existing.loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        if (hasError) {
            ReaderUnavailable(
                modifier = Modifier.fillMaxSize(),
                onCloseRequested = onCloseRequested
            )
        }
    }
}

@Composable
private fun ReaderUnavailable(
    modifier: Modifier = Modifier,
    onCloseRequested: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Preview is unavailable for this book.",
            style = MaterialTheme.typography.h6
        )
        if (onCloseRequested != null) {
            androidx.compose.material.TextButton(onClick = onCloseRequested) {
                Text(text = "Back")
            }
        }
    }
}

