package com.mg.statussaver.presentation.screens.status


import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.mg.statussaver.presentation.screens.home.MediaType
import com.mg.statussaver.presentation.screens.home.StatusItem
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun FullscreenVideoPlayer(videoPath: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoPath.toUri())
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    var isPlaying by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Play/Pause Button
        IconButton(
            onClick = {
                isPlaying = !isPlaying
                if (isPlaying) exoPlayer.play() else exoPlayer.pause()
            },
            modifier = Modifier
                .align(Alignment.Center)
                .size(72.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = "Play/Pause",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun StatusViewerScreen(
    statusList: List<StatusItem>,
    initialIndex: Int,
    onBack: () -> Unit,
    onShare: (StatusItem) -> Unit,
    onDownload: (StatusItem) -> Unit
) {
     val context = LocalContext.current

    if (statusList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No media to display", color = Color.White)
        }
        return
    }

    val pagerState = rememberPagerState(initialPage = initialIndex)
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        HorizontalPager(
            count = statusList.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val item = statusList[page]
            if (item.type == MediaType.VIDEO) {
                FullscreenVideoPlayer(videoPath = item.path)
            } else {
                AsyncImage(
                    model = item.path,
                    contentDescription = "Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Left arrow for navigation
        IconButton(
            onClick = {
                if (pagerState.currentPage > 0) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Previous",
                tint = Color.White
            )
        }

        // Right arrow for navigation
        IconButton(
            onClick = {
                if (pagerState.currentPage < statusList.size - 1) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Next",
                tint = Color.White
            )
        }


        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0f)),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            IconButton(onClick = {
                val item = statusList[pagerState.currentPage]
                shareToWhatsApp(context, item)
            }) {
                Icon(
                    Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "Share",
                    tint = Color.White
                )
            }
        }

        // Bottom actions
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0f)),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = {
                val item = statusList[pagerState.currentPage]
                onShare(item)
            }) {
                Icon(Icons.Outlined.Share, "Share", tint = Color.White)
            }

            IconButton(onClick = {
                val item = statusList[pagerState.currentPage]
                onDownload(item)
            }) {
                Icon(Icons.Outlined.Download, "Download", tint = Color.White)
            }
        }
    }

    BackHandler(onBack = onBack)
}


fun shareToWhatsApp(context: Context, item: StatusItem) {
    val uri = item.path.toUri()
    val isDocumentUri = uri.scheme == "content"

    val file: File = if (isDocumentUri) {
        // Copy to cache
        val inputStream = context.contentResolver.openInputStream(uri)
        val ext = if (item.type == MediaType.VIDEO) ".mp4" else ".jpg"
        val tempFile = File(context.cacheDir, "shared${System.currentTimeMillis()}$ext")
        inputStream?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        tempFile
    } else {
        File(item.path)
    }

    val fileUri = androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = if (item.type == MediaType.VIDEO) "video/*" else "image/*"
        putExtra(Intent.EXTRA_STREAM, fileUri)
        setPackage("com.whatsapp")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
    }
}