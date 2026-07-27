package com.bsp.wsiw.core.domain

import com.bsp.wsiw.core.common.AppDispatchers
import com.bsp.wsiw.core.common.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

abstract class FlowUseCase<in P, out T>(private val dispatchers: AppDispatchers) {
    operator fun invoke(params: P): Flow<Result<T>> = execute(params).flowOn(dispatchers.io)
    protected abstract fun execute(params: P): Flow<Result<T>>
}
