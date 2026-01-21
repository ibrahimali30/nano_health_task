package com.ibrahim.nano_health_task.di

import android.content.Context
import androidx.room.Room
import com.ibrahim.nano_health_task.data.network.ApiService
import com.ibrahim.nano_health_task.feed.data.RemoteFeedsRepository
import com.ibrahim.nano_health_task.feed.data.local.FeedDao
import com.ibrahim.nano_health_task.feed.data.local.FeedDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRemoteFeedsRepository(api: ApiService): RemoteFeedsRepository {
        return RemoteFeedsRepository(api)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FeedDatabase {
        return Room.databaseBuilder(context, FeedDatabase::class.java, "feeds.db").build()
    }

    @Provides
    @Singleton
    fun provideFeedDao(db: FeedDatabase): FeedDao = db.feedDao()
}
