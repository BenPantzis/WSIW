package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.domain.repository.WatchlistRepository
import javax.inject.Inject

class RefreshWatchlistUseCase @Inject constructor(
    private val repository: WatchlistRepository,
) {
    suspend operator fun invoke() = repository.refreshWatchlist()
}
