package com.ibrahim.nano_health_task.feed.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ibrahim.nano_health_task.feed.model.ImageMedia
import com.ibrahim.nano_health_task.feed.model.Post
import com.ibrahim.nano_health_task.feed.model.VideoMedia
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun FeedScreen(viewModel: FeedViewModel, modifier: Modifier = Modifier) {
    val posts by viewModel.posts.collectAsState()
    val activePostId by viewModel.activePostId.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()

    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Pause playback while scrolling to avoid heavy operations during flings;
    // after scrolling stops, pick the center-most item and start playback with a small debounce.
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collectLatest { scrolling ->
                if (scrolling) {
                    // Pause playback while the user is actively scrolling
                    if (viewModel.activePostId.value != null) {
//                        viewModel.setActivePost(null)
                    }
                } else {
                    // Wait briefly for scroll/fling to settle
                    delay(300)
                    // determine the most visible item and activate it IF it is sufficiently visible
                    val visibleInfo = listState.layoutInfo.visibleItemsInfo
                    if (visibleInfo.isNotEmpty()) {
                        // pick item with largest visible fraction
                        val selected = visibleInfo.maxByOrNull { it.visibleFraction() }
                        selected?.let { info ->
                            val frac = info.visibleFraction()
                            // require a dominant visibility (e.g., >60%) to autoplay
                            if (frac >= 0.6f) {
                                val index = info.index
                                val post = posts.getOrNull(index)
                                viewModel.setActivePost(post?.id)

                                // pagination trigger: if near end, load next (earlier threshold)
                                if (index >= posts.size - 3) {
                                    viewModel.loadNextPage()
                                }
                            } else {
                                // No dominant item; keep playback paused
                                viewModel.setActivePost(null)
                            }
                        }
                    }
                }
            }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        SwipeRefresh(state = rememberSwipeRefreshState(isRefreshing), onRefresh = { viewModel.refresh() }) {
            Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(innerPadding)) {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(posts, key = { it.id }) { post ->
                        PostItem(
                            post = post,
                            play = post.id == activePostId,
                        )
                    }

                    if (isLoadingMore) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }

        Text(modifier = Modifier.padding(top = 100.dp) ,text = activePostId.toString())
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
                        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                            VideoPlayer(
                                media = m,
                                playWhenReady = play,
                            )
                        }
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
