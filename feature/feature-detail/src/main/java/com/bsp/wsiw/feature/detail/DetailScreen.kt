package com.bsp.wsiw.feature.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bsp.wsiw.core.domain.model.CastMember
import com.bsp.wsiw.core.domain.model.Genre
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.model.MovieDetail
import com.bsp.wsiw.core.domain.model.VideoEntry
import com.bsp.wsiw.core.ui.component.ErrorContent
import com.bsp.wsiw.core.ui.component.RemoteImage
import com.bsp.wsiw.core.ui.component.shimmerEffect
import com.bsp.wsiw.core.ui.theme.AppTheme
import kotlin.math.roundToInt

private val BackdropHeight = 300.dp
private val ContentOverlap = 32.dp
private val GoldDefault = Color(0xFFE8A020)

private val LocalAccentColor = compositionLocalOf { GoldDefault }

@Composable
fun DetailScreen(
    movieId: Int,
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
    viewModel: DetailViewModel = hiltViewModel<DetailViewModel, DetailViewModel.Factory>(
        creationCallback = { it.create(movieId) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DetailEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    DetailContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onBack = onBack,
        onMovieClick = onMovieClick,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
internal fun DetailContent(
    uiState: DetailUiState,
    onAction: (DetailAction) -> Unit,
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    when {
        uiState.isLoading -> DetailLoadingContent(onBack = onBack)
        uiState.error != null -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            ErrorContent(
                message = uiState.error!!,
                onRetry = { onAction(DetailAction.Retry) },
                modifier = Modifier.align(Alignment.Center),
            )
            BackButton(onBack = onBack, modifier = Modifier.statusBarsPadding())
        }
        uiState.movie != null -> CollapsingDetailContent(
            movie = uiState.movie!!,
            onBack = onBack,
            onMovieClick = onMovieClick,
            accentArgb = uiState.accentArgb,
            isWatchlisted = uiState.isWatchlisted,
            onToggleWatchlist = { onAction(DetailAction.ToggleWatchlist) },
            isRefreshing = uiState.isRefreshing,
            snackbarHostState = snackbarHostState,
        )
    }
}

// --- Collapsing detail ---

@Composable
private fun CollapsingDetailContent(
    movie: MovieDetail,
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
    accentArgb: Int?,
    isWatchlisted: Boolean,
    onToggleWatchlist: () -> Unit,
    isRefreshing: Boolean,
    snackbarHostState: SnackbarHostState,
) {
    val backdropHeightPx = with(LocalDensity.current) { BackdropHeight.toPx() }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    val scrollFraction by remember {
        derivedStateOf {
            val offset = if (listState.firstVisibleItemIndex == 0) {
                listState.firstVisibleItemScrollOffset.toFloat()
            } else {
                backdropHeightPx
            }
            (offset / backdropHeightPx).coerceIn(0f, 1f)
        }
    }

    val accentColor by animateColorAsState(
        targetValue = accentArgb?.let { Color(it) } ?: GoldDefault,
        animationSpec = tween(durationMillis = 300),
        label = "palette_color",
    )

    CompositionLocalProvider(LocalAccentColor provides accentColor) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            BackdropLayer(
                movie = movie,
                listState = listState,
                scrollFraction = scrollFraction,
                backdropHeightPx = backdropHeightPx,
            )
            ContentList(movie = movie, listState = listState, onMovieClick = onMovieClick)
            BookmarkButton(
                isWatchlisted = isWatchlisted,
                onClick = onToggleWatchlist,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .graphicsLayer { alpha = (1f - scrollFraction).coerceIn(0f, 1f) }
                    .padding(top = BackdropHeight - ContentOverlap - 56.dp, end = 16.dp),
            )
            TopBar(
                title = movie.title,
                scrollFraction = scrollFraction,
                onBack = onBack,
                isRefreshing = isRefreshing,
            )
            androidx.compose.material3.SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun BackdropLayer(
    movie: MovieDetail,
    listState: LazyListState,
    scrollFraction: Float,
    backdropHeightPx: Float,
) {
    val parallaxOffset by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                -listState.firstVisibleItemScrollOffset.toFloat() * 0.35f
            } else {
                -backdropHeightPx * 0.35f
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BackdropHeight)
            .graphicsLayer {
                translationY = parallaxOffset
                alpha = 1f - scrollFraction
            },
    ) {
        RemoteImage(
            url = movie.backdropUrl ?: movie.posterUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        val accent = LocalAccentColor.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.25f to Color.Transparent,
                            0.70f to accent.copy(alpha = 0.18f),
                            1.0f to Color(0xFF0D0D0D),
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun ContentList(
    movie: MovieDetail,
    listState: LazyListState,
    onMovieClick: (Int) -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Spacer(modifier = Modifier.height(BackdropHeight - ContentOverlap))
        }
        item {
            MovieDetailCard(movie = movie, onMovieClick = onMovieClick)
        }
    }
}

@Composable
private fun TopBar(title: String, scrollFraction: Float, onBack: () -> Unit, isRefreshing: Boolean) {
    val bgAlpha = scrollFraction.coerceIn(0f, 0.95f)
    val titleAlpha = ((scrollFraction - 0.6f) / 0.4f).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = bgAlpha))
            .statusBarsPadding(),
    ) {
        BackButton(onBack = onBack)

        if (titleAlpha > 0f) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = titleAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 56.dp),
            )
        }

        if (isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                color = GoldDefault,
                trackColor = Color.Transparent,
            )
        }
    }
}

