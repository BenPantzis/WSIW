package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.common.AppDispatchers
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.FlowUseCase
import com.bsp.wsiw.core.domain.model.DiscoverFilter
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.model.PagedResult
import com.bsp.wsiw.core.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DiscoverMoviesUseCase @Inject constructor(
    private val repository: MovieRepository,
    dispatchers: AppDispatchers,
) : FlowUseCase<DiscoverMoviesUseCase.Params, PagedResult<Movie>>(dispatchers) {

    data class Params(val genreId: Int?, val filter: DiscoverFilter, val page: Int = 1)

    override fun execute(params: Params): Flow<Result<PagedResult<Movie>>> =
        repository.discoverMovies(genreId = params.genreId, filter = params.filter, page = params.page)
}
