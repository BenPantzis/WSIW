package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.common.AppDispatchers
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.FlowUseCase
import com.bsp.wsiw.core.domain.model.DiscoverFilter
import com.bsp.wsiw.core.domain.model.PagedResult
import com.bsp.wsiw.core.domain.model.TvShow
import com.bsp.wsiw.core.domain.repository.TvRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DiscoverTvUseCase @Inject constructor(
    private val repository: TvRepository,
    dispatchers: AppDispatchers,
) : FlowUseCase<DiscoverTvUseCase.Params, PagedResult<TvShow>>(dispatchers) {

    data class Params(val genreId: Int?, val filter: DiscoverFilter, val page: Int = 1)

    override fun execute(params: Params): Flow<Result<PagedResult<TvShow>>> =
        repository.discoverTv(genreId = params.genreId, filter = params.filter, page = params.page)
}
