package com.bsp.wsiw.feature.watchlist

import androidx.compose.foundation.background
import com.bsp.wsiw.core.ui.theme.GoldDefault
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.ui.component.EmptyState
import com.bsp.wsiw.core.ui.component.MoviePosterCard
import com.bsp.wsiw.core.ui.component.RemoteImage
import com.bsp.wsiw.core.ui.component.ScreenScaffold
import com.bsp.wsiw.core.ui.component.ShimmerPosterCard
import com.bsp.wsiw.core.ui.theme.AppTheme
import kotlin.math.roundToInt

private val ButtonScrim = Color(0x99000000)  // semi-transparent scrim behind icon buttons
private val BadgeScrim = Color(0xCC000000)   // slightly more opaque scrim for rating badges

@Composable
fun WatchlistScreen(
    onMovieClick: (Int) -> Unit,
    onBrowseMovies: () -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is WatchlistEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message.resolve(context),
                        actionLabel = if (event.undoMovie != null) context.getString(R.string.watchlist_action_undo) else null,
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed && event.undoMovie != null) {
                        viewModel.onAction(WatchlistAction.UndoRemove(event.undoMovie))
                    }
                }
            }
        }
    }

    WatchlistContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onMovieClick = onMovieClick,
        onBrowseMovies = onBrowseMovies,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
internal fun WatchlistContent(
    uiState: WatchlistUiState,
    onAction: (WatchlistAction) -> Unit,
    onMovieClick: (Int) -> Unit,
    onBrowseMovies: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val spacing = AppTheme.spacing
    ScreenScaffold(snackbarHostState = snackbarHostState) { padding, _ ->
        val contentPadding = PaddingValues(
            start = spacing.md, end = spacing.md,
            top = padding.calculateTopPadding() + spacing.md,
            bottom = padding.calculateBottomPadding() + spacing.md,
        )
        when {
            uiState.isLoading -> WatchlistShimmerGrid(contentPadding = contentPadding)
            uiState.movies.isEmpty() -> WatchlistEmptyState(
                onBrowseMovies = onBrowseMovies,
                modifier = Modifier.padding(padding),
            )
            uiState.viewMode == WatchlistViewMode.Grid -> WatchlistGrid(
                uiState = uiState,
                onMovieClick = onMovieClick,
                onAction = onAction,
                contentPadding = contentPadding,
            )
            else -> WatchlistList(
                uiState = uiState,
                onMovieClick = onMovieClick,
                onAction = onAction,
                contentPadding = contentPadding,
            )
        }
    }
}

// --- Shared header ---

@Composable
private fun SortAndToggleHeader(
    count: Int,
    sort: WatchlistSort,
    viewMode: WatchlistViewMode,
    onAction: (WatchlistAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = AppTheme.spacing
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.watchlist_title_with_count, count),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { onAction(WatchlistAction.ToggleViewMode) }) {
                Icon(
                    imageVector = if (viewMode == WatchlistViewMode.Grid) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                    contentDescription = stringResource(
                        if (viewMode == WatchlistViewMode.Grid) R.string.watchlist_cd_toggle_list
                        else R.string.watchlist_cd_toggle_grid,
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            modifier = Modifier.padding(start = spacing.xs),
        ) {
            WatchlistSort.entries.forEach { option ->
                FilterChip(
                    selected = sort == option,
                    onClick = { onAction(WatchlistAction.SelectSort(option)) },
                    label = { Text(stringResource(option.labelRes)) },
                )
            }
        }
        Spacer(Modifier.height(spacing.xs))
    }
}

// --- Grid ---

@Composable
private fun WatchlistGrid(
    uiState: WatchlistUiState,
    onMovieClick: (Int) -> Unit,
    onAction: (WatchlistAction) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val spacing = AppTheme.spacing
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
        modifier = modifier.fillMaxSize(),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            SortAndToggleHeader(
                count = uiState.movies.size,
                sort = uiState.sort,
                viewMode = uiState.viewMode,
                onAction = onAction,
            )
        }
        items(uiState.sortedMovies, key = { it.id }) { movie ->
            WatchlistPosterCard(
                movie = movie,
                userRating = uiState.ratings[movie.id],
                onClick = { onMovieClick(movie.id) },
                onRemove = { onAction(WatchlistAction.RemoveMovie(movie.id)) },
            )
        }
    }
}

@Composable
private fun WatchlistPosterCard(
    movie: Movie,
    userRating: Float?,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    MoviePosterCard(
        posterUrl = movie.posterUrl,
        title = movie.title,
        voteAverage = movie.voteAverage,
        onClick = onClick,
        overlayContent = {
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(AppTheme.spacing.xs)
                    .size(36.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .background(ButtonScrim, CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = stringResource(R.string.watchlist_cd_remove),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            if (userRating != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(AppTheme.spacing.xs)
                        .background(BadgeScrim, RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = GoldDefault,
                        modifier = Modifier.size(10.dp),
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = (userRating / 2f).roundToInt().toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                    )
                }
            }
        },
    )
}

// --- List ---

@Composable
private fun WatchlistList(
    uiState: WatchlistUiState,
    onMovieClick: (Int) -> Unit,
    onAction: (WatchlistAction) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
        modifier = modifier.fillMaxSize(),
    ) {
        item {
            SortAndToggleHeader(
                count = uiState.movies.size,
                sort = uiState.sort,
                viewMode = uiState.viewMode,
                onAction = onAction,
            )
        }
        items(uiState.sortedMovies, key = { it.id }) { movie ->
            WatchlistListItem(
                movie = movie,
                userRating = uiState.ratings[movie.id],
                onClick = { onMovieClick(movie.id) },
                onRemove = { onAction(WatchlistAction.RemoveMovie(movie.id)) },
            )
        }
    }
}

@Composable
private fun WatchlistListItem(
    movie: Movie,
    userRating: Float?,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { it * 0.35f },
    )
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onRemove()
        }
    }
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.errorContainer),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(end = AppTheme.spacing.lg),
                )
            }
        },
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Row(
                modifier = Modifier.padding(AppTheme.spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
            ) {
                RemoteImage(
                    url = movie.posterUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(72.dp)
                        .aspectRatio(2f / 3f)
                        .clip(MaterialTheme.shapes.small),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
                ) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val year = movie.releaseDate.take(4).takeIf { it.length == 4 && it.all(Char::isDigit) }
                    if (year != null) {
                        Text(
                            text = year,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (movie.overview.isNotBlank()) {
                        Text(
                            text = movie.overview,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "★ ${(movie.voteAverage * 10).roundToInt() / 10.0}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        if (userRating != null) {
                            Text(
                                text = stringResource(R.string.watchlist_your_rating, "★".repeat((userRating / 2f).roundToInt())),
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldDefault,
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Empty state ---

@Composable
private fun WatchlistEmptyState(
    onBrowseMovies: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        icon = "🔖",
        title = stringResource(R.string.watchlist_empty_title),
        body = stringResource(R.string.watchlist_empty_body),
        modifier = modifier,
        action = { Button(onClick = onBrowseMovies) { Text(stringResource(R.string.watchlist_browse_button)) } },
    )
}

// --- Shimmer ---

@Composable
private fun WatchlistShimmerGrid(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val spacing = AppTheme.spacing
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
        userScrollEnabled = false,
        modifier = modifier.fillMaxSize(),
    ) {
        items(6) { ShimmerPosterCard() }
    }
}
