package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.domain.repository.RatingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMovieRatingUseCase @Inject constructor(private val repository: RatingRepository) {
    operator fun invoke(movieId: Int): Flow<Float?> = repository.getMovieRating(movieId)
}
