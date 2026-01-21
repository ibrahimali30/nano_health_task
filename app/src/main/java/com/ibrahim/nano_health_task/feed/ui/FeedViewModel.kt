package com.ibrahim.nano_health_task.feed.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibrahim.nano_health_task.feed.data.FeedRepository
import com.ibrahim.nano_health_task.feed.model.Post
import com.ibrahim.nano_health_task.feed.model.VideoMedia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FeedViewModel(
    private val repository: FeedRepository = FeedRepository()
) : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    // ID of the post that should currently play its video(s)
    private val _activePostId = MutableStateFlow<String?>(null)
    val activePostId: StateFlow<String?> = _activePostId.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private var currentPage = 0

    init {
        repository.getFeed()
            .onEach { _posts.value = it }
            .launchIn(viewModelScope)

        // initial load
        viewModelScope.launch {
            val first = repository.loadPage(currentPage)
            _posts.value = first
        }
    }

    fun onPlayVideoClicked(videoUrl: VideoMedia) {
        setActivePost(videoUrl.id)
    }
    fun setActivePost(postId: String?) {
        _activePostId.value = postId
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val refreshed = repository.refresh()
            _posts.value = refreshed
            currentPage = 0
            _isRefreshing.value = false
        }
    }

    fun loadNextPage() {
        if (_isLoadingMore.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            currentPage += 1
            val next = repository.loadPage(currentPage)
            if (next.isNotEmpty()) {
                _posts.value = _posts.value + next
            }
            _isLoadingMore.value = false
        }
    }
}
