package com.ibrahim.nano_health_task.feed.ui

import com.ibrahim.nano_health_task.feed.data.FeedRepository
import com.ibrahim.nano_health_task.feed.model.ImageMedia
import com.ibrahim.nano_health_task.feed.model.Post
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {
    private val repository = mockk<FeedRepository>()
    private lateinit var viewModel: FeedViewModel
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        // Provide repository behavior
        coEvery { repository.loadPage(0, any()) } returns listOf(
            Post(id = "p1", author = "a", caption = "c", media = listOf(ImageMedia(id = "i1", url = "u")))
        )

        viewModel = FeedViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load populates posts`() = runTest {
        // let init coroutine run
        advanceUntilIdle()
        val posts = viewModel.posts.value
        assertEquals(1, posts.size)
        assertEquals("p1", posts[0].id)
    }

    @Test
    fun `loadNextPage appends posts`() = runTest {
        coEvery { repository.loadPage(1, any()) } returns listOf(
            Post(id = "p2", author = "b", caption = "c2", media = emptyList())
        )

        viewModel.loadNextPage()
        advanceUntilIdle()
        val posts = viewModel.posts.value
        // Should now contain initial + new appended
        assertEquals(2, posts.size)
    }
}
