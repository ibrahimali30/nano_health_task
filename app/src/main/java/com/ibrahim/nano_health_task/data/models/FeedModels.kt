package com.ibrahim.nano_health_task.data.models

data class PostsResponse(
    val posts: List<Post> = emptyList()
)

data class Post(
    val id: String,
    val author: String?,
    val caption: String?,
    val media: List<MediaItem> = emptyList()
)

data class MediaItem(
    val id: String?,
    val mediaType: String?, // "image" | "video"
    val url: String?,
    val thumb: String?,           // thumbnail url (videos often include "thumb")
)
