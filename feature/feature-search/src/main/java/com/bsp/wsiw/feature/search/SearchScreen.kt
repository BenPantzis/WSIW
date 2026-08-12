package com.bsp.wsiw.feature.search

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.model.PersonSummary
import com.bsp.wsiw.core.domain.model.SearchResult
import com.bsp.wsiw.core.domain.model.TvShow
import com.bsp.wsiw.core.ui.component.EmptyState
import com.bsp.wsiw.core.ui.component.MoviePosterCard
import com.bsp.wsiw.core.ui.component.ScreenScaffold
import com.bsp.wsiw.core.ui.component.ShimmerPosterCard
import com.bsp.wsiw.core.ui.theme.AppTheme

@Composable
fun SearchScreen(
    onMovieClick: (Int) -> Unit,
    onPersonClick: (Int) -> Unit,
    onTvShowClick: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SearchEvent.ShowError -> Unit
            }
        }
    }

    SearchContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onMovieClick = onMovieClick,
        onPersonClick = onPersonClick,
        onTvShowClick = onTvShowClick,
    )
}

@Composable
internal fun SearchContent(
    uiState: SearchUiState,
    onAction: (SearchAction) -> Unit,
    onMovieClick: (Int) -> Unit,
    onPersonClick: (Int) -> Unit,
    onTvShowClick: (Int) -> Unit,
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
                uiState.results.isEmpty() -> NoResultsState(query = uiState.query)
                else -> MixedResultsList(
                    results = uiState.results,
                    onMovieClick = onMovieClick,
                    onPersonClick = onPersonClick,
                    onTvShowClick = onTvShowClick,
                    contentPadding = gridPadding,
                )
            }
        }
    }
}

// --- Mixed results ---

@Composable
private fun MixedResultsList(
    results: List<SearchResult>,
    onMovieClick: (Int) -> Unit,
    onPersonClick: (Int) -> Unit,
    onTvShowClick: (Int) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val spacing = AppTheme.spacing
    val people = results.filterIsInstance<SearchResult.PersonResult>()
    val mediaItems = results.filter { it is SearchResult.MovieResult || it is SearchResult.TvResult }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
        modifier = modifier.fillMaxSize(),
    ) {
        if (people.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                PeopleSection(
                    people = people.map { it.person },
                    onPersonClick = onPersonClick,
                )
            }
        }

        if (mediaItems.isNotEmpty() && people.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(R.string.search_section_movies_tv),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = spacing.xs, vertical = spacing.xs),
                )
            }
        }

        items(mediaItems, key = { result ->
            when (result) {
                is SearchResult.MovieResult -> "movie_${result.movie.id}"
                is SearchResult.TvResult -> "tv_${result.show.id}"
                else -> result.hashCode()
            }
        }) { result ->
            when (result) {
                is SearchResult.MovieResult -> MoviePosterCard(
                    posterUrl = result.movie.posterUrl,
                    title = result.movie.title,
                    voteAverage = result.movie.voteAverage,
                    onClick = { onMovieClick(result.movie.id) },
                    modifier = Modifier.animateItem(fadeInSpec = tween(300), fadeOutSpec = tween(200)),
                )
                is SearchResult.TvResult -> MoviePosterCard(
                    posterUrl = result.show.posterUrl,
                    title = result.show.name,
                    voteAverage = result.show.voteAverage,
                    onClick = { onTvShowClick(result.show.id) },
                    modifier = Modifier.animateItem(fadeInSpec = tween(300), fadeOutSpec = tween(200)),
                )
                else -> Unit
            }
        }
    }
}

@Composable
private fun PeopleSection(
    people: List<PersonSummary>,
    onPersonClick: (Int) -> Unit,
) {
    val spacing = AppTheme.spacing
    Column {
        Text(
            text = stringResource(R.string.search_section_people),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = spacing.xs, vertical = spacing.xs),
        )
        LazyColumn(modifier = Modifier.height((people.size * 64).coerceAtMost(192).dp)) {
            items(people, key = { it.id }) { person ->
                PersonRow(
                    person = person,
                    onClick = { onPersonClick(person.id) },
                    modifier = Modifier.animateItem(fadeInSpec = tween(300), fadeOutSpec = tween(200)),
                )
            }
        }
    }
}

@Composable
private fun PersonRow(
    person: PersonSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = AppTheme.spacing
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = spacing.sm),
        ) {
            if (person.profileUrl != null) {
                AsyncImage(
                    model = person.profileUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                )
            } else {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(spacing.sm),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(spacing.md))
            Column {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                val dept = person.knownForDepartment
                if (dept != null) {
                    Text(
                        text = dept,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
                MoviePosterCard(
                    posterUrl = movie.posterUrl,
                    title = movie.title,
                    voteAverage = movie.voteAverage,
                    onClick = { onMovieClick(movie.id) },
                    modifier = Modifier.animateItem(fadeInSpec = tween(300), fadeOutSpec = tween(200)),
                )
            }
        }
        else -> EmptyState(
            icon = "🎬",
            title = stringResource(R.string.search_idle_headline),
            body = stringResource(R.string.search_idle_body),
        )
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
    EmptyState(
        icon = "🔍",
        title = stringResource(R.string.search_no_results_title, query),
        body = stringResource(R.string.search_no_results_body),
    )
}

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
        items(12) { ShimmerPosterCard() }
    }
}
