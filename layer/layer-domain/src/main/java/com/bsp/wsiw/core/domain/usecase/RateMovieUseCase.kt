package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.domain.repository.RatingRepository
import javax.inject.Inject

class RateMovieUseCase @Inject constructor(private val repository: RatingRepository) {
    suspend operator fun invoke(movieId: Int, rating: Float) = repository.rateMovie(movieId, rating)
    suspend fun remove(movieId: Int) = repository.removeRating(movieId)
}
