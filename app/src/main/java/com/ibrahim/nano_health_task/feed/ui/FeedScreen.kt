package com.ibrahim.nano_health_task.feed.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ibrahim.nano_health_task.feed.model.ImageMedia
import com.ibrahim.nano_health_task.feed.model.Post
import com.ibrahim.nano_health_task.feed.model.VideoMedia
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(viewModel: FeedViewModel, modifier: Modifier = Modifier) {
    val posts by viewModel.posts.collectAsState()
    val activePostId by viewModel.activePostId.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()

    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val bufferingMap = remember { mutableStateMapOf<String, Boolean>() }

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
                            setBuffering = { bufferingMap[post.id] = it },
                            viewModel = viewModel
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
    }
}

@Composable
private fun PostItem(post: Post, play: Boolean, setBuffering: (Boolean) -> Unit, viewModel: FeedViewModel) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                val avatarUrl = remember(post.id) {"https://budgetstockphoto.com/samples/pics/swan.jpg" }

                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(text = "@${post.author}")

            }

            Text(text = post.caption, modifier = Modifier.padding(horizontal = 8.dp))

            Spacer(modifier = Modifier.height(8.dp))

            // Render media: if only one item show it full-width, otherwise show a 2-column grid (up to 4 items visible)
            val mediaCount = post.media.size

            if (mediaCount <= 1) {
                // single media: existing behavior
                post.media.firstOrNull()?.let { mediaItem ->
                    when (mediaItem) {
                        is ImageMedia -> {
                            AsyncImage(
                                model = mediaItem.mediaUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        is VideoMedia -> {
                            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                                VideoPlayer(
                                    media = mediaItem,
                                    playWhenReady = play,
                                )
                            }
                        }
                    }
                }
            } else {
                // Grid: show up to 4 items in a 2x2 grid. On last item show +N overlay for remaining items.
                val visible = post.media.take(4)
                val rows = (visible.size + 1) / 2
                Column {
                    for (r in 0 until rows) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            val leftIndex = r * 2
                            val rightIndex = leftIndex + 1

                            val left = visible.getOrNull(leftIndex)
                            val right = visible.getOrNull(rightIndex)

                            // Left cell
                            MediaGridCell(
                                media = left,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(180.dp)
                                    .clickable(enabled = left != null) {
                                    },
                                overlayText = if (leftIndex == 3 && mediaCount > 4) "+${mediaCount - 4}" else null
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            // Right cell
                            MediaGridCell(
                                media = right,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(180.dp)
                                    .clickable(enabled = right != null) {},
                                overlayText = if (rightIndex == 3 && mediaCount > 4) "+${mediaCount - 4}" else null
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom action bar: like, comment, share
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {}
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Like",
                    )
                }

                Text(text = "73", modifier = Modifier.padding(end = 16.dp))

                IconButton(onClick = { }) {
                    Icon(imageVector = Icons.Filled.Comment, contentDescription = "Comment")
                }

                IconButton(onClick = { /* share intent */ }) {
                    Icon(imageVector = Icons.Filled.Share, contentDescription = "Share")
                }
            }
        }
    }
}

@Composable
private fun MediaGridCell(media: com.ibrahim.nano_health_task.feed.model.Media?, modifier: Modifier = Modifier, onClick: (Int) -> Unit = {}, overlayText: String?) {
    Box(modifier = modifier) {
        if (media == null) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)))
            return@Box
        }

        when (media) {
            is ImageMedia -> {
                AsyncImage(
                    model = media.mediaUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            is VideoMedia -> {
                val thumb = media.placeHolder
                if (thumb.isNotEmpty()) {
                    AsyncImage(
                        model = thumb,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)))
                }

                IconButton(onClick = { /* play handled by viewer */ }, modifier = Modifier.background(Color.Black).align(Alignment.Center)) {
                    Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White)
                }
            }
        }

        if (!overlayText.isNullOrBlank()) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f)), contentAlignment = Alignment.Center) {
                Text(text = overlayText, color = Color.White)
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
