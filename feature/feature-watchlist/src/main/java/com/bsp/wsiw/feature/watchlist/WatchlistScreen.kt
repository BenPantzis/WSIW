package com.bsp.wsiw.feature.watchlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.ui.component.RemoteImage
import com.bsp.wsiw.core.ui.component.ScreenScaffold
import com.bsp.wsiw.core.ui.component.shimmerEffect
import com.bsp.wsiw.core.ui.theme.AppTheme
import kotlin.math.roundToInt

@Composable
fun WatchlistScreen(
    onMovieClick: (Int) -> Unit,
    onBrowseMovies: () -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is WatchlistEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
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
        when {
            uiState.isLoading -> WatchlistShimmerGrid(
                contentPadding = PaddingValues(
                    start = spacing.md, end = spacing.md,
                    top = padding.calculateTopPadding() + spacing.md,
                    bottom = padding.calculateBottomPadding() + spacing.md,
                ),
            )
            uiState.movies.isEmpty() -> WatchlistEmptyState(
                onBrowseMovies = onBrowseMovies,
                modifier = Modifier.padding(padding),
            )
            else -> WatchlistGrid(
                movies = uiState.movies,
                onMovieClick = onMovieClick,
                onRemove = { onAction(WatchlistAction.RemoveMovie(it)) },
                contentPadding = PaddingValues(
                    start = spacing.md, end = spacing.md,
                    top = padding.calculateTopPadding() + spacing.md,
                    bottom = padding.calculateBottomPadding() + spacing.md,
                ),
            )
        }
    }
}

// --- Grid ---

@Composable
private fun WatchlistGrid(
    movies: List<Movie>,
    onMovieClick: (Int) -> Unit,
    onRemove: (Int) -> Unit,
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
            Text(
                text = "Watchlist  •  ${movies.size}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = spacing.xs, vertical = spacing.xs),
            )
        }
        items(movies, key = { it.id }) { movie ->
            WatchlistPosterCard(
                movie = movie,
                onClick = { onMovieClick(movie.id) },
                onRemove = { onRemove(movie.id) },
            )
        }
    }
}

@Composable
private fun WatchlistPosterCard(
    movie: Movie,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Box(modifier = Modifier.aspectRatio(2f / 3f)) {
            RemoteImage(
                url = movie.posterUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.55f to Color.Transparent,
                                1.0f to Color(0xE6000000),
                            ),
                        ),
                    ),
            )
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
                        .background(Color(0x99000000), CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Remove from watchlist",
                        tint = Color(0xFFE8A020),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = AppTheme.spacing.sm, vertical = AppTheme.spacing.sm),
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "★ ${(movie.voteAverage * 10).roundToInt() / 10.0}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFE8A020),
                )
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
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
            modifier = Modifier.padding(horizontal = AppTheme.spacing.xxl),
        ) {
            Text(text = "🔖", style = MaterialTheme.typography.displayMedium)
            Text(
                text = "Your watchlist is empty",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Bookmark movies on their detail page to save them here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(AppTheme.spacing.sm))
            Button(onClick = onBrowseMovies) {
                Text("Browse movies")
            }
        }
    }
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
        items(6) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            ) {
                Box(
                    modifier = Modifier
                        .aspectRatio(2f / 3f)
                        .shimmerEffect(),
                )
            }
        }
    }
}
