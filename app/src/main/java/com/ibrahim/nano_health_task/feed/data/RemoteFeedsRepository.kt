package com.ibrahim.nano_health_task.feed.data

import com.ibrahim.nano_health_task.data.network.ApiService
import com.ibrahim.nano_health_task.feed.model.ImageMedia
import com.ibrahim.nano_health_task.feed.model.Post
import com.ibrahim.nano_health_task.feed.model.VideoMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoteFeedsRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun fetchAll(): List<Post> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getHomeFeeds()
            val posts = resp.posts
            return@withContext posts.mapIndexed { pi, netPost ->
                val media = netPost.media.mapIndexed { idx, m ->
                    val id = m.id ?: "m-${netPost.id}-$idx"
                    when (m.mediaType?.lowercase()) {
                        "image" -> ImageMedia(id = id, url = m.url)
                        "video" -> VideoMedia(id = id, url = m.url ?: "", thumb = m.thumb ?: "")
                        else -> ImageMedia(id = id, url = m.url)
                    }
                }
                Post(
                    id = netPost.id,
                    author = netPost.author ?: "",
                    caption = netPost.caption ?: "",
                    media = media
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
