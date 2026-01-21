package com.ibrahim.nano_health_task.feed.model

import androidx.annotation.DrawableRes
import com.ibrahim.nano_health_task.feed.ui.videos

sealed interface Media {
    val id: String
}

data class ImageMedia(
    override val id: String,
    @DrawableRes val resId: Int
) : Media

data class VideoMedia(
    override val id: String,
    val url: String = videos.random().url
) : Media

data class Post(
    val id: String,
    val author: String,
    val caption: String,
    val media: List<Media>
)
