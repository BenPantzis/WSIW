package com.bsp.wsiw.feature.home

import androidx.annotation.StringRes
import com.bsp.wsiw.core.domain.model.DiscoverFilter
import com.bsp.wsiw.core.domain.model.Genre
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.model.SortBy
import com.bsp.wsiw.core.ui.UiText

enum class HomeCategory(@param:StringRes val labelRes: Int, val defaultSortBy: SortBy) {
    Popular(R.string.home_category_popular, SortBy.Popularity),
    Trending(R.string.home_category_trending, SortBy.Popularity),
    TopRated(R.string.home_category_top_rated, SortBy.Rating),
    NowPlaying(R.string.home_category_now_playing, SortBy.Popularity),
    Upcoming(R.string.home_category_upcoming, SortBy.ReleaseDate),
    ByGenre(R.string.home_category_by_genre, SortBy.Popularity),
}

sealed interface HomeAction {
    data object LoadMovies : HomeAction
    data object Retry : HomeAction
    data object Refresh : HomeAction
    data object LoadNextPage : HomeAction
    data class SelectCategory(val category: HomeCategory) : HomeAction
    data class SelectGenre(val genreId: Int) : HomeAction
    data object OpenFilterSheet : HomeAction
    data object DismissFilterSheet : HomeAction
    data class ApplyFilter(val filter: DiscoverFilter) : HomeAction
}

sealed interface HomeEvent {
    data class ShowSnackbar(val message: UiText) : HomeEvent
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val movies: List<Movie> = emptyList(),
    val error: UiText? = null,
    val isRefreshing: Boolean = false,
    val isPullRefreshing: Boolean = false,
    val selectedCategory: HomeCategory = HomeCategory.Popular,
    val genres: List<Genre> = emptyList(),
    val selectedGenreId: Int? = null,
    val isGenresLoading: Boolean = false,
    val currentPage: Int = 1,
    val totalPages: Int = Int.MAX_VALUE,
    val isLoadingMore: Boolean = false,
    val filter: DiscoverFilter = DiscoverFilter(),
    val showFilterSheet: Boolean = false,
) {
    val canLoadMore get() = !isLoading && !isLoadingMore && currentPage < totalPages
}
