package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.common.AppDispatchers
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.FlowUseCase
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.model.PagedResult
import com.bsp.wsiw.core.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTrendingMoviesUseCase @Inject constructor(
    private val repository: MovieRepository,
    dispatchers: AppDispatchers,
) : FlowUseCase<Int, PagedResult<Movie>>(dispatchers) {
    override fun execute(params: Int): Flow<Result<PagedResult<Movie>>> =
        repository.getTrendingMovies(params)
}
