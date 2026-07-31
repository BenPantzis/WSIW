package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.repository.RatingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLocalRatedMoviesUseCase @Inject constructor(private val repository: RatingRepository) {
    operator fun invoke(): Flow<List<Pair<Movie, Float>>> = repository.getLocalRatedMovies()
}
