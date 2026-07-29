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
import com.bsp.wsiw.core.domain.usecase.GetMovieDetailUseCase
import com.bsp.wsiw.core.domain.usecase.IsWatchlistedUseCase
import com.bsp.wsiw.core.domain.usecase.ToggleWatchlistUseCase
import com.bsp.wsiw.core.ui.BaseViewModel
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
    private val movieRepository: MovieRepository,
    @ApplicationContext private val context: Context,
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
        viewModelScope.launch {
            isWatchlisted(movieId).collect { watchlisted ->
                updateState { copy(isWatchlisted = watchlisted) }
            }
        }
    }

    override fun handleAction(action: DetailAction) {
        when (action) {
            DetailAction.Retry -> loadDetail()
            DetailAction.ToggleWatchlist -> {
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
                            sendEvent(DetailEvent.ShowError("Couldn't refresh — showing cached data"))
                        } else {
                            updateState {
                                copy(isLoading = false, isRefreshing = false, error = result.exception?.message ?: "Something went wrong")
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
