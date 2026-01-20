package com.ibrahim.nano_health_task.feed.model

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes

sealed interface Media {
    val id: String
}

data class ImageMedia(
    override val id: String,
    @DrawableRes val resId: Int
) : Media

data class VideoMedia(
    override val id: String,
    @RawRes val resId: Int
) : Media

data class Post(
    val id: String,
    val author: String,
    val caption: String,
    val media: List<Media>
)
