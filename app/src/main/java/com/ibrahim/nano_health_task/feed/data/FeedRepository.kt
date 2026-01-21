package com.ibrahim.nano_health_task.feed.data

import com.ibrahim.nano_health_task.feed.model.Post
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FeedRepository @Inject constructor() {
    private val all = SampleFeed.posts

    // Simulate network/pagination: return next page based on page index
    suspend fun loadPage(page: Int, pageSize: Int = 3): List<Post> {
        // Simulate latency
        delay(300)
        val start = page * pageSize
        if (start >= all.size) return emptyList()
        val end = (start + pageSize).coerceAtMost(all.size)
        // For demo, shuffle or map ids to make unique
        return all.subList(start, end).map { p ->
            p.copy(id = p.id + "-p${page}")
        }
    }

    // Simulate refresh
    suspend fun refresh(): List<Post> {
        delay(300)
        return all
    }

    fun getFeed(): Flow<List<Post>> = flow {
        emit(SampleFeed.posts)
    }
}
