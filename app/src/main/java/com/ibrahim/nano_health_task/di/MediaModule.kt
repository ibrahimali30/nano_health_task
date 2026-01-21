package com.ibrahim.nano_health_task.di

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import java.io.File

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {
    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        return ExoPlayer.Builder(context).build()
    }

    @Provides
    @Singleton
    fun provideSimpleCache(@ApplicationContext context: Context): SimpleCache {
        val cacheDir = File(context.cacheDir, "exo_cache")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val evictor = LeastRecentlyUsedCacheEvictor(50L * 1024L * 1024L)
        val dbProvider = ExoDatabaseProvider(context)
        return SimpleCache(cacheDir, evictor, dbProvider)
    }

    @Provides
    fun provideDefaultDataSourceFactory(@ApplicationContext context: Context): DefaultDataSource.Factory {
        return DefaultDataSource.Factory(context)
    }
}
