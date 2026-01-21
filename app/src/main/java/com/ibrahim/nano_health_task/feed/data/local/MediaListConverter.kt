package com.ibrahim.nano_health_task.feed.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MediaListConverter {
    private val gson = Gson()
    private val type = object : TypeToken<List<MediaEntity>>() {}.type

    @TypeConverter
    fun fromList(list: List<MediaEntity>): String = gson.toJson(list)

    @TypeConverter
    fun toList(json: String): List<MediaEntity> = gson.fromJson(json, type) ?: emptyList()
}
