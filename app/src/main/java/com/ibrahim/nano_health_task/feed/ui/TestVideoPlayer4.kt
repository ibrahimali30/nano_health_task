package com.ibrahim.nano_health_task.feed.ui

import androidx.compose.ui.tooling.preview.Preview
import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

// --- 1. Cache Singleton Management ---
object VideoCache {
    private var simpleCache: SimpleCache? = null

    fun getInstance(context: Context): SimpleCache {
        if (simpleCache == null) {
            val cacheDir = File(context.cacheDir, "exo_video_cache")
            val size = 100 * 1024 * 1024L // 100MB cache limit
            val evictor = LeastRecentlyUsedCacheEvictor(size)
            val databaseProvider = StandaloneDatabaseProvider(context)
            simpleCache = SimpleCache(cacheDir, evictor, databaseProvider)
        }
        return simpleCache!!
    }
}

@Composable
fun AutoPlayVideoList4(videoUrls: List<String>) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val listState = rememberLazyListState()

    // --- 2. Setup Cached ExoPlayer ---
    val exoPlayer = remember {
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

    val playingItemIndex = remember { mutableStateOf<Int?>(null) }

    // --- 3. Scroll Detection Logic ---
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                if (visibleItems.isNotEmpty()) {
                    val viewportCenter = (listState.layoutInfo.viewportEndOffset + listState.layoutInfo.viewportStartOffset) / 2
                    val centerItem = visibleItems.minByOrNull {
                        kotlin.math.abs((it.offset + it.size / 2) - viewportCenter)
                    }
                    playingItemIndex.value = centerItem?.index
                }
            }
    }

    // --- 4. Handle Media Switching ---
    LaunchedEffect(playingItemIndex.value) {
        val index = playingItemIndex.value
        if (index != null && index < videoUrls.size) {
            val mediaItem = MediaItem.fromUri(videoUrls[index])
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    // --- 5. Lifecycle Management ---
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> {
                    if (playingItemIndex.value != null) exoPlayer.play()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        itemsIndexed(videoUrls) { index, url ->
            VideoItem(
                exoPlayer = exoPlayer,
                isPlaying = index == playingItemIndex.value
            )
        }
    }
}

@Composable
fun VideoItem(
    exoPlayer: ExoPlayer,
    isPlaying: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(vertical = 4.dp)
            .background(Color.Black)
    ) {
        if (isPlaying) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        player = exoPlayer
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Placeholder: In a real app, you'd show a thumbnail here
            Box(modifier = Modifier.fillMaxSize().background(Color.Red))
        }
    }
}

@Composable
@Preview
fun Preview4() {
    AutoPlayVideoList4(videos.map { it.url })
}

@Composable
@Preview
fun Preview5() {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data("https://budgetstockphoto.com/samples/pics/dice.jpg")
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
    )
}

data class VideoItem(
    val id: String,
    val url: String
)

val videos = listOf(
    VideoItem("1", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
    VideoItem("3", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"),
    VideoItem("2", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"),
    VideoItem("2", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4"),
    VideoItem("2", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/VolkswagenGTIReview.mp4"),
    VideoItem("2", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"),
    VideoItem("2", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4"),
    VideoItem("2", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4"),
    VideoItem("2", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4"),
    VideoItem("2", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"),
)

//https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
//https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"),
//https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4
//https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4
//https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/VolkswagenGTIReview.mp4
//https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4
//https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4
//https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4
//https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4
//https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
//
//https://budgetstockphoto.com/samples/pics/dice.jpg
//https://budgetstockphoto.com/samples/pics/padlock.jpg
//https://budgetstockphoto.com/samples/pics/ring.jpg
//https://budgetstockphoto.com/samples/pics/carspeed.jpg
//https://budgetstockphoto.com/samples/pics/creditcards.jpg
//https://budgetstockphoto.com/samples/pics/integratedcircuit.jpg
//https://budgetstockphoto.com/samples/pics/pills.jpg
//https://budgetstockphoto.com/samples/pics/swan.jpg
//https://budgetstockphoto.com/samples/pics/rainbow.jpg