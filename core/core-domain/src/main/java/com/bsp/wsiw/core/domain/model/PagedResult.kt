package com.bsp.wsiw.core.domain.model

data class PagedResult<T>(val items: List<T>, val totalPages: Int)
