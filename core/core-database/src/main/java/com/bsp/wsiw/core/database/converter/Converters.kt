package com.bsp.wsiw.core.database.converter

import androidx.room.TypeConverter
import com.bsp.wsiw.core.domain.model.Genre

class Converters {

    @TypeConverter
    fun genresToString(genres: List<Genre>): String =
        genres.joinToString("|") { "${it.id}:${it.name}" }

    @TypeConverter
    fun stringToGenres(value: String): List<Genre> =
        if (value.isEmpty()) emptyList()
        else value.split("|").map {
            val parts = it.split(":", limit = 2)
            Genre(id = parts[0].toInt(), name = parts[1])
        }
}
