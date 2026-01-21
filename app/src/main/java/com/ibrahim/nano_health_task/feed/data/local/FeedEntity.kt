package com.ibrahim.nano_health_task.feed.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ibrahim.nano_health_task.feed.data.local.MediaListConverter

@Entity(tableName = "posts")
@TypeConverters(MediaListConverter::class)
data class PostEntity(
    @PrimaryKey val id: String,
    val author: String,
    val caption: String,
    val media: List<MediaEntity>
)

data class MediaEntity(
    val id: String,
    val type: String,
    val url: String?,
    val thumbnailUrl: String?
)

