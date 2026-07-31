package com.bsp.wsiw.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bsp.wsiw.core.ui.component.EmptyState
import com.bsp.wsiw.core.ui.component.MoviePosterCard
import com.bsp.wsiw.core.ui.component.ScreenScaffold
import com.bsp.wsiw.core.ui.theme.AppTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatedMoviesScreen(
    onBack: () -> Unit,
    viewModel: RatedMoviesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenScaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.profile_rated_all_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.profile_cd_back),
                        )
                    }
                },
            )
        },
    ) { paddingValues, _ ->
        when {
            state.isLoading && state.movies.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }
            state.movies.isEmpty() -> {
                EmptyState(
                    icon = "⭐",
                    title = stringResource(R.string.profile_rated_empty_title),
                    body = stringResource(R.string.profile_rated_empty_body),
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                )
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        start = AppTheme.spacing.lg,
                        end = AppTheme.spacing.lg,
                        top = paddingValues.calculateTopPadding() + AppTheme.spacing.sm,
                        bottom = paddingValues.calculateBottomPadding() + AppTheme.spacing.lg,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.movies, key = { it.first.id }) { (movie, rating) ->
                        MoviePosterCard(
                            posterUrl = movie.posterUrl,
                            title = movie.title,
                            voteAverage = movie.voteAverage,
                            onClick = {},
                            overlayContent = {
                                Text(
                                    text = stringResource(R.string.profile_rating_badge, rating.roundToInt()),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(AppTheme.spacing.xs)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(4.dp),
                                        )
                                        .padding(horizontal = 5.dp, vertical = 2.dp),
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}
