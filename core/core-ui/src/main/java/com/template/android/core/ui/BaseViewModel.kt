package com.template.android.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<Action, Event, UiState>(
    initialState: UiState,
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    // extraBufferCapacity prevents tryEmit from dropping on rapid UI interactions
    private val _actions = MutableSharedFlow<Action>(extraBufferCapacity = 64)

    init {
        viewModelScope.launch {
            _actions.collect(::handleAction)
        }
    }

    fun onAction(action: Action) {
        _actions.tryEmit(action)
    }

    protected abstract fun handleAction(action: Action)

    protected fun updateState(reducer: UiState.() -> UiState) {
        _uiState.update(reducer)
    }

    protected fun sendEvent(event: Event) {
        viewModelScope.launch {
            _events.send(event)
        }
    }
}
