package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.model.Genre
import com.bsp.wsiw.core.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGenresUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    operator fun invoke(): Flow<Result<List<Genre>>> = repository.getGenres()
}
