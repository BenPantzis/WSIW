package com.bsp.wsiw.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bsp.wsiw.core.domain.model.Genre
import com.bsp.wsiw.core.domain.model.MovieDetail

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
    val genres: List<Genre>, // stored via Converters.genresToString / stringToGenres
    val runtime: Int,
    val originalLanguage: String,
    val cachedAt: Long,
)

fun MovieDetailEntity.toDomain() = MovieDetail(
    id = id,
    title = title,
    overview = overview,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
    tagline = tagline,
    genres = genres,
    runtime = runtime,
    originalLanguage = originalLanguage,
)
