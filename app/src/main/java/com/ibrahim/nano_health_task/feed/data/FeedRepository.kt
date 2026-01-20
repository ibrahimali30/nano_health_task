package com.ibrahim.nano_health_task.feed.data

import com.ibrahim.nano_health_task.feed.model.Post
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FeedRepository {
    fun getFeed(): Flow<List<Post>> = flow {
        emit(SampleFeed.posts)
    }
}

