package com.ibrahim.nano_health_task.feed.data

import com.ibrahim.nano_health_task.feed.data.local.FeedDao
import com.ibrahim.nano_health_task.feed.data.local.MediaEntity
import com.ibrahim.nano_health_task.feed.data.local.PostEntity
import com.ibrahim.nano_health_task.feed.model.ImageMedia
import com.ibrahim.nano_health_task.feed.model.Post
import com.ibrahim.nano_health_task.feed.model.VideoMedia
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedRepositoryTest {
    private val remote = mockk<RemoteFeedsRepository>()
    private val dao = mockk<FeedDao>(relaxed = true)
    private lateinit var repo: FeedRepository

    @Before
    fun setup() {
        repo = FeedRepository(remote, dao)
    }

    @Test
    fun `loadPage returns remote posts when available and inserts into DB`() = runTest {
        val netPosts = listOf(
            Post(id = "p1", author = "a", caption = "c", media = listOf(ImageMedia(id = "i1", url = "https://example.com/i1.jpg")))
        )
        coEvery { remote.fetchAllPage(0) } returns netPosts

        val result = repo.loadPage(0)

        assertEquals(1, result.size)
        assertEquals("p1", result[0].id)
        coVerify { dao.insertAll(any()) }
    }

    @Test
    fun `loadPage falls back to cached on remote error`() = runTest {
        coEvery { remote.fetchAllPage(1) } throws RuntimeException("network")
        val cached = listOf(
            PostEntity(id = "pCached", author = "x", caption = "y", media = listOf(MediaEntity("m1", "image", "u", null)))
        )
        coEvery { dao.getAll() } returns cached

        val result = repo.loadPage(1)

        assertEquals(1, result.size)
        assertEquals("pCached", result[0].id)
    }
}

