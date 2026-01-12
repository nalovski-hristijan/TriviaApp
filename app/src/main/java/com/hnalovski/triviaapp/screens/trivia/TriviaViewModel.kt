package com.hnalovski.triviaapp.screens.trivia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnalovski.triviaapp.model.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class TriviaViewModel: ViewModel() {

    private val mutableState: MutableStateFlow<ViewState> = MutableStateFlow(ViewState(isLoading = false))
    val state = mutableState
        .onStart {

        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ViewState(isLoading = false)
        )




    data class ViewState(
        val model: ViewState? = null,
        val error: String? = null,
        val isLoading: Boolean = false
    )

    data class ViewModel(
        val question: Question
    )
}