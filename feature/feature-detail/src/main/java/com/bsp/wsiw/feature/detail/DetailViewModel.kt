package com.bsp.wsiw.feature.detail

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.repository.MovieRepository
import com.bsp.wsiw.core.domain.repository.SessionRepository
import com.bsp.wsiw.core.domain.usecase.GetMovieDetailUseCase
import com.bsp.wsiw.core.domain.usecase.GetMovieRatingUseCase
import com.bsp.wsiw.core.domain.usecase.IsWatchlistedUseCase
import com.bsp.wsiw.core.domain.usecase.RateMovieUseCase
import com.bsp.wsiw.core.domain.usecase.ToggleWatchlistUseCase
import com.bsp.wsiw.core.ui.BaseViewModel
import com.bsp.wsiw.core.ui.UiText
import com.bsp.wsiw.core.ui.R as CoreUiR
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.pow

@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
class DetailViewModel @AssistedInject constructor(
    @Assisted private val movieId: Int,
    private val getMovieDetail: GetMovieDetailUseCase,
    private val isWatchlisted: IsWatchlistedUseCase,
    private val toggleWatchlist: ToggleWatchlistUseCase,
    private val getMovieRating: GetMovieRatingUseCase,
    private val rateMovie: RateMovieUseCase,
    private val movieRepository: MovieRepository,
    private val sessionRepository: SessionRepository,
    @param:ApplicationContext private val context: Context,
) : BaseViewModel<DetailAction, DetailEvent, DetailUiState>(
    initialState = DetailUiState(),
) {
    @AssistedFactory
    interface Factory {
        fun create(movieId: Int): DetailViewModel
    }

    private var loadJob: Job? = null

    init {
        loadDetail()
        loadReviewPreview()
        loadWatchProviders()
        viewModelScope.launch {
            isWatchlisted(movieId).collect { watchlisted ->
                updateState { copy(isWatchlisted = watchlisted) }
            }
        }
        viewModelScope.launch {
            sessionRepository.isAuthenticated.collect { authenticated ->
                updateState { copy(isAuthenticated = authenticated) }
            }
        }
        viewModelScope.launch {
            getMovieRating(movieId).collect { rating ->
                updateState { copy(userRating = rating) }
            }
        }
    }

    override fun handleAction(action: DetailAction) {
        when (action) {
            DetailAction.Retry -> loadDetail()
            DetailAction.ToggleWatchlist -> {
                if (!uiState.value.isAuthenticated) {
                    viewModelScope.launch { sendEvent(DetailEvent.SignInRequired) }
                    return
                }
                val detail = uiState.value.movie ?: return
                viewModelScope.launch {
                    toggleWatchlist(
                        movie = Movie(
                            id = detail.id,
                            title = detail.title,
                            overview = detail.overview,
                            posterUrl = detail.posterUrl,
                            backdropUrl = detail.backdropUrl,
                            releaseDate = detail.releaseDate,
                            voteAverage = detail.voteAverage,
                            voteCount = detail.voteCount,
                        ),
                        isWatchlisted = uiState.value.isWatchlisted,
                    )
                }
            }
            DetailAction.ShowRatingDialog -> {
                if (!uiState.value.isAuthenticated) {
                    viewModelScope.launch { sendEvent(DetailEvent.SignInRequired) }
                    return
                }
                updateState { copy(showRatingDialog = true) }
            }
            DetailAction.DismissRatingDialog -> updateState { copy(showRatingDialog = false) }
            is DetailAction.RateMovie -> {
                updateState { copy(showRatingDialog = false) }
                viewModelScope.launch {
                    sendEvent(DetailEvent.RatingSubmitted(action.rating))
                    rateMovie(movieId, action.rating)
                }
            }
            DetailAction.RemoveRating -> {
                updateState { copy(showRatingDialog = false) }
                viewModelScope.launch { rateMovie.remove(movieId) }
            }
        }
    }

    private fun loadWatchProviders() {
        viewModelScope.launch {
            movieRepository.getWatchProviders(movieId).collect { result ->
                if (result is com.bsp.wsiw.core.common.Result.Success && !result.data.isEmpty) {
                    updateState { copy(watchProviders = result.data) }
                }
            }
        }
    }

    private fun loadReviewPreview() {
        viewModelScope.launch {
            movieRepository.getMovieReviews(movieId, page = 1).collect { result ->
                if (result is Result.Success) {
                    updateState {
                        copy(
                            previewReviews = result.data.items.take(3),
                            totalReviewCount = result.data.items.size +
                                ((result.data.totalPages - 1) * 20),
                        )
                    }
                }
            }
        }
    }

    private fun loadDetail() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getMovieDetail(movieId).collect { result ->
                when (result) {
                    Result.Loading -> updateState {
                        copy(isLoading = movie == null, error = null, isRefreshing = false)
                    }
                    is Result.Success -> {
                        val movie = result.data ?: return@collect
                        updateState {
                            copy(isLoading = false, movie = movie, error = null, isRefreshing = result.isRefreshing)
                        }
                        // Only extract palette once — accentArgb is null until first successful extraction
                        if (uiState.value.accentArgb == null) {
                            extractPalette(movie.posterUrl ?: movie.backdropUrl)
                        }
                    }
                    is Result.Error -> {
                        if (uiState.value.movie != null) {
                            // Cached detail already visible — surface an event, keep the screen
                            updateState { copy(isRefreshing = false) }
                            sendEvent(DetailEvent.ShowError(UiText.StringResource(R.string.error_refresh_cached)))
                        } else {
                            updateState {
                                copy(isLoading = false, isRefreshing = false, error = UiText.StringResource(CoreUiR.string.error_something_went_wrong))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun extractPalette(url: String?) {
        if (url == null) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .allowHardware(false)
                    .size(300)
                    .build()
                val result = context.imageLoader.execute(request)
                if (result is SuccessResult) {
                    val bitmap = (result.drawable as? BitmapDrawable)?.bitmap ?: return@launch
                    val palette = Palette.from(bitmap).generate()
                    val swatch = palette.vibrantSwatch
                        ?: palette.darkVibrantSwatch
                        ?: palette.dominantSwatch
                        ?: palette.mutedSwatch
                    swatch?.rgb?.let { rgb ->
                        if (contrastOnDark(rgb) >= MIN_CONTRAST_RATIO) {
                            updateState { copy(accentArgb = rgb) }
                        }
                        // Below threshold: accentArgb stays null → GoldDefault is used
                    }
                }
            } catch (_: Exception) {
                // Palette extraction is best-effort; silently ignore failures
            }
        }
    }
}

// WCAG AA for large text / UI components. The accent renders on the app's near-black
// dark background (luminance ≈ 0.002), so anything below 3:1 is barely distinguishable.
private const val MIN_CONTRAST_RATIO = 3.0

// Approximate luminance of the app's dark background (#0D0D0D ≈ 0.002).
private const val DARK_BG_LUMINANCE = 0.002

private fun contrastOnDark(argb: Int): Double {
    val r = ((argb shr 16) and 0xFF) / 255.0
    val g = ((argb shr 8) and 0xFF) / 255.0
    val b = (argb and 0xFF) / 255.0
    fun linearize(c: Double) = if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    val luminance = 0.2126 * linearize(r) + 0.7152 * linearize(g) + 0.0722 * linearize(b)
    val lighter = maxOf(luminance, DARK_BG_LUMINANCE)
    val darker = minOf(luminance, DARK_BG_LUMINANCE)
    return (lighter + 0.05) / (darker + 0.05)
}
