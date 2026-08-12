package com.bsp.wsiw.feature.tv

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bsp.wsiw.core.domain.model.DiscoverFilter
import com.bsp.wsiw.core.domain.model.Genre
import com.bsp.wsiw.core.domain.model.SortBy
import com.bsp.wsiw.core.domain.model.TvShow
import com.bsp.wsiw.core.ui.component.ErrorContent
import com.bsp.wsiw.core.ui.component.FilterBottomSheet
import com.bsp.wsiw.core.ui.component.FilterSheetState
import com.bsp.wsiw.core.ui.component.MoviePosterCard
import com.bsp.wsiw.core.ui.component.ScreenScaffold
import com.bsp.wsiw.core.ui.component.ShimmerPosterCard
import com.bsp.wsiw.core.ui.component.SortOption
import com.bsp.wsiw.core.ui.theme.AppTheme
import com.bsp.wsiw.core.ui.R as CoreUiR

@Composable
fun TvListScreen(
    onShowClick: (Int) -> Unit,
    viewModel: TvListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TvListEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message.resolve(context))
            }
        }
    }

    TvListContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onShowClick = onShowClick,
        snackbarHostState = snackbarHostState,
    )

    if (uiState.showFilterSheet) {
        val sortOptions = SortBy.entries.map { it.toSortOption() }
        FilterBottomSheet(
            state = uiState.filter.toFilterSheetState(sortOptions),
            onApply = { viewModel.onAction(TvListAction.ApplyFilter(it.toDiscoverFilter())) },
            onReset = { viewModel.onAction(TvListAction.ApplyFilter(DiscoverFilter())) },
            onDismiss = { viewModel.onAction(TvListAction.DismissFilterSheet) },
        )
    }
}

@Composable
internal fun TvListContent(
    uiState: TvListUiState,
    onAction: (TvListAction) -> Unit,
    onShowClick: (Int) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    ScreenScaffold(snackbarHostState = snackbarHostState) { padding, _ ->
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = padding.calculateTopPadding()),
            ) {
                CategoryChipRow(
                    selected = uiState.selectedCategory,
                    onSelect = { onAction(TvListAction.SelectCategory(it)) },
                    modifier = Modifier.weight(1f),
                )
                val activeCount = uiState.filter.activeCount
                @OptIn(ExperimentalMaterial3Api::class)
                BadgedBox(
                    badge = {
                        if (activeCount > 0) Badge { Text(activeCount.toString()) }
                    },
                    modifier = Modifier.padding(end = AppTheme.spacing.sm),
                ) {
                    IconButton(onClick = { onAction(TvListAction.OpenFilterSheet) }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = if (activeCount > 0) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = uiState.selectedCategory == TvCategory.ByGenre && uiState.genres.isNotEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                GenreChipRow(
                    genres = uiState.genres,
                    selectedGenreId = uiState.selectedGenreId,
                    onSelect = { onAction(TvListAction.SelectGenre(it)) },
                )
            }
            when {
                uiState.isLoading -> ShimmerGrid(PaddingValues(bottom = padding.calculateBottomPadding()))
                uiState.error != null -> ErrorContent(
                    message = uiState.error!!.asString(),
                    onRetry = { onAction(TvListAction.Retry) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = padding.calculateBottomPadding()),
                )
                else -> @OptIn(ExperimentalMaterial3Api::class) PullToRefreshBox(
                    isRefreshing = uiState.isPullRefreshing,
                    onRefresh = { onAction(TvListAction.Refresh) },
                    modifier = Modifier.weight(1f),
                ) {
                    TvGrid(
                        shows = uiState.shows,
                        isLoadingMore = uiState.isLoadingMore,
                        canLoadMore = uiState.canLoadMore,
                        onShowClick = onShowClick,
                        onLoadMore = { onAction(TvListAction.LoadNextPage) },
                        bottomPadding = padding.calculateBottomPadding(),
                    )
                    if (uiState.isRefreshing) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color.Transparent,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChipRow(
    selected: TvCategory,
    onSelect: (TvCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = AppTheme.spacing
    LazyRow(
        contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        modifier = modifier,
    ) {
        items(TvCategory.entries) { category ->
            FilterChip(
                selected = category == selected,
                onClick = { onSelect(category) },
                label = { Text(stringResource(category.labelRes)) },
            )
        }
    }
}

@Composable
private fun GenreChipRow(
    genres: List<Genre>,
    selectedGenreId: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = AppTheme.spacing
    LazyRow(
        contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        modifier = modifier,
    ) {
        items(genres, key = { it.id }) { genre ->
            FilterChip(
                selected = genre.id == selectedGenreId,
                onClick = { onSelect(genre.id) },
                label = { Text(genre.name) },
            )
        }
    }
}

@Composable
private fun ShimmerGrid(contentPadding: PaddingValues, modifier: Modifier = Modifier) {
    val spacing = AppTheme.spacing
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(
            start = spacing.md, end = spacing.md, top = spacing.md,
            bottom = contentPadding.calculateBottomPadding() + spacing.md,
        ),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
        userScrollEnabled = false,
        modifier = modifier.fillMaxSize(),
    ) {
        items(12) { ShimmerPosterCard() }
    }
}

@Composable
private fun TvGrid(
    shows: List<TvShow>,
    isLoadingMore: Boolean,
    canLoadMore: Boolean,
    onShowClick: (Int) -> Unit,
    onLoadMore: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val spacing = AppTheme.spacing
    val gridState = rememberLazyGridState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = gridState.layoutInfo.totalItemsCount
            canLoadMore && total > 0 && lastVisible >= total - 4
        }
    }
    LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) onLoadMore() }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(
            start = spacing.md, end = spacing.md, top = spacing.md,
            bottom = bottomPadding + spacing.md,
        ),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
        modifier = modifier.fillMaxSize(),
    ) {
        items(shows, key = { it.id }) { show ->
            MoviePosterCard(
                posterUrl = show.posterUrl,
                title = show.name,
                voteAverage = show.voteAverage,
                onClick = { onShowClick(show.id) },
            )
        }
        if (isLoadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.padding(4.dp))
                }
            }
        }
    }
}

@Composable
private fun SortBy.toSortOption() = SortOption(
    label = stringResource(labelRes),
    apiValue = apiValue,
)

private val SortBy.labelRes: Int
    get() = when (this) {
        SortBy.Popularity -> CoreUiR.string.filter_sort_popularity
        SortBy.Rating -> CoreUiR.string.filter_sort_rating
        SortBy.ReleaseDate -> CoreUiR.string.filter_sort_release_date
        SortBy.VoteCount -> CoreUiR.string.filter_sort_vote_count
    }

private fun DiscoverFilter.toFilterSheetState(sortOptions: List<SortOption>) = FilterSheetState(
    sortOptions = sortOptions,
    selectedSortOption = sortOptions.first { it.apiValue == sortBy.apiValue },
    minRating = minRating,
    year = year,
)

private fun FilterSheetState.toDiscoverFilter() = DiscoverFilter(
    sortBy = SortBy.entries.first { it.apiValue == selectedSortOption.apiValue },
    minRating = minRating,
    year = year,
)
