package com.ibrahim.nano_health_task.feed.video

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import java.io.File

object MediaProvider {
    @Volatile
    private var exoPlayer: ExoPlayer? = null

    @Volatile
    private var simpleCache: SimpleCache? = null

    fun init(context: Context) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
        }
        if (simpleCache == null) {
            val cacheDir = File(context.cacheDir, "exo_cache")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val evictor = LeastRecentlyUsedCacheEvictor(50L * 1024L * 1024L)
            val dbProvider = ExoDatabaseProvider(context)
//            simpleCache = SimpleCache(cacheDir, evictor, dbProvider)
        }
    }

    fun getPlayer(): ExoPlayer = exoPlayer ?: throw IllegalStateException("MediaProvider not initialized")

    fun getDefaultDataSourceFactory(context: Context): DefaultDataSource.Factory = DefaultDataSource.Factory(context)

    fun getCacheDataSourceFactory(context: Context): CacheDataSource.Factory {
        val cache = simpleCache ?: throw IllegalStateException("Cache not initialized")
        val upstream = DefaultDataSource.Factory(context)
        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstream)
            .setCacheWriteDataSinkFactory(CacheDataSink.Factory().setCache(cache).setFragmentSize(CacheDataSink.DEFAULT_FRAGMENT_SIZE))
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
        simpleCache?.release()
        simpleCache = null
    }
}

