package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.repository.WatchlistRepository
import javax.inject.Inject

class ToggleWatchlistUseCase @Inject constructor(
    private val repository: WatchlistRepository,
) {
    suspend operator fun invoke(movie: Movie, isWatchlisted: Boolean) {
        if (isWatchlisted) {
            repository.removeFromWatchlist(movie.id)
        } else {
            repository.addToWatchlist(movie)
        }
    }
}
