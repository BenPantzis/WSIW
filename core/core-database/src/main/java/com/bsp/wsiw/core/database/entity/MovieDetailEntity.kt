package com.bsp.wsiw.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bsp.wsiw.core.domain.model.CastMember
import com.bsp.wsiw.core.domain.model.Genre
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.model.MovieDetail
import com.bsp.wsiw.core.domain.model.VideoEntry

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
    val genres: List<Genre>,
    val runtime: Int,
    val originalLanguage: String,
    val cachedAt: Long,
    val trailerKey: String? = null,
    val trailerName: String? = null,
    val cast: List<CastMember> = emptyList(),
    val similarMovies: List<Movie> = emptyList(),
    val recommendedMovies: List<Movie> = emptyList(),
    val certification: String? = null,
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
    trailer = if (trailerKey != null && trailerName != null) VideoEntry(trailerKey, trailerName) else null,
    cast = cast,
    similarMovies = similarMovies,
    recommendedMovies = recommendedMovies,
    certification = certification,
)
