package com.bsp.wsiw.core.data.remote.model

import com.google.gson.annotations.SerializedName

data class TvListResponseDto(
    val results: List<TvShowDto>,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int,
)

data class TvShowDto(
    val id: Int,
    val name: String,
    val overview: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("genre_ids") val genreIds: List<Int> = emptyList(),
)

data class TvDetailDto(
    val id: Int,
    val name: String,
    val overview: String,
    val tagline: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("vote_count") val voteCount: Int,
    val status: String,
    val genres: List<GenreDto>,
    val seasons: List<SeasonDto>,
    val networks: List<NetworkDto>,
    @SerializedName("aggregate_credits") val aggregateCredits: TvCreditsDto?,
    @SerializedName("content_ratings") val contentRatings: TvContentRatingsDto?,
    val similar: TvListResponseDto?,
    val recommendations: TvListResponseDto?,
    val videos: TvVideosDto?,
)

data class SeasonDto(
    @SerializedName("season_number") val seasonNumber: Int,
    val name: String,
    @SerializedName("episode_count") val episodeCount: Int,
    @SerializedName("air_date") val airDate: String?,
    @SerializedName("poster_path") val posterPath: String?,
    val overview: String?,
)

data class NetworkDto(
    val id: Int,
    val name: String,
)

data class TvCreditsDto(
    val cast: List<TvCastMemberDto>,
)

data class TvCastMemberDto(
    val id: Int,
    val name: String,
    @SerializedName("profile_path") val profilePath: String?,
    val roles: List<TvRoleDto>,
)

data class TvRoleDto(
    val character: String,
)

data class TvContentRatingsDto(
    val results: List<TvContentRatingItemDto>,
)

data class TvContentRatingItemDto(
    @SerializedName("iso_3166_1") val country: String,
    val rating: String,
)

data class TvVideosDto(
    val results: List<TvVideoItemDto>,
)

data class TvVideoItemDto(
    val key: String,
    val site: String,
    val type: String,
    val official: Boolean,
)
