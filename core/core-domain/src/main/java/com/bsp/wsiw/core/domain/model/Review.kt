package com.bsp.wsiw.core.domain.model

data class Review(
    val id: String,
    val author: String,
    val avatarUrl: String?,
    val rating: Float?,
    val content: String,
    val createdAt: String,
)
