package com.bsp.wsiw.feature.tv

import androidx.annotation.StringRes
import com.bsp.wsiw.core.domain.model.DiscoverFilter
import com.bsp.wsiw.core.domain.model.Genre
import com.bsp.wsiw.core.domain.model.SortBy
import com.bsp.wsiw.core.domain.model.TvShow
import com.bsp.wsiw.core.ui.UiText

enum class TvCategory(@param:StringRes val labelRes: Int, val apiKey: String, val defaultSortBy: SortBy) {
    Trending(R.string.tv_category_trending, "trending", SortBy.Popularity),
    Popular(R.string.tv_category_popular, "popular", SortBy.Popularity),
    TopRated(R.string.tv_category_top_rated, "top_rated", SortBy.Rating),
    OnTheAir(R.string.tv_category_on_the_air, "on_the_air", SortBy.Popularity),
    AiringToday(R.string.tv_category_airing_today, "airing_today", SortBy.Popularity),
    ByGenre(R.string.tv_category_by_genre, "by_genre", SortBy.Popularity),
}

sealed interface TvListAction {
    data object LoadShows : TvListAction
    data object Retry : TvListAction
    data object Refresh : TvListAction
    data object LoadNextPage : TvListAction
    data class SelectCategory(val category: TvCategory) : TvListAction
    data class SelectGenre(val genreId: Int) : TvListAction
    data object OpenFilterSheet : TvListAction
    data object DismissFilterSheet : TvListAction
    data class ApplyFilter(val filter: DiscoverFilter) : TvListAction
}

sealed interface TvListEvent {
    data class ShowSnackbar(val message: UiText) : TvListEvent
}

data class TvListUiState(
    val isLoading: Boolean = true,
    val shows: List<TvShow> = emptyList(),
    val error: UiText? = null,
    val isRefreshing: Boolean = false,
    val isPullRefreshing: Boolean = false,
    val selectedCategory: TvCategory = TvCategory.Trending,
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
