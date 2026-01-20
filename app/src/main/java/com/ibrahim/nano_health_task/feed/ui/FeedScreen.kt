package com.ibrahim.nano_health_task.feed.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ibrahim.nano_health_task.feed.model.ImageMedia
import com.ibrahim.nano_health_task.feed.model.Post
import com.ibrahim.nano_health_task.feed.model.VideoMedia
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FeedScreen(viewModel: FeedViewModel, modifier: Modifier = Modifier) {
    val posts by viewModel.posts.collectAsState()
    val activePostId by viewModel.activePostId.collectAsState()

    val listState = rememberLazyListState()

    // Observe visible items and decide which post should be active for autoplay
    LaunchedEffect(listState, Unit) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collectLatest { visibleInfo ->
                if (visibleInfo.isNotEmpty()) {
                    val selected = visibleInfo.maxByOrNull { it.size * it.visibleFraction() }
                    selected?.let { info ->
                        val index = info.index
                        val post = posts.getOrNull(index)
                        viewModel.setActivePost(post?.id)
                    }
                } else {
                    viewModel.setActivePost(null)
                }
            }
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            items(posts, key = { it.id }) { post ->
                PostItem(
                    post = post,
                    play = post.id == activePostId
                )
            }
        }
    }
}

@Composable
private fun PostItem(post: Post, play: Boolean) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "@${post.author}", modifier = Modifier.padding(8.dp))
            Text(text = post.caption, modifier = Modifier.padding(horizontal = 8.dp))

            Spacer(modifier = Modifier.height(8.dp))

            // Render media - show first image/video, then rest below
            for (m in post.media) {
                when (m) {
                    is ImageMedia -> {
                        AsyncImage(
                            model = m.resId,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        )
                    }
                    is VideoMedia -> {
                        // play only if 'play' is true
                        VideoPlayer(
                            videoResId = m.resId,
                            playWhenReady = play,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// Helper extension to approximate visible fraction for LazyListItemInfo
private fun androidx.compose.foundation.lazy.LazyListItemInfo.visibleFraction(): Float {
    // visibleFraction is approximate; can be refined
    val visibleHeight = when {
        this.offset >= 0 -> (this.size - this.offset)
        else -> (this.size + this.offset)
    }
    return (visibleHeight.toFloat() / this.size.toFloat()).coerceIn(0f, 1f)
}
