package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsWatchlistedUseCase @Inject constructor(
    private val repository: WatchlistRepository,
) {
    operator fun invoke(movieId: Int): Flow<Boolean> = repository.isWatchlisted(movieId)
}
