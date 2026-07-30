package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.domain.repository.RatingRepository
import javax.inject.Inject

class RefreshRatingsUseCase @Inject constructor(private val repository: RatingRepository) {
    suspend operator fun invoke() = repository.refreshRatings()
}
