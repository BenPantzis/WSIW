package com.bsp.wsiw.feature.profile

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.ui.component.AvatarImage
import com.bsp.wsiw.core.ui.component.MoviePosterCard
import com.bsp.wsiw.core.ui.theme.AppTheme
import kotlin.math.roundToInt
import androidx.core.net.toUri

@Composable
fun ProfileScreen(
    onSeeAllRatings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.OpenBrowser -> {
                    CustomTabsIntent.Builder().build()
                        .launchUrl(context, event.url.toUri())
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        when {
            state.isAuthenticated -> AuthenticatedContent(
                accountName = state.accountName ?: "",
                avatarUrl = state.avatarUrl,
                watchlistCount = state.watchlistCount,
                ratingsCount = state.ratingsCount,
                averageRating = state.averageRating,
                favoriteCount = state.favoriteCount,
                ratedMovies = state.ratedMovies,
                onSeeAllRatings = onSeeAllRatings,
                onSignOut = { viewModel.onAction(ProfileAction.SignOut) },
            )
            state.isAwaitingApproval -> AwaitingApprovalContent(
                isLoading = state.isExchangingToken,
                error = state.error,
                onContinue = { viewModel.onAction(ProfileAction.CompleteSignIn) },
                onCancel = { viewModel.onAction(ProfileAction.CancelSignIn) },
            )
            else -> UnauthenticatedContent(
                isLoading = state.isSigningIn,
                error = state.error,
                onSignIn = { viewModel.onAction(ProfileAction.SignIn) },
            )
        }
    }
}

@Composable
private fun AuthenticatedContent(
    accountName: String,
    avatarUrl: String?,
    watchlistCount: Int,
    ratingsCount: Int,
    averageRating: Float?,
    favoriteCount: Int,
    ratedMovies: List<Pair<Movie, Float>>,
    onSeeAllRatings: () -> Unit,
    onSignOut: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(AppTheme.spacing.xxl))

        AvatarImage(
            url = avatarUrl,
            name = accountName,
            size = 88.dp,
        )
        Spacer(Modifier.height(AppTheme.spacing.lg))
        Text(
            text = accountName,
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(AppTheme.spacing.xs))
        Text(
            text = stringResource(R.string.profile_auth_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(AppTheme.spacing.xl))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
        ) {
            StatChip(
                label = stringResource(R.string.profile_stats_watchlist),
                value = "$watchlistCount",
                modifier = Modifier.weight(1f),
            )
            StatChip(
                label = stringResource(R.string.profile_stats_rated),
                value = "$ratingsCount",
                modifier = Modifier.weight(1f),
            )
            StatChip(
                label = stringResource(R.string.profile_stats_avg_rating),
                value = averageRating?.let { "%.1f".format(it) } ?: "—",
                modifier = Modifier.weight(1f),
            )
            StatChip(
                label = stringResource(R.string.profile_stats_favorites),
                value = "$favoriteCount",
                modifier = Modifier.weight(1f),
            )
        }

        if (ratedMovies.isNotEmpty()) {
            Spacer(Modifier.height(AppTheme.spacing.xl))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.profile_ratings_header),
                    style = MaterialTheme.typography.titleSmall,
                )
                TextButton(onClick = onSeeAllRatings) {
                    Text(
                        text = stringResource(R.string.profile_ratings_see_all),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(Modifier.height(AppTheme.spacing.sm))
            LazyRow(
                contentPadding = PaddingValues(horizontal = AppTheme.spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
            ) {
                items(ratedMovies, key = { it.first.id }) { (movie, rating) ->
                    MoviePosterCard(
                        posterUrl = movie.posterUrl,
                        title = movie.title,
                        voteAverage = movie.voteAverage,
                        onClick = {},
                        modifier = Modifier.width(110.dp),
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

            Spacer(Modifier.height(AppTheme.spacing.xl))
            RatingBreakdown(
                ratedMovies = ratedMovies,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.lg),
            )
        }

        Spacer(Modifier.height(AppTheme.spacing.xxl))

        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.spacing.lg),
        ) {
            Text(stringResource(R.string.profile_action_sign_out))
        }

        Spacer(Modifier.height(AppTheme.spacing.xxl))
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun RatingBreakdown(
    ratedMovies: List<Pair<Movie, Float>>,
    modifier: Modifier = Modifier,
) {
    val distribution = remember(ratedMovies) {
        val counts = IntArray(10)
        ratedMovies.forEach { (_, rating) ->
            counts[rating.roundToInt().coerceIn(1, 10) - 1]++
        }
        counts.toList()
    }
    val maxCount = distribution.max().coerceAtLeast(1)
    val barMaxHeight: Dp = 48.dp

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.profile_breakdown_header),
            style = MaterialTheme.typography.titleSmall,
        )
        Spacer(Modifier.height(AppTheme.spacing.sm))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barMaxHeight + AppTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        ) {
            distribution.forEachIndexed { index, count ->
                val fraction = if (count == 0) 0f else (count.toFloat() / maxCount).coerceAtLeast(0.08f)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (count == 0) 2.dp else barMaxHeight * fraction)
                            .background(
                                color = if (count > 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp),
                            ),
                    )
                    Spacer(Modifier.height(AppTheme.spacing.xs))
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun AwaitingApprovalContent(
    isLoading: Boolean,
    error: String?,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppTheme.spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.profile_approval_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(AppTheme.spacing.sm))
        Text(
            text = stringResource(R.string.profile_approval_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(AppTheme.spacing.xxl))
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.profile_approval_continue))
            }
            Spacer(Modifier.height(AppTheme.spacing.sm))
            OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.profile_approval_cancel))
            }
        }
        if (error != null) {
            Spacer(Modifier.height(AppTheme.spacing.md))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun UnauthenticatedContent(
    isLoading: Boolean,
    error: String?,
    onSignIn: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppTheme.spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.profile_unauthenticated_title),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(AppTheme.spacing.sm))
        Text(
            text = stringResource(R.string.profile_unauthenticated_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(AppTheme.spacing.xxl))
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = onSignIn, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.profile_sign_in_button))
            }
        }
        if (error != null) {
            Spacer(Modifier.height(AppTheme.spacing.md))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
    }
}
