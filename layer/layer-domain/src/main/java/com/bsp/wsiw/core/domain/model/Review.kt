package com.bsp.wsiw.core.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Review(
    val id: String,
    val author: String,
    val avatarUrl: String?,
    val rating: Float?,
    val content: String,
    val createdAt: String,
)
