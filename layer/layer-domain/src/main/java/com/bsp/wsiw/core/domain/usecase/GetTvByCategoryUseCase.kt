package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.common.AppDispatchers
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.FlowUseCase
import com.bsp.wsiw.core.domain.model.PagedResult
import com.bsp.wsiw.core.domain.model.TvShow
import com.bsp.wsiw.core.domain.repository.TvRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTvByCategoryUseCase @Inject constructor(
    private val repository: TvRepository,
    dispatchers: AppDispatchers,
) : FlowUseCase<GetTvByCategoryUseCase.Params, PagedResult<TvShow>>(dispatchers) {

    data class Params(val category: String, val page: Int = 1)

    override fun execute(params: Params): Flow<Result<PagedResult<TvShow>>> =
        repository.getTvByCategory(category = params.category, page = params.page)
}
