package com.bsp.wsiw.core.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profileUrl: String?,
)
