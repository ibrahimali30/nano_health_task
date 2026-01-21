package com.ibrahim.nano_health_task.feed.ui

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.ibrahim.nano_health_task.feed.model.VideoMedia


@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    media: VideoMedia,
    playWhenReady: Boolean,
    viewModel: FeedViewModel = viewModel(),
) {
    val context = LocalContext.current

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var isBuffering by remember { mutableStateOf(false) }
    var isPlayingState by remember { mutableStateOf(false) }
    val activePostId by viewModel.activePostId.collectAsState()

    val play = media.id == activePostId
    // create/release player based on playWhenReady
    DisposableEffect(playWhenReady, media.id, activePostId) {
        if (playWhenReady || play) {
            val p = ExoPlayer.Builder(context).build()
            val uri = media.url.toUri()
            val mediaItem = MediaItem.fromUri(uri)
            p.setMediaItem(mediaItem)
            val listener = object : Player.Listener {
                override fun onIsLoadingChanged(isLoading: Boolean) {
                    isBuffering = isLoading
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    isPlayingState = isPlaying
                }
            }
            p.addListener(listener)
            p.prepare()
            p.playWhenReady = true
            p.play()
            exoPlayer = p

            onDispose {
                p.removeListener(listener)
                p.stop()
                p.release()
                exoPlayer = null
                isBuffering = false
            }
        } else {
            // ensure any existing player is released when not playing
            exoPlayer?.let { p ->
                p.pause(); p.stop(); p.release()
                exoPlayer = null
                isBuffering = false
            }
            onDispose { /* nothing */ }
        }
    }

    Box(modifier = modifier) {
        val currentPlayer = exoPlayer
        if (currentPlayer != null) {
            AndroidView(
                modifier = Modifier
                    .clickable {
                        if (exoPlayer?.isPlaying == false) exoPlayer?.play() else exoPlayer?.pause()
                    }
                    .fillMaxSize(),
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        player = currentPlayer
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                }
            )
        } else {
            // Show placeholder box while not playing
            Box(modifier = Modifier.fillMaxSize())
        }

        if (isBuffering && !isPlayingState) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        val isPlaying = exoPlayer?.playWhenReady == true
        if (isPlaying.not()) {
            IconButton(
                onClick = {
                    if (exoPlayer == null){
                        viewModel.onPlayVideoClicked(videoUrl = media)
                    }else{
                        exoPlayer?.play()
                    }

                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
            }

        }
    }
}
