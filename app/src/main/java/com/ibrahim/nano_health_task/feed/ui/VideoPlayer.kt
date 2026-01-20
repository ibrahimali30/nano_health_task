package com.ibrahim.nano_health_task.feed.ui

import android.net.Uri
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import androidx.core.net.toUri
import com.ibrahim.nano_health_task.feed.video.PlayerCache

@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    videoResId: Int,
    playWhenReady: Boolean
) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    DisposableEffect(key1 = exoPlayer, key2 = videoResId) {
        val uriString = "android.resource://${context.packageName}/$videoResId"
        val uri: Uri = uriString.toUri()

        val upstreamFactory = DefaultDataSource.Factory(context)
        // initialize cache but avoid unused local variable warning - used later if needed
        PlayerCache.getCacheDataSourceFactory(context, upstreamFactory)

        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(playWhenReady) {
        exoPlayer.playWhenReady = playWhenReady
        if (playWhenReady) exoPlayer.play()
        else exoPlayer.pause()
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
                player = exoPlayer
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }
    )
}
