package com.bsp.wsiw.core.domain.model

data class PersonSummary(
    val id: Int,
    val name: String,
    val profileUrl: String?,
    val knownForDepartment: String?,
)
