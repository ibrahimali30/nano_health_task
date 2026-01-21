package com.ibrahim.nano_health_task.feed.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [PostEntity::class], version = 1, exportSchema = false)
@TypeConverters(MediaListConverter::class)
abstract class FeedDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao
}