@Composable
private fun BookmarkButton(
    isWatchlisted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .background(Color(0x99000000), CircleShape),
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = if (isWatchlisted) "Remove from watchlist" else "Add to watchlist",
                tint = if (isWatchlisted) Color(0xFFE8A020) else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onBack, modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .background(Color(0x99000000), CircleShape),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// --- Detail card content ---

@Composable
private fun MovieDetailCard(movie: MovieDetail, onMovieClick: (Int) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = ContentOverlap, topEnd = ContentOverlap),
        color = MaterialTheme.colorScheme.background,
    ) {
        val spacing = AppTheme.spacing
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacing.xl, bottom = 48.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                StatsRow(movie = movie)
                Spacer(Modifier.height(spacing.lg))

                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                if (movie.tagline.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "“${movie.tagline}”",
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (movie.genres.isNotEmpty()) {
                    Spacer(Modifier.height(spacing.lg))
                    GenreChipsRow(genres = movie.genres)
                }

                Spacer(Modifier.height(spacing.xl))
                SectionHeader("Overview")
                Spacer(Modifier.height(spacing.sm))
                Text(
                    text = movie.overview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                )

                Spacer(Modifier.height(spacing.xl))
                SectionHeader("Details")
                Spacer(Modifier.height(spacing.sm))
                if (movie.releaseDate.isNotBlank()) {
                    DetailRow("Release Date", formatReleaseDate(movie.releaseDate))
                }
                if (movie.originalLanguage.isNotBlank()) {
                    DetailRow("Language", movie.originalLanguage.uppercase())
                }
                if (movie.runtime > 0) {
                    DetailRow("Runtime", formatRuntime(movie.runtime))
                }
                DetailRow("Score", "${(movie.voteAverage * 10).roundToInt() / 10.0} / 10 (${movie.voteCount.formatCount()} votes)")
            }

            // Trailer
            if (movie.trailer != null) {
                Spacer(Modifier.height(AppTheme.spacing.xl))
                SectionHeader("Trailer", modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(AppTheme.spacing.sm))
                TrailerButton(
                    trailer = movie.trailer!!,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // Cast
            if (movie.cast.isNotEmpty()) {
                Spacer(Modifier.height(AppTheme.spacing.xl))
                SectionHeader("Cast", modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(AppTheme.spacing.sm))
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
                ) {
                    items(movie.cast) { member -> CastCard(member) }
                }
            }

            // Similar movies
            if (movie.similarMovies.isNotEmpty()) {
                Spacer(Modifier.height(AppTheme.spacing.xl))
                SectionHeader("More Like This", modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(AppTheme.spacing.sm))
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                ) {
                    items(movie.similarMovies) { similar ->
                        SimilarMovieCard(movie = similar, onClick = { onMovieClick(similar.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TrailerButton(trailer: VideoEntry, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val accent = LocalAccentColor.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .border(1.dp, accent.copy(alpha = 0.7f), MaterialTheme.shapes.small)
            .clickable {
                val uri = Uri.parse("https://www.youtube.com/watch?v=${trailer.key}")
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
            .padding(horizontal = AppTheme.spacing.md, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = trailer.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CastCard(member: CastMember) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp),
    ) {
        if (member.profileUrl != null) {
            RemoteImage(
                url = member.profileUrl,
                contentDescription = member.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Text(
                    text = member.name.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = member.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = member.character,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontStyle = FontStyle.Italic,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SimilarMovieCard(movie: Movie, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(110.dp)
            .aspectRatio(2f / 3f)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
    ) {
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
                            0.5f to Color.Transparent,
                            1.0f to Color(0xCC0D0D0D),
                        ),
                    ),
                ),
        )
        Text(
            text = movie.title,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp),
        )
    }
}

@Composable
private fun StatsRow(movie: MovieDetail) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
    ) {
        StarRating(voteAverage = movie.voteAverage)
        Text(
            text = "(${movie.voteCount.formatCount()})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (movie.runtime > 0) {
            Text(
                text = "•",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = formatRuntime(movie.runtime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StarRating(voteAverage: Double, modifier: Modifier = Modifier) {
    val filled = (voteAverage / 2).roundToInt().coerceIn(0, 5)
    Text(
        text = "★".repeat(filled) + "☆".repeat(5 - filled),
        style = MaterialTheme.typography.titleSmall,
        color = LocalAccentColor.current,
        modifier = modifier,
    )
}

@Composable
private fun GenreChipsRow(genres: List<Genre>) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
    ) {
        genres.forEach { genre -> GenreChip(genre = genre) }
    }
}

@Composable
private fun GenreChip(genre: Genre) {
    Text(
        text = genre.name,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .border(
                width = 1.dp,
                color = LocalAccentColor.current.copy(alpha = 0.6f),
                shape = MaterialTheme.shapes.extraSmall,
            )
            .padding(horizontal = AppTheme.spacing.md, vertical = 6.dp),
    )
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = LocalAccentColor.current,
        letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing,
        modifier = modifier,
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(
            text = "$label  ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

// --- Loading skeleton ---

@Composable
private fun DetailLoadingContent(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BackdropHeight)
                .shimmerEffect(),
        )

        Column(
            modifier = Modifier
                .padding(top = BackdropHeight - ContentOverlap)
                .padding(horizontal = 20.dp, vertical = AppTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(modifier = Modifier.width(120.dp).height(16.dp).shimmerEffect())
            Box(modifier = Modifier.fillMaxWidth(0.85f).height(32.dp).shimmerEffect())
            Box(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp).shimmerEffect())
            Spacer(Modifier.height(AppTheme.spacing.xs))
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
                repeat(3) { Box(modifier = Modifier.width(72.dp).height(28.dp).shimmerEffect()) }
            }
            Spacer(Modifier.height(AppTheme.spacing.sm))
            repeat(6) {
                Box(modifier = Modifier.fillMaxWidth(if (it == 5) 0.7f else 1f).height(14.dp).shimmerEffect())
            }
            // Trailer skeleton
            Spacer(Modifier.height(AppTheme.spacing.md))
            Box(modifier = Modifier.fillMaxWidth().height(46.dp).shimmerEffect())
            // Cast skeleton
            Spacer(Modifier.height(AppTheme.spacing.md))
            Box(modifier = Modifier.width(60.dp).height(12.dp).shimmerEffect())
            Spacer(Modifier.height(AppTheme.spacing.xs))
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
                repeat(4) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(56.dp).clip(CircleShape).shimmerEffect())
                        Spacer(Modifier.height(6.dp))
                        Box(modifier = Modifier.width(60.dp).height(10.dp).shimmerEffect())
                    }
                }
            }
            // Similar movies skeleton
            Spacer(Modifier.height(AppTheme.spacing.md))
            Box(modifier = Modifier.width(80.dp).height(12.dp).shimmerEffect())
            Spacer(Modifier.height(AppTheme.spacing.xs))
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .width(110.dp)
                            .aspectRatio(2f / 3f)
                            .clip(MaterialTheme.shapes.small)
                            .shimmerEffect(),
                    )
                }
            }
        }

        BackButton(onBack = onBack, modifier = Modifier.statusBarsPadding())
    }
}

// --- Helpers ---

private fun formatReleaseDate(raw: String): String = try {
    val date = java.time.LocalDate.parse(raw)
    date.format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy"))
} catch (_: Exception) {
    raw
}

private fun formatRuntime(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

private fun Int.formatCount(): String = when {
    this >= 1_000_000 -> "${this / 1_000_000}M"
    this >= 1_000 -> "${this / 1_000}K"
    else -> toString()
}
