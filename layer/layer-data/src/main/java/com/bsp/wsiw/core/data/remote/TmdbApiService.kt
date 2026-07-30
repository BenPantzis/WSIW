package com.bsp.wsiw.core.data.remote

import com.bsp.wsiw.core.data.remote.model.GenreListResponseDto
import com.bsp.wsiw.core.data.remote.model.MovieDetailDto
import com.bsp.wsiw.core.data.remote.model.MovieListResponseDto
import com.bsp.wsiw.core.data.remote.model.PersonDetailDto
import com.bsp.wsiw.core.data.remote.model.ReviewListResponseDto
import com.bsp.wsiw.core.data.remote.model.WatchProvidersResponseDto
import com.bsp.wsiw.core.data.remote.model.WatchlistUpdateBody
import com.bsp.wsiw.core.data.remote.model.WatchlistUpdateResponseDto
import retrofit2.http.Body
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
        @Query("with_genres") genreId: Int,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
    ): MovieListResponseDto

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
    ): MovieListResponseDto

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
}
