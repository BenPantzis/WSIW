package com.bsp.wsiw.feature.detail

import com.bsp.wsiw.core.domain.model.PersonDetail

sealed interface PersonAction {
    data object Retry : PersonAction
}

data class PersonUiState(
    val isLoading: Boolean = true,
    val person: PersonDetail? = null,
    val error: String? = null,
)
