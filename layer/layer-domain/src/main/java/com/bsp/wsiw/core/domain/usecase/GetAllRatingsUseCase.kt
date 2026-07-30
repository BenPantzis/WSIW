package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.domain.repository.RatingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllRatingsUseCase @Inject constructor(private val repository: RatingRepository) {
    operator fun invoke(): Flow<Map<Int, Float>> = repository.getAllRatings()
}
