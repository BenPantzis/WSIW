package com.bsp.wsiw.feature.tv

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bsp.wsiw.core.domain.model.CastMember
import com.bsp.wsiw.core.domain.model.Season
import com.bsp.wsiw.core.domain.model.TvShow
import com.bsp.wsiw.core.domain.model.TvShowDetail
import com.bsp.wsiw.core.ui.component.AvatarImage
import com.bsp.wsiw.core.ui.component.ErrorContent
import com.bsp.wsiw.core.ui.component.MoviePosterCard
import com.bsp.wsiw.core.ui.component.RemoteImage
import com.bsp.wsiw.core.ui.component.shimmerEffect
import com.bsp.wsiw.core.ui.theme.AppTheme
import com.bsp.wsiw.core.ui.util.formatTmdbDate

private val BackdropHeight = 300.dp
private val ContentOverlap = 32.dp
private val ButtonScrim = Color(0x99000000)

@Composable
fun TvDetailScreen(
    seriesId: Int,
    onBack: () -> Unit,
    onShowClick: (Int) -> Unit,
    onPersonClick: (Int) -> Unit,
    viewModel: TvDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(seriesId) { viewModel.load(seriesId) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> TvDetailShimmer(onBack = onBack)
        uiState.error != null -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            ErrorContent(
                message = uiState.error!!.asString(),
                onRetry = { viewModel.onAction(TvDetailAction.Retry) },
                modifier = Modifier.align(Alignment.Center),
            )
            BackButton(onBack = onBack, modifier = Modifier.statusBarsPadding())
        }
        uiState.show != null -> CollapsingTvDetail(
            show = uiState.show!!,
            onBack = onBack,
            onShowClick = onShowClick,
            onPersonClick = onPersonClick,
        )
    }
}

@Composable
private fun CollapsingTvDetail(
    show: TvShowDetail,
    onBack: () -> Unit,
    onShowClick: (Int) -> Unit,
    onPersonClick: (Int) -> Unit,
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val firstVisibleOffset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
    val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    val backdropHeightPx = with(androidx.compose.ui.platform.LocalDensity.current) { BackdropHeight.toPx() }
    val scrolledPx = remember(firstVisibleIndex, firstVisibleOffset) {
        if (firstVisibleIndex == 0) firstVisibleOffset.toFloat() else backdropHeightPx
    }
    val backdropAlpha = (1f - scrolledPx / backdropHeightPx).coerceIn(0f, 1f)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
        ) {
            // Backdrop
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(BackdropHeight)
                        .graphicsLayer { alpha = backdropAlpha },
                ) {
                    RemoteImage(
                        url = show.backdropUrl ?: show.posterUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0f to Color.Transparent,
                                    1f to MaterialTheme.colorScheme.background,
                                )
                            ),
                    )
                }
            }

            // Main content card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { translationY = -ContentOverlap.toPx() },
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val spacing = AppTheme.spacing
                    Column(modifier = Modifier.padding(horizontal = spacing.xl, vertical = spacing.xl)) {
                        // Status + content rating chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StatusChip(status = show.status)
                            val rating = show.contentRating
                        if (rating != null) {
                                ContentRatingChip(rating = rating)
                            }
                        }

                        Spacer(Modifier.height(spacing.md))

                        // Title
                        Text(
                            text = show.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )

                        Spacer(Modifier.height(spacing.sm))

                        // Meta row: date · seasons · rating
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                        ) {
                            val firstAirDate = show.firstAirDate
                            if (firstAirDate != null) {
                                Text(
                                    text = formatTmdbDate(firstAirDate),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text("·", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            val seasonCount = show.seasons.size
                            Text(
                                text = if (seasonCount == 1) stringResource(R.string.tv_seasons_count, seasonCount)
                                       else stringResource(R.string.tv_seasons_count_plural, seasonCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text("·", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "★ %.1f".format(show.voteAverage),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        // Genres
                        if (show.genres.isNotEmpty()) {
                            Spacer(Modifier.height(spacing.sm))
                            Text(
                                text = show.genres.joinToString(" · ") { it.name },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Networks
                        if (show.networks.isNotEmpty()) {
                            Spacer(Modifier.height(spacing.xs))
                            Text(
                                text = show.networks.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Tagline
                        if (show.tagline.isNotEmpty()) {
                            Spacer(Modifier.height(spacing.lg))
                            Text(
                                text = "\"${show.tagline}\"",
                                style = MaterialTheme.typography.bodyLarge,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Overview
                        if (show.overview.isNotEmpty()) {
                            Spacer(Modifier.height(spacing.md))
                            Text(
                                text = show.overview,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }

                        // Trailer button
                        if (show.trailerKey != null) {
                            Spacer(Modifier.height(spacing.lg))
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${show.trailerKey}"))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC0000)),
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(spacing.xs))
                                Text(stringResource(R.string.tv_detail_trailer))
                            }
                        }
                    }
                }
            }

            // Seasons
            if (show.seasons.isNotEmpty()) {
                item {
                    SectionHeader(title = stringResource(R.string.tv_detail_seasons))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = AppTheme.spacing.xl),
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
                    ) {
                        items(show.seasons, key = { it.seasonNumber }) { season ->
                            SeasonCard(season = season)
                        }
                    }
                    Spacer(Modifier.height(AppTheme.spacing.xl))
                }
            }

            // Cast
            if (show.cast.isNotEmpty()) {
                item {
                    SectionHeader(title = stringResource(R.string.tv_detail_cast))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = AppTheme.spacing.xl),
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
                    ) {
                        items(show.cast, key = { it.id }) { member ->
                            CastMemberCard(member = member, onClick = { onPersonClick(member.id) })
                        }
                    }
                    Spacer(Modifier.height(AppTheme.spacing.xl))
                }
            }

            // Similar
            if (show.similar.isNotEmpty()) {
                item {
                    SectionHeader(title = stringResource(R.string.tv_detail_similar))
                    MediaRow(shows = show.similar, onShowClick = onShowClick)
                    Spacer(Modifier.height(AppTheme.spacing.xl))
                }
            }

            // Recommendations
            if (show.recommendations.isNotEmpty()) {
                item {
                    SectionHeader(title = stringResource(R.string.tv_detail_recommended))
                    MediaRow(shows = show.recommendations, onShowClick = onShowClick)
                    Spacer(Modifier.height(AppTheme.spacing.xl))
                }
            }

            item { Spacer(Modifier.navigationBarsPadding()) }
        }

        BackButton(
            onBack = onBack,
            modifier = Modifier.statusBarsPadding(),
        )
    }
}

// --- Section components ---

@Composable
private fun SectionHeader(title: String) {
    val spacing = AppTheme.spacing
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = spacing.xl, vertical = spacing.sm),
    )
}

