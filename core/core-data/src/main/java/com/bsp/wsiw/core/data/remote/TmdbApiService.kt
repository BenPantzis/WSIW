package com.bsp.wsiw.core.data.remote

import com.bsp.wsiw.core.data.remote.model.GenreListResponseDto
import com.bsp.wsiw.core.data.remote.model.MovieDetailDto
import com.bsp.wsiw.core.data.remote.model.MovieListResponseDto
import com.bsp.wsiw.core.data.remote.model.PersonDetailDto
import com.bsp.wsiw.core.data.remote.model.ReviewListResponseDto
import retrofit2.http.GET
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
        @Query("append_to_response") appendToResponse: String = "videos,credits,similar",
    ): MovieDetailDto
}
