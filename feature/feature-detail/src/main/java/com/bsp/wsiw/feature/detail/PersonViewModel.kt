package com.bsp.wsiw.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.repository.MovieRepository
import com.bsp.wsiw.core.ui.BaseViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch

class PersonViewModel @AssistedInject constructor(
    @Assisted private val personId: Int,
    private val repository: MovieRepository,
) : BaseViewModel<PersonAction, Nothing, PersonUiState>(
    initialState = PersonUiState(),
) {
    @AssistedFactory
    interface Factory {
        fun create(personId: Int): PersonViewModel
    }

    init {
        load()
    }

    override fun handleAction(action: PersonAction) {
        when (action) {
            PersonAction.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            repository.getPersonDetail(personId).collect { result ->
                when (result) {
                    is Result.Success -> updateState { copy(isLoading = false, person = result.data) }
                    is Result.Error -> updateState {
                        copy(isLoading = false, error = result.exception?.message ?: "Something went wrong")
                    }
                    Result.Loading -> Unit
                }
            }
        }
    }
}
