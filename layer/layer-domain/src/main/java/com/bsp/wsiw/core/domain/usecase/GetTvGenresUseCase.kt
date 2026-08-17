package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.model.Genre
import com.bsp.wsiw.core.domain.repository.TvRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTvGenresUseCase @Inject constructor(
    private val repository: TvRepository,
) {
    operator fun invoke(): Flow<Result<List<Genre>>> = repository.getTvGenres()
}
