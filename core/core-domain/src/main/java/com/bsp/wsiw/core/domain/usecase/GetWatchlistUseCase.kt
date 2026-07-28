package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWatchlistUseCase @Inject constructor(
    private val repository: WatchlistRepository,
) {
    operator fun invoke(): Flow<List<Movie>> = repository.getWatchlist()
}
