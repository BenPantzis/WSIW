package com.bsp.wsiw.core.testing

import com.bsp.wsiw.core.common.AppDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("FunctionNaming")
fun TestAppDispatchers(
    dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
): AppDispatchers = AppDispatchers(
    io = dispatcher,
    default = dispatcher,
    main = dispatcher,
)
