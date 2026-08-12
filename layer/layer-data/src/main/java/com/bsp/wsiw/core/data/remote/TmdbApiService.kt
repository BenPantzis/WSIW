package com.bsp.wsiw.core.data.remote

import com.bsp.wsiw.core.data.remote.model.GenreListResponseDto
import com.bsp.wsiw.core.data.remote.model.MovieDetailDto
import com.bsp.wsiw.core.data.remote.model.MovieListResponseDto
import com.bsp.wsiw.core.data.remote.model.MultiSearchResponseDto
import com.bsp.wsiw.core.data.remote.model.PersonDetailDto
import com.bsp.wsiw.core.data.remote.model.ReviewListResponseDto
import com.bsp.wsiw.core.data.remote.model.TvDetailDto
import com.bsp.wsiw.core.data.remote.model.TvListResponseDto
import com.bsp.wsiw.core.data.remote.model.WatchProvidersResponseDto
import com.bsp.wsiw.core.data.remote.model.RatingBody
import com.bsp.wsiw.core.data.remote.model.WatchlistUpdateBody
import com.bsp.wsiw.core.data.remote.model.WatchlistUpdateResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
    ): MovieListResponseDto

    @GET("trending/movie/week")
    suspend fun getTrendingMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
    ): MovieListResponseDto

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
    ): MovieListResponseDto

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
    ): MovieListResponseDto

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
    ): MovieListResponseDto

    @GET("genre/movie/list")
    suspend fun getGenres(
        @Query("language") language: String = "en-US",
    ): GenreListResponseDto

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("with_genres") genreId: Int?,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("vote_average.gte") minRating: Float? = null,
        @Query("primary_release_year") year: Int? = null,
        @Query("vote_count.gte") minVoteCount: Int? = null,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
    ): MovieListResponseDto

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
    ): MultiSearchResponseDto

    // --- TV ---

    @GET("trending/tv/week")
    suspend fun getTrendingTv(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
    ): TvListResponseDto

    @GET("tv/popular")
    suspend fun getPopularTv(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
    ): TvListResponseDto

    @GET("tv/top_rated")
    suspend fun getTopRatedTv(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
    ): TvListResponseDto

    @GET("tv/on_the_air")
    suspend fun getOnTheAirTv(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
    ): TvListResponseDto

    @GET("tv/airing_today")
    suspend fun getAiringTodayTv(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
    ): TvListResponseDto

    @GET("genre/tv/list")
    suspend fun getTvGenres(
        @Query("language") language: String = "en-US",
    ): GenreListResponseDto

    @GET("discover/tv")
    suspend fun discoverTv(
        @Query("with_genres") genreId: Int?,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("vote_average.gte") minRating: Float? = null,
        @Query("first_air_date_year") year: Int? = null,
        @Query("vote_count.gte") minVoteCount: Int? = null,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
    ): TvListResponseDto

    @GET("tv/{seriesId}")
    suspend fun getTvDetail(
        @Path("seriesId") seriesId: Int,
        @Query("language") language: String = "en-US",
        @Query("append_to_response") appendToResponse: String = "aggregate_credits,content_ratings,similar,recommendations,videos",
    ): TvDetailDto

    @GET("person/{personId}")
    suspend fun getPersonDetail(
        @Path("personId") personId: Int,
        @Query("language") language: String = "en-US",
        @Query("append_to_response") appendToResponse: String = "movie_credits",
    ): PersonDetailDto

    @GET("movie/{movieId}/reviews")
    suspend fun getMovieReviews(
        @Path("movieId") movieId: Int,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
    ): ReviewListResponseDto

    @GET("movie/{movieId}")
    suspend fun getMovieDetail(
        @Path("movieId") movieId: Int,
        @Query("language") language: String = "en-US",
        @Query("append_to_response") appendToResponse: String = "videos,credits,similar,recommendations,release_dates",
    ): MovieDetailDto

    @GET("movie/{movieId}/watch/providers")
    suspend fun getMovieWatchProviders(
        @Path("movieId") movieId: Int,
    ): WatchProvidersResponseDto

    @GET("account/{accountId}/watchlist/movies")
    suspend fun getWatchlistMovies(
        @Path("accountId") accountId: Int,
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "created_at.desc",
    ): MovieListResponseDto

    @POST("account/{accountId}/watchlist")
    suspend fun updateWatchlist(
        @Path("accountId") accountId: Int,
        @Body body: WatchlistUpdateBody,
    ): WatchlistUpdateResponseDto

    @GET("account/{accountId}/rated/movies")
    suspend fun getRatedMovies(
        @Path("accountId") accountId: Int,
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "created_at.desc",
    ): MovieListResponseDto

    @GET("account/{accountId}/favorite/movies")
    suspend fun getFavoriteMovies(
        @Path("accountId") accountId: Int,
        @Query("page") page: Int = 1,
    ): MovieListResponseDto

    @POST("movie/{movieId}/rating")
    suspend fun rateMovie(
        @Path("movieId") movieId: Int,
        @Body body: RatingBody,
    ): WatchlistUpdateResponseDto

    @DELETE("movie/{movieId}/rating")
    suspend fun deleteMovieRating(
        @Path("movieId") movieId: Int,
    ): WatchlistUpdateResponseDto
}
