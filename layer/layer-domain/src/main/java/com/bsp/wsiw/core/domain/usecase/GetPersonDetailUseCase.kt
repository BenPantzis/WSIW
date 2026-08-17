package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.common.AppDispatchers
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.FlowUseCase
import com.bsp.wsiw.core.domain.model.PersonDetail
import com.bsp.wsiw.core.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPersonDetailUseCase @Inject constructor(
    private val repository: MovieRepository,
    dispatchers: AppDispatchers,
) : FlowUseCase<Int, PersonDetail>(dispatchers) {
    override fun execute(params: Int): Flow<Result<PersonDetail>> =
        repository.getPersonDetail(params)
}
