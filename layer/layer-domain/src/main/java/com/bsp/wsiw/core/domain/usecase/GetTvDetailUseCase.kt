package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.common.AppDispatchers
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.FlowUseCase
import com.bsp.wsiw.core.domain.model.TvShowDetail
import com.bsp.wsiw.core.domain.repository.TvRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTvDetailUseCase @Inject constructor(
    private val repository: TvRepository,
    dispatchers: AppDispatchers,
) : FlowUseCase<Int, TvShowDetail>(dispatchers) {
    override fun execute(params: Int): Flow<Result<TvShowDetail>> =
        repository.getTvDetail(seriesId = params)
}
