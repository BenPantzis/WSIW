package com.template.android.core.domain

import com.template.android.core.common.AppDispatchers
import kotlinx.coroutines.withContext

abstract class UseCase<in P, out R>(private val dispatchers: AppDispatchers) {
    suspend operator fun invoke(params: P): R = withContext(dispatchers.io) {
        execute(params)
    }

    protected abstract suspend fun execute(params: P): R
}

// Pass to use cases that take no input: invoke(NoParams)
object NoParams
