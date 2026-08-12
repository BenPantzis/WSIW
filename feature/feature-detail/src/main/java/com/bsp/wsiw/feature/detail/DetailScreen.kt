package com.bsp.wsiw.feature.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.bsp.wsiw.core.domain.model.Review
import com.bsp.wsiw.core.domain.model.VideoEntry
import com.bsp.wsiw.core.domain.model.WatchProvider
import com.bsp.wsiw.core.domain.model.WatchProviders
import com.bsp.wsiw.core.ui.UiText
import com.bsp.wsiw.core.ui.theme.GoldDefault
import com.bsp.wsiw.core.ui.component.AvatarImage
import com.bsp.wsiw.core.ui.component.ErrorContent
import com.bsp.wsiw.core.ui.component.RemoteImage
import com.bsp.wsiw.core.ui.component.shimmerEffect
import com.bsp.wsiw.core.ui.theme.AppTheme
import com.bsp.wsiw.core.ui.util.formatTmdbDate
import androidx.compose.ui.res.stringResource
import kotlin.math.roundToInt

private val BackdropHeight = 300.dp
private val ContentOverlap = 32.dp
private val CardBottomPadding = 48.dp          // extra breathing room below last section
private val TopBarTitlePadding = 56.dp         // horizontal clear for back button
private val ButtonScrim = Color(0x99000000)    // semi-transparent scrim behind icon buttons
private val RatingChipHPadding = 14.dp
private val RatingChipVPadding = 7.dp

private val LocalAccentColor = compositionLocalOf { GoldDefault }

@Composable
fun DetailScreen(
    movieId: Int,
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
    onPersonClick: (Int) -> Unit = {},
    onReviewsClick: (movieId: Int, movieTitle: String) -> Unit = { _, _ -> },
    viewModel: DetailViewModel = hiltViewModel<DetailViewModel, DetailViewModel.Factory>(
        creationCallback = { it.create(movieId) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var submittedRating by remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DetailEvent.ShowError -> snackbarHostState.showSnackbar(event.message.resolve(context))
                DetailEvent.SignInRequired -> snackbarHostState.showSnackbar(context.getString(R.string.detail_sign_in_required))
                is DetailEvent.RatingSubmitted -> submittedRating = event.rating
            }
        }
    }

    DetailContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onBack = onBack,
        onMovieClick = onMovieClick,
        onPersonClick = onPersonClick,
        onReviewsClick = onReviewsClick,
        snackbarHostState = snackbarHostState,
        submittedRating = submittedRating,
        onFeedbackFinished = { submittedRating = null },
    )
}

@Composable
internal fun DetailContent(
    uiState: DetailUiState,
    onAction: (DetailAction) -> Unit,
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit = {},
    onPersonClick: (Int) -> Unit = {},
    onReviewsClick: (movieId: Int, movieTitle: String) -> Unit = { _, _ -> },
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    submittedRating: Float? = null,
    onFeedbackFinished: () -> Unit = {},
) {
    when {
        uiState.isLoading -> DetailLoadingContent(onBack = onBack)
        uiState.error != null -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            ErrorContent(
                message = uiState.error!!.asString(),
                onRetry = { onAction(DetailAction.Retry) },
                modifier = Modifier.align(Alignment.Center),
            )
            BackButton(onBack = onBack, modifier = Modifier.statusBarsPadding())
        }
        uiState.movie != null -> CollapsingDetailContent(
            movie = uiState.movie!!,
            onBack = onBack,
            onMovieClick = onMovieClick,
            onPersonClick = onPersonClick,
            onReviewsClick = onReviewsClick,
            previewReviews = uiState.previewReviews,
            totalReviewCount = uiState.totalReviewCount,
            accentArgb = uiState.accentArgb,
            isWatchlisted = uiState.isWatchlisted,
            onToggleWatchlist = { onAction(DetailAction.ToggleWatchlist) },
            isRefreshing = uiState.isRefreshing,
            snackbarHostState = snackbarHostState,
            watchProviders = uiState.watchProviders,
            userRating = uiState.userRating,
            showRatingDialog = uiState.showRatingDialog,
            onShowRatingDialog = { onAction(DetailAction.ShowRatingDialog) },
            onDismissRatingDialog = { onAction(DetailAction.DismissRatingDialog) },
            onRateMovie = { rating -> onAction(DetailAction.RateMovie(rating)) },
            onRemoveRating = { onAction(DetailAction.RemoveRating) },
            submittedRating = submittedRating,
            onFeedbackFinished = onFeedbackFinished,
        )
    }
}

