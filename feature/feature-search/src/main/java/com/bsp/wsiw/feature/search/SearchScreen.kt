package com.bsp.wsiw.feature.search

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
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
fun SearchScreen(
    onMovieClick: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SearchEvent.ShowError -> Unit // future: show snackbar
            }
        }
    }

    SearchContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onMovieClick = onMovieClick,
    )
}

@Composable
internal fun SearchContent(
    uiState: SearchUiState,
    onAction: (SearchAction) -> Unit,
    onMovieClick: (Int) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val spacing = AppTheme.spacing
    ScreenScaffold { padding, _ ->
        Column(modifier = Modifier.fillMaxSize()) {
            SearchTextField(
                query = uiState.query,
                onQueryChange = { onAction(SearchAction.UpdateQuery(it)) },
                onClear = { onAction(SearchAction.ClearQuery) },
                focusRequester = focusRequester,
                modifier = Modifier.padding(
                    top = padding.calculateTopPadding() + spacing.sm,
                    start = spacing.lg,
                    end = spacing.lg,
                    bottom = spacing.sm,
                ),
            )

            val gridPadding = PaddingValues(
                start = spacing.md, end = spacing.md,
                bottom = padding.calculateBottomPadding() + spacing.md,
            )
            when {
                uiState.query.isBlank() -> TrendingIdleState(
                    trendingMovies = uiState.trendingMovies,
                    isTrendingLoading = uiState.isTrendingLoading,
                    onMovieClick = onMovieClick,
                    contentPadding = gridPadding,
                )
                uiState.isLoading -> SearchShimmerGrid(contentPadding = gridPadding)
                uiState.movies.isEmpty() -> NoResultsState(query = uiState.query)
                else -> SearchResultsGrid(
                    movies = uiState.movies,
                    onMovieClick = onMovieClick,
                    contentPadding = gridPadding,
                )
            }
        }
    }
}

// --- Search bar ---

@Composable
private fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        placeholder = {
            Text(
                text = stringResource(R.string.search_placeholder),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.search_cd_clear),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
        textStyle = MaterialTheme.typography.bodyLarge,
    )
}

// --- State composables ---

@Composable
private fun TrendingIdleState(
    trendingMovies: List<Movie>,
    isTrendingLoading: Boolean,
    onMovieClick: (Int) -> Unit,
    contentPadding: PaddingValues,
) {
    val spacing = AppTheme.spacing
    when {
        isTrendingLoading -> SearchShimmerGrid(
            contentPadding = contentPadding,
            headerSlot = { TrendingHeader() },
        )
        trendingMovies.isNotEmpty() -> LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) { TrendingHeader() }
            items(trendingMovies, key = { it.id }) { movie ->
                SearchPosterCard(movie = movie, onClick = { onMovieClick(movie.id) })
            }
        }
        else -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                Text(text = "🎬", style = MaterialTheme.typography.displayMedium)
                Text(
                    text = stringResource(R.string.search_idle_headline),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.search_idle_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun TrendingHeader() {
    val spacing = AppTheme.spacing
    Column {
        Spacer(Modifier.height(spacing.sm))
        Text(
            text = stringResource(R.string.search_trending_header),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = spacing.xs, end = spacing.xs, bottom = spacing.xs),
        )
    }
}

@Composable
private fun NoResultsState(query: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
            modifier = Modifier.padding(horizontal = AppTheme.spacing.xxl),
        ) {
            Text(text = "🔍", style = MaterialTheme.typography.displayMedium)
            Text(
                text = stringResource(R.string.search_no_results_title, query),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.search_no_results_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// --- Results grid ---

@Composable
private fun SearchResultsGrid(
    movies: List<Movie>,
    onMovieClick: (Int) -> Unit,
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
        items(movies, key = { it.id }) { movie ->
            SearchPosterCard(movie = movie, onClick = { onMovieClick(movie.id) })
        }
    }
}

@Composable
private fun SearchPosterCard(movie: Movie, onClick: () -> Unit) {
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

// --- Shimmer skeleton ---

@Composable
private fun SearchShimmerGrid(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    headerSlot: (@Composable () -> Unit)? = null,
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
        if (headerSlot != null) {
            item(span = { GridItemSpan(maxLineSpan) }) { headerSlot() }
        }
        items(12) {
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
