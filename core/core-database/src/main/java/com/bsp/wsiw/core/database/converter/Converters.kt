package com.bsp.wsiw.core.database.converter

import androidx.room.TypeConverter
import com.bsp.wsiw.core.database.model.CastMemberData
import com.bsp.wsiw.core.database.model.GenreData
import com.bsp.wsiw.core.database.model.MovieData
import org.json.JSONArray
import org.json.JSONObject

class Converters {

    @TypeConverter
    fun genresToString(genres: List<GenreData>): String =
        genres.joinToString("|") { "${it.id}:${it.name}" }

    @TypeConverter
    fun stringToGenres(value: String): List<GenreData> =
        if (value.isEmpty()) emptyList()
        else value.split("|").map {
            val parts = it.split(":", limit = 2)
            GenreData(id = parts[0].toInt(), name = parts[1])
        }

    @TypeConverter
    fun castToJson(cast: List<CastMemberData>): String {
        val array = JSONArray()
        for (m in cast) {
            array.put(JSONObject().apply {
                put("id", m.id)
                put("name", m.name)
                put("character", m.character)
                put("profileUrl", m.profileUrl ?: JSONObject.NULL)
            })
        }
        return array.toString()
    }

    @TypeConverter
    fun jsonToCast(json: String): List<CastMemberData> {
        if (json.isEmpty() || json == "[]") return emptyList()
        val array = JSONArray(json)
        return (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            CastMemberData(
                id = o.getInt("id"),
                name = o.getString("name"),
                character = o.getString("character"),
                profileUrl = o.optString("profileUrl", "").takeIf { it.isNotEmpty() },
            )
        }
    }

    @TypeConverter
    fun moviesToJson(movies: List<MovieData>): String {
        val array = JSONArray()
        for (m in movies) {
            array.put(JSONObject().apply {
                put("id", m.id)
                put("title", m.title)
                put("overview", m.overview)
                put("posterUrl", m.posterUrl ?: JSONObject.NULL)
                put("backdropUrl", m.backdropUrl ?: JSONObject.NULL)
                put("releaseDate", m.releaseDate)
                put("voteAverage", m.voteAverage)
                put("voteCount", m.voteCount)
            })
        }
        return array.toString()
    }

    @TypeConverter
    fun jsonToMovies(json: String): List<MovieData> {
        if (json.isEmpty() || json == "[]") return emptyList()
        val array = JSONArray(json)
        return (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            MovieData(
                id = o.getInt("id"),
                title = o.getString("title"),
                overview = o.getString("overview"),
                posterUrl = o.optString("posterUrl", "").takeIf { it.isNotEmpty() },
                backdropUrl = o.optString("backdropUrl", "").takeIf { it.isNotEmpty() },
                releaseDate = o.getString("releaseDate"),
                voteAverage = o.getDouble("voteAverage"),
                voteCount = o.getInt("voteCount"),
            )
        }
    }
}
