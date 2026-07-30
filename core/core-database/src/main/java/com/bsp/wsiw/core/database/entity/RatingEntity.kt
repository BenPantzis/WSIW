package com.bsp.wsiw.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ratings")
data class RatingEntity(
    @PrimaryKey val movieId: Int,
    val rating: Float,
    val ratedAt: Long,
)
