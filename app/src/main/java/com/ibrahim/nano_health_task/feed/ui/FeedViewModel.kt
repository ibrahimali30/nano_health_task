package com.ibrahim.nano_health_task.feed.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibrahim.nano_health_task.feed.data.FeedRepository
import com.ibrahim.nano_health_task.feed.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class FeedViewModel(
    private val repository: FeedRepository = FeedRepository()
) : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    // ID of the post that should currently play its video(s)
    private val _activePostId = MutableStateFlow<String?>(null)
    val activePostId: StateFlow<String?> = _activePostId.asStateFlow()

    init {
        repository.getFeed()
            .onEach { _posts.value = it }
            .launchIn(viewModelScope)
    }

    fun setActivePost(postId: String?) {
        _activePostId.value = postId
    }
}

