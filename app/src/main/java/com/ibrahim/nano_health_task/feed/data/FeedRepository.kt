package com.ibrahim.nano_health_task.feed.data

import com.ibrahim.nano_health_task.feed.data.local.FeedDao
import com.ibrahim.nano_health_task.feed.data.local.MediaEntity
import com.ibrahim.nano_health_task.feed.data.local.PostEntity
import com.ibrahim.nano_health_task.feed.model.ImageMedia
import com.ibrahim.nano_health_task.feed.model.Post
import com.ibrahim.nano_health_task.feed.model.VideoMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FeedRepository @Inject constructor(
    private val remote: RemoteFeedsRepository,
    private val dao: FeedDao
) {
    private var currentPage = 0

    suspend fun loadPage(page: Int, pageSize: Int = 10): List<Post> {
        return withContext(Dispatchers.IO) {
            try {
                // Remote API: append page query param to endpoint (assume ApiService supports it via implementation)
                val fetched = remote.fetchAllPage(page)
                if (fetched.isNotEmpty()) {
                    // map to entity and persist
                    val entities = fetched.map { p ->
                        PostEntity(
                            id = p.id,
                            author = p.author,
                            caption = p.caption,
                            media = p.media.map { m ->
                                when (m) {
                                    is ImageMedia -> MediaEntity(id = m.id, type = "image", url = m.url, thumbnailUrl = null)
                                    is VideoMedia -> MediaEntity(id = m.id, type = "video", url = m.url, thumbnailUrl = m.thumbnailUrl)
                                    else -> MediaEntity(id = m.id, type = "unknown", url = null, thumbnailUrl = null)
                                }
                            }
                        )
                    }
                    dao.insertAll(entities)
                    fetched
                } else {
                    // no new data -> return cached
                    val cached = dao.getAll()
                    cached.map { entity ->
                        Post(
                            id = entity.id,
                            author = entity.author,
                            caption = entity.caption,
                            media = entity.media.map { me ->
                                if (me.type == "video") VideoMedia(id = me.id, url = me.url ?: "", thumbnailUrl = me.thumbnailUrl)
                                else ImageMedia(id = me.id, url = me.url)
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                // network or mapping error -> load cached
                val cached = dao.getAll()
                cached.map { entity ->
                    Post(
                        id = entity.id,
                        author = entity.author,
                        caption = entity.caption,
                        media = entity.media.map { me ->
                            if (me.type == "video") VideoMedia(id = me.id, url = me.url ?: "", thumbnailUrl = me.thumbnailUrl)
                            else ImageMedia(id = me.id, url = me.url)
                        }
                    )
                }
            }
        }
    }

    suspend fun refresh(): List<Post> {
        currentPage = 0
        val all = loadPage(0)
        return all
    }
}
