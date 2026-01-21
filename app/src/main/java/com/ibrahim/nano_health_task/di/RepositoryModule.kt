package com.ibrahim.nano_health_task.di

import com.ibrahim.nano_health_task.data.network.ApiService
import com.ibrahim.nano_health_task.feed.data.RemoteFeedsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
}
