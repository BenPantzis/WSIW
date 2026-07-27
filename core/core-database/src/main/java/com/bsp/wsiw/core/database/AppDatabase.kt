package com.bsp.wsiw.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bsp.wsiw.core.database.entity.PlaceholderEntity

@Database(
    entities = [PlaceholderEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase()
