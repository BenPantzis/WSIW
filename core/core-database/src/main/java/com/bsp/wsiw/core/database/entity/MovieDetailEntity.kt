package com.bsp.wsiw.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bsp.wsiw.core.database.model.CastMemberData
import com.bsp.wsiw.core.database.model.GenreData
import com.bsp.wsiw.core.database.model.MovieData

@Entity(tableName = "movie_detail_cache")
data class MovieDetailEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int,
    val tagline: String,
    val genres: List<GenreData>,
    val runtime: Int,
    val originalLanguage: String,
    val cachedAt: Long,
    val trailerKey: String? = null,
    val trailerName: String? = null,
    val cast: List<CastMemberData> = emptyList(),
    val similarMovies: List<MovieData> = emptyList(),
    val recommendedMovies: List<MovieData> = emptyList(),
    val certification: String? = null,
)