@Composable
private fun StatusChip(status: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun ContentRatingChip(rating: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = rating,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun SeasonCard(season: Season) {
    val spacing = AppTheme.spacing
    Column(modifier = Modifier.width(100.dp)) {
        if (season.posterUrl != null) {
            AsyncImage(
                model = season.posterUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(MaterialTheme.shapes.small),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
        Spacer(Modifier.height(spacing.xs))
        Text(
            text = season.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "${season.episodeCount} eps",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CastMemberCard(member: CastMember, onClick: () -> Unit) {
    val spacing = AppTheme.spacing
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clip(MaterialTheme.shapes.small)
            .background(Color.Transparent)
            .padding(spacing.xs),
    ) {
        Surface(onClick = onClick, color = Color.Transparent, shape = CircleShape) {
            AvatarImage(
                url = member.profileUrl,
                name = member.name,
                size = 56.dp,
            )
        }
        Spacer(Modifier.height(spacing.xs))
        Text(
            text = member.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Text(
            text = member.character,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun MediaRow(shows: List<TvShow>, onShowClick: (Int) -> Unit) {
    val spacing = AppTheme.spacing
    LazyRow(
        contentPadding = PaddingValues(horizontal = spacing.xl),
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        items(shows, key = { it.id }) { show ->
            Box(modifier = Modifier.width(120.dp)) {
                MoviePosterCard(
                    posterUrl = show.posterUrl,
                    title = show.name,
                    voteAverage = show.voteAverage,
                    onClick = { onShowClick(show.id) },
                )
            }
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = AppTheme.spacing
    IconButton(
        onClick = onBack,
        modifier = modifier.padding(spacing.sm),
    ) {
        Surface(shape = CircleShape, color = ButtonScrim) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

@Composable
private fun TvDetailShimmer(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BackdropHeight)
                    .shimmerEffect(),
            )
        }
        BackButton(onBack = onBack, modifier = Modifier.statusBarsPadding())
    }
}
