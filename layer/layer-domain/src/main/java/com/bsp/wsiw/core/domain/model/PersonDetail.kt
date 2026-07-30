package com.bsp.wsiw.core.domain.model

data class PersonDetail(
    val id: Int,
    val name: String,
    val biography: String,
    val birthday: String?,
    val placeOfBirth: String?,
    val profileUrl: String?,
    val knownForDepartment: String,
    val filmography: List<Movie>,
)
