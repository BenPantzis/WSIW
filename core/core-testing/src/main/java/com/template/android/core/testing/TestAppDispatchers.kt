package com.template.android.core.testing

import com.template.android.core.common.AppDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
fun TestAppDispatchers(
    dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
): AppDispatchers = AppDispatchers(
    io = dispatcher,
    default = dispatcher,
    main = dispatcher,
)
