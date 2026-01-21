package com.ibrahim.nano_health_task.feed.ui

import android.content.Context
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource


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
            val player = buildExoPlayer(context)
            val mediaItem = MediaItem.fromUri(media.url.toUri())
            player.setMediaItem(mediaItem)
            val listener = object : Player.Listener {
                override fun onIsLoadingChanged(isLoading: Boolean) {
                    isBuffering = isLoading
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    isPlayingState = isPlaying
                }
            }
            player.addListener(listener)
            player.prepare()
            player.playWhenReady = true
            player.play()
            exoPlayer = player

            onDispose {
                player.removeListener(listener)
                player.stop()
                player.release()
                exoPlayer = null
                isBuffering = false
            }
        } else {
            // ensure any existing player is released when not playing
            exoPlayer?.let { p ->
                p.pause(); p.stop(); p.release()
                exoPlayer = null
                isBuffering = false
                isPlayingState = false
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
            // Show placeholder image while not playing (use media.thumbnailUrl or media.url)
            val thumbUrl = media.placeHolder
            if (thumbUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(thumbUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                )
            } else {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)))
            }
        }

        if (isBuffering && !isPlayingState) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        val isPlaying = exoPlayer?.playWhenReady == true && exoPlayer?.isPlaying == true
        if (isPlayingState.not() || exoPlayer == null) {
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


fun buildExoPlayer(context: Context) = run {
    // Create the DataSource Factory with Cache support
    val httpDataSourceFactory = DefaultHttpDataSource.Factory()
    val cacheDataSourceFactory = CacheDataSource.Factory()
        .setCache(VideoCache.getInstance(context))
        .setUpstreamDataSourceFactory(httpDataSourceFactory)
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    ExoPlayer.Builder(context)
        .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
        .build()
        .apply {
            repeatMode = Player.REPEAT_MODE_ONE
        }
}
