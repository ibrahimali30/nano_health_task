package com.ibrahim.nano_health_task.feed.video

import android.content.Context
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.*
import java.io.File

object PlayerCache {
    @Volatile
    private var simpleCache: SimpleCache? = null

    fun getOrCreate(context: Context): SimpleCache {
        return simpleCache ?: synchronized(this) {
            simpleCache ?: run {
                val cacheDir = File(context.cacheDir, "exo_cache")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                val evictor = LeastRecentlyUsedCacheEvictor(50L * 1024L * 1024L) // 50MB
                val databaseProvider = ExoDatabaseProvider(context)
                val cache = SimpleCache(cacheDir, evictor, databaseProvider)
                simpleCache = cache
                cache
            }
        }
    }

    fun getCacheDataSourceFactory(context: Context, upstreamFactory: com.google.android.exoplayer2.upstream.DataSource.Factory): CacheDataSource.Factory {
        val cache = getOrCreate(context)
        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(CacheDataSink.Factory().setCache(cache).setFragmentSize(CacheDataSink.DEFAULT_FRAGMENT_SIZE))
    }
}