// --- Collapsing detail ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollapsingDetailContent(
    movie: MovieDetail,
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
    onPersonClick: (Int) -> Unit,
    onReviewsClick: (movieId: Int, movieTitle: String) -> Unit,
    previewReviews: List<Review>,
    totalReviewCount: Int,
    accentArgb: Int?,
    isWatchlisted: Boolean,
    onToggleWatchlist: () -> Unit,
    isRefreshing: Boolean,
    snackbarHostState: SnackbarHostState,
    watchProviders: WatchProviders? = null,
    userRating: Float? = null,
    showRatingDialog: Boolean = false,
    onShowRatingDialog: () -> Unit = {},
    onDismissRatingDialog: () -> Unit = {},
    onRateMovie: (Float) -> Unit = {},
    onRemoveRating: () -> Unit = {},
    submittedRating: Float? = null,
    onFeedbackFinished: () -> Unit = {},
) {
    var chipCenterPx by remember { mutableStateOf(Offset.Zero) }
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
            BookmarkButton(
                isWatchlisted = isWatchlisted,
                onClick = onToggleWatchlist,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .graphicsLayer { alpha = (1f - scrollFraction).coerceIn(0f, 1f) }
                    .padding(top = BackdropHeight - ContentOverlap - 56.dp, end = AppTheme.spacing.lg),
            )
            ContentList(
                movie = movie,
                listState = listState,
                onMovieClick = onMovieClick,
                onPersonClick = onPersonClick,
                onReviewsClick = onReviewsClick,
                previewReviews = previewReviews,
                totalReviewCount = totalReviewCount,
                watchProviders = watchProviders,
                userRating = userRating,
                onShowRatingDialog = onShowRatingDialog,
                onRatingRowPositioned = { chipCenterPx = it },
            )
            if (submittedRating != null && chipCenterPx != Offset.Zero) {
                FloatingRatingFeedback(
                    rating = submittedRating!!,
                    chipCenter = chipCenterPx,
                    onFinished = onFeedbackFinished,
                )
            }
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

        if (showRatingDialog) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = onDismissRatingDialog,
                sheetState = sheetState,
            ) {
                RatingBottomSheet(
                    currentRating = userRating,
                    accentColor = accentColor,
                    posterUrl = movie.posterUrl,
                    title = movie.title,
                    year = movie.releaseDate.take(4),
                    onRate = onRateMovie,
                    onRemove = onRemoveRating,
                )
            }
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
    onPersonClick: (Int) -> Unit,
    onReviewsClick: (movieId: Int, movieTitle: String) -> Unit,
    previewReviews: List<Review>,
    totalReviewCount: Int,
    watchProviders: WatchProviders? = null,
    userRating: Float? = null,
    onShowRatingDialog: () -> Unit = {},
    onRatingRowPositioned: (Offset) -> Unit = {},
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Spacer(modifier = Modifier.height(BackdropHeight - ContentOverlap))
        }
        item {
            MovieDetailCard(
                movie = movie,
                onMovieClick = onMovieClick,
                onPersonClick = onPersonClick,
                onReviewsClick = onReviewsClick,
                previewReviews = previewReviews,
                totalReviewCount = totalReviewCount,
                watchProviders = watchProviders,
                userRating = userRating,
                onShowRatingDialog = onShowRatingDialog,
                onRatingRowPositioned = onRatingRowPositioned,
            )
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
                    .padding(horizontal = TopBarTitlePadding),
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
                .background(ButtonScrim, CircleShape),
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = if (isWatchlisted) stringResource(R.string.detail_cd_remove_from_watchlist) else stringResource(R.string.detail_cd_add_to_watchlist),
                tint = if (isWatchlisted) LocalAccentColor.current else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
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
                .background(ButtonScrim, CircleShape),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// --- Detail card content ---

@Composable
private fun MovieDetailCard(
    movie: MovieDetail,
    onMovieClick: (Int) -> Unit,
    onPersonClick: (Int) -> Unit,
    onReviewsClick: (movieId: Int, movieTitle: String) -> Unit,
    previewReviews: List<Review>,
    totalReviewCount: Int,
    watchProviders: WatchProviders? = null,
    userRating: Float? = null,
    onShowRatingDialog: () -> Unit = {},
    onRatingRowPositioned: (Offset) -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = ContentOverlap, topEnd = ContentOverlap),
        color = MaterialTheme.colorScheme.background,
    ) {
        val spacing = AppTheme.spacing
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacing.xl, bottom = CardBottomPadding),
        ) {
            Column(modifier = Modifier.padding(horizontal = spacing.content)) {
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

                Spacer(Modifier.height(spacing.lg))
                RatingRow(userRating = userRating, onClick = onShowRatingDialog, onPositioned = onRatingRowPositioned)

                Spacer(Modifier.height(spacing.xl))
                SectionHeader(stringResource(R.string.detail_section_overview))
                Spacer(Modifier.height(spacing.sm))
                Text(
                    text = movie.overview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                )

                Spacer(Modifier.height(spacing.xl))
                SectionHeader(stringResource(R.string.detail_section_details))
                Spacer(Modifier.height(spacing.sm))
                if (movie.releaseDate.isNotBlank()) {
                    DetailRow(stringResource(R.string.detail_row_release_date), formatReleaseDate(movie.releaseDate))
                }
                if (movie.originalLanguage.isNotBlank()) {
                    DetailRow(stringResource(R.string.detail_row_language), movie.originalLanguage.uppercase())
                }
                if (movie.runtime > 0) {
                    DetailRow(stringResource(R.string.detail_row_runtime), formatRuntime(movie.runtime))
                }
                DetailRow(
                    stringResource(R.string.detail_row_score),
                    stringResource(
                        R.string.detail_score_value,
                        "${(movie.voteAverage * 10).roundToInt() / 10.0}",
                        movie.voteCount.formatCount(),
                    ),
                )
            }

            // Trailer
            if (movie.trailer != null) {
                Spacer(Modifier.height(AppTheme.spacing.xl))
                SectionHeader(stringResource(R.string.detail_section_trailer), modifier = Modifier.padding(horizontal = spacing.content))
                Spacer(Modifier.height(AppTheme.spacing.sm))
                TrailerButton(
                    trailer = movie.trailer!!,
                    modifier = Modifier.padding(horizontal = spacing.content),
                )
            }

            // Cast
            if (movie.cast.isNotEmpty()) {
                Spacer(Modifier.height(AppTheme.spacing.xl))
                SectionHeader(stringResource(R.string.detail_section_cast), modifier = Modifier.padding(horizontal = spacing.content))
                Spacer(Modifier.height(AppTheme.spacing.sm))
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = spacing.content),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
                ) {
                    items(movie.cast) { member -> CastCard(member, onClick = { onPersonClick(member.id) }) }
                }
            }

            // Where to Watch
            if (watchProviders != null && !watchProviders.isEmpty) {
                Spacer(Modifier.height(AppTheme.spacing.xl))
                SectionHeader(stringResource(R.string.detail_section_where_to_watch), modifier = Modifier.padding(horizontal = spacing.content))
                Spacer(Modifier.height(AppTheme.spacing.sm))
                WatchProvidersSection(
                    providers = watchProviders,
                    modifier = Modifier.padding(horizontal = spacing.content),
                )
            }

            // Recommended / Similar movies
            val displayMovies = movie.recommendedMovies.ifEmpty { movie.similarMovies }
            if (displayMovies.isNotEmpty()) {
                Spacer(Modifier.height(AppTheme.spacing.xl))
                SectionHeader(stringResource(R.string.detail_section_recommended), modifier = Modifier.padding(horizontal = spacing.content))
                Spacer(Modifier.height(AppTheme.spacing.sm))
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = spacing.content),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                ) {
                    items(displayMovies) { movie ->
                        MovieThumbnailCard(
                            posterUrl = movie.posterUrl,
                            title = movie.title,
                            onClick = { onMovieClick(movie.id) },
                        )
                    }
                }
            }

            // Reviews
            if (previewReviews.isNotEmpty()) {
                Spacer(Modifier.height(AppTheme.spacing.xl))
                InlineReviewsSection(
                    movieId = movie.id,
                    movieTitle = movie.title,
                    reviews = previewReviews,
                    totalCount = totalReviewCount,
                    onReviewsClick = onReviewsClick,
                )
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
            .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.md),
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
private fun CastCard(member: CastMember, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick),
    ) {
        AvatarImage(
            url = member.profileUrl,
            name = member.name,
            size = 56.dp,
        )
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
        if (movie.certification != null) {
            Text(
                text = "•",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            CertificationBadge(certification = movie.certification!!)
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
                .padding(horizontal = AppTheme.spacing.content, vertical = AppTheme.spacing.xl),
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

@Composable
private fun InlineReviewsSection(
    movieId: Int,
    movieTitle: String,
    reviews: List<Review>,
    totalCount: Int,
    onReviewsClick: (movieId: Int, movieTitle: String) -> Unit,
) {
    val spacing = AppTheme.spacing
    val accent = LocalAccentColor.current
    val reviewsTitle = if (totalCount > 0)
        stringResource(R.string.detail_section_reviews_with_count, totalCount)
    else
        stringResource(R.string.detail_section_reviews)

    Column(modifier = Modifier.padding(horizontal = spacing.content)) {
        SectionHeader(reviewsTitle)
        Spacer(Modifier.height(spacing.md))
        reviews.forEach { review ->
            InlineReviewCard(review = review)
            Spacer(Modifier.height(spacing.md))
        }
        Spacer(Modifier.height(spacing.xs))
        OutlinedButton(
            onClick = { onReviewsClick(movieId, movieTitle) },
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.7f)),
        ) {
            Text(
                text = stringResource(R.string.detail_see_all_reviews),
                color = accent,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun InlineReviewCard(review: Review) {
    val spacing = AppTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
            .padding(spacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = review.author,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = formatTmdbDate(review.createdAt.take(10)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (review.rating != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "★", color = LocalAccentColor.current, style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = "${review.rating}/10",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
        Spacer(Modifier.height(spacing.sm))
        Text(
            text = review.content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
        )
    }
}

// --- New: Certification badge ---

@Composable
private fun CertificationBadge(certification: String) {
    Text(
        text = certification,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 5.dp, vertical = 2.dp),
    )
}

// --- New: Watch providers ---

@Composable
private fun WatchProvidersSection(providers: WatchProviders, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
        if (providers.streaming.isNotEmpty()) ProviderRow(label = stringResource(R.string.detail_providers_stream), providers = providers.streaming)
        if (providers.rent.isNotEmpty()) ProviderRow(label = stringResource(R.string.detail_providers_rent), providers = providers.rent)
        if (providers.buy.isNotEmpty()) ProviderRow(label = stringResource(R.string.detail_providers_buy), providers = providers.buy)
    }
}

@Composable
private fun ProviderRow(label: String, providers: List<WatchProvider>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(44.dp),
        )
        providers.forEach { provider ->
            RemoteImage(
                url = provider.logoUrl,
                contentDescription = provider.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.small),
            )
        }
    }
}

// --- Rating ---

@Composable
private fun RatingRow(
    userRating: Float?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPositioned: (Offset) -> Unit = {},
) {
    val accent = LocalAccentColor.current
    val isRated = userRating != null
    val starsFilled = userRating?.let { (it / 2f).roundToInt().coerceIn(1, 5) } ?: 0
    val chipBackground = if (isRated) accent.copy(alpha = 0.12f) else Color.Transparent
    val chipBorder = if (isRated) accent.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val labelColor = if (isRated) accent else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .onGloballyPositioned { coords ->
                val topLeft = coords.localToRoot(Offset.Zero)
                val sz = coords.size
                onPositioned(Offset(topLeft.x + sz.width / 2f, topLeft.y + sz.height / 2f))
            }
            .clickable(onClick = onClick)
            .border(1.dp, chipBorder, RoundedCornerShape(20.dp))
            .background(chipBackground, RoundedCornerShape(20.dp))
            .padding(horizontal = RatingChipHPadding, vertical = RatingChipVPadding),
    ) {
        Text(
            text = if (isRated) "★".repeat(starsFilled) + "☆".repeat(5 - starsFilled) else "★",
            style = MaterialTheme.typography.labelLarge,
            color = if (isRated) accent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
        Text(
            text = if (isRated) {
                val v = userRating!!
                stringResource(R.string.detail_rating_display, if (v == v.toLong().toFloat()) v.toInt().toString() else v.toString())
            } else {
                stringResource(R.string.detail_rate_this_film)
            },
            style = MaterialTheme.typography.labelMedium,
            color = labelColor,
        )
    }
}

@Composable
private fun RatingBottomSheet(
    currentRating: Float?,
    accentColor: Color,
    posterUrl: String?,
    title: String,
    year: String,
    onRate: (Float) -> Unit,
    onRemove: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    // Tracks the live selection while the user is tapping (before confirm)
    var liveRating by remember(currentRating) { mutableStateOf(currentRating) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = AppTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Movie context header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.spacing.content, vertical = AppTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (posterUrl != null) {
                RemoteImage(
                    url = posterUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(48.dp)
                        .aspectRatio(2f / 3f)
                        .clip(MaterialTheme.shapes.small),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
                if (year.length == 4) {
                    Text(
                        text = year,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Divider
        androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Spacer(Modifier.height(AppTheme.spacing.content))

        // Live numeric display — above the stars so it's visible while dragging
        val displayText = liveRating?.let {
            val formatted = if (it == it.toLong().toFloat()) it.toInt().toString() else it.toString()
            stringResource(R.string.detail_rating_display, formatted)
        } ?: stringResource(R.string.detail_drag_to_rate)
        Text(
            text = displayText,
            style = MaterialTheme.typography.bodyMedium,
            color = if (liveRating != null) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )

        Spacer(Modifier.height(AppTheme.spacing.md))

        // Stars — unified drag surface; finger position maps to rating
        var starRowWidthPx by remember { mutableStateOf(0f) }

        Box(
            modifier = Modifier
                .onSizeChanged { starRowWidthPx = it.width.toFloat() }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val newRating = ratingFromX(down.position.x, starRowWidthPx)
                        if (newRating != liveRating) {
                            liveRating = newRating
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        drag(down.id) { change ->
                            change.consume()
                            val dragged = ratingFromX(change.position.x, starRowWidthPx)
                            if (dragged != liveRating) {
                                liveRating = dragged
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                    }
                },
        ) {
            val starsFloat = (liveRating ?: 0f) / 2f
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (i in 1..5) {
                    val isFullFilled = starsFloat >= i
                    val isHalfFilled = !isFullFilled && starsFloat >= i - 0.5f
                    Text(
                        text = "★",
                        style = MaterialTheme.typography.displaySmall,
                        color = when {
                            isFullFilled -> accentColor
                            isHalfFilled -> accentColor.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        },
                        modifier = Modifier.size(44.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(AppTheme.spacing.content))

        Button(
            onClick = { liveRating?.let(onRate) },
            enabled = liveRating != null,
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.spacing.content),
        ) {
            Text(
                text = stringResource(R.string.detail_rate_button),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        if (currentRating != null) {
            TextButton(onClick = onRemove) {
                Text(
                    text = stringResource(R.string.detail_clear_rating),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        } else {
            Spacer(Modifier.height(AppTheme.spacing.sm))
        }
    }
}

// --- Emoji rating feedback ---

@Composable
private fun FloatingRatingFeedback(
    rating: Float,
    chipCenter: Offset,
    onFinished: () -> Unit,
) {
    val accent = LocalAccentColor.current
    val density = LocalDensity.current
    val emoji = emojiForRating(rating)

    val scale = remember { Animatable(0f) }
    val travelY = remember { Animatable(0f) }
    val emojiAlpha = remember { Animatable(1f) }
    val particleRadius = remember { Animatable(0f) }
    val particleAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        val travelPx = with(density) { 96.dp.toPx() }
        val particlePx = with(density) { 38.dp.toPx() }
        launch { scale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 350f)) }
        launch { travelY.animateTo(-travelPx, tween(860, easing = FastOutSlowInEasing)) }
        launch {
            delay(300)
            emojiAlpha.animateTo(0f, tween(560))
            onFinished()
        }
        launch { particleRadius.animateTo(particlePx, tween(420, easing = FastOutSlowInEasing)) }
        launch {
            delay(70)
            particleAlpha.animateTo(0f, tween(380))
        }
    }

    val dotRadiusPx = with(density) { 4.dp.toPx() }
    val emojiHalfPx = with(density) { 20.dp.toPx() }

    // Particle ring — drawn during the draw phase so no recomposition on value change
    Canvas(Modifier.fillMaxSize()) {
        val pa = particleAlpha.value
        val pr = particleRadius.value
        if (pa > 0f) {
            val color = accent.copy(alpha = pa * 0.8f)
            repeat(8) { i ->
                val angle = Math.toRadians(i * 45.0)
                drawCircle(
                    color = color,
                    radius = dotRadiusPx,
                    center = Offset(
                        chipCenter.x + (pr * cos(angle)).toFloat(),
                        chipCenter.y + (pr * sin(angle)).toFloat(),
                    ),
                )
            }
        }
    }

    // Emoji — starts at chip center, scales in with spring, floats upward, fades out
    Text(
        text = emoji,
        style = MaterialTheme.typography.displaySmall,
        modifier = Modifier
            .absoluteOffset {
                IntOffset(
                    x = (chipCenter.x - emojiHalfPx).roundToInt(),
                    y = (chipCenter.y - emojiHalfPx).roundToInt(),
                )
            }
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                translationY = travelY.value
                alpha = emojiAlpha.value
            },
    )
}

private fun emojiForRating(rating: Float): String = when {
    rating <= 2f -> "😤"
    rating <= 4f -> "😕"
    rating <= 6f -> "😐"
    rating <= 8f -> "😊"
    else -> "🤩"
}

// --- Helpers ---

/** Maps a finger X position (pixels) across the star row to a TMDB rating in 0.5 steps (0.5–10). */
private fun ratingFromX(x: Float, width: Float): Float {
    if (width <= 0f) return 0.5f
    val fraction = (x / width).coerceIn(0f, 1f)
    val raw = fraction * 10f
    return (kotlin.math.round(raw * 2) / 2f).coerceIn(0.5f, 10f)
}

private fun formatReleaseDate(raw: String): String = formatTmdbDate(raw)

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
