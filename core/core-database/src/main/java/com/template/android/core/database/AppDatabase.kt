package com.template.android.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.template.android.core.database.entity.PlaceholderEntity

@Database(
    entities = [PlaceholderEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase()
