package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.common.AppDispatchers
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.FlowUseCase
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPopularMoviesUseCase @Inject constructor(
    private val repository: MovieRepository,
    dispatchers: AppDispatchers,
) : FlowUseCase<GetPopularMoviesUseCase.Params, List<Movie>>(dispatchers) {

    data class Params(val page: Int, val forceRefresh: Boolean = false)

    override fun execute(params: Params): Flow<Result<List<Movie>>> =
        repository.getPopularMovies(page = params.page, forceRefresh = params.forceRefresh)
}
