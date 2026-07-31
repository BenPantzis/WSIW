package com.bsp.wsiw.core.database

import androidx.room.Room
import org.robolectric.RuntimeEnvironment

fun buildTestDatabase(): AppDatabase =
    Room.inMemoryDatabaseBuilder(
        RuntimeEnvironment.getApplication(),
        AppDatabase::class.java,
    )
        .allowMainThreadQueries()
        .build()
