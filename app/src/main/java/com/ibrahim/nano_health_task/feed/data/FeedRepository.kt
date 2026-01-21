package com.ibrahim.nano_health_task.feed.data

import com.ibrahim.nano_health_task.feed.model.Post
import javax.inject.Inject

class FeedRepository @Inject constructor(
    private val remote: RemoteFeedsRepository
) {
    suspend fun loadPage(page: Int, pageSize: Int = 3): List<Post> {
        val fetchedFromRemote = remote.fetchAll()
        return fetchedFromRemote
    }

    suspend fun refresh(): List<Post> {
        val fetchedFromRemote = remote.fetchAll()
        return fetchedFromRemote
    }
}
