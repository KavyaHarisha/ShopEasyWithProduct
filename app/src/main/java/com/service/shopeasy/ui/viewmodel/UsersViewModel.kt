package com.service.shopeasy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.service.shopeasy.data.repository.UserRepository
import com.service.shopeasy.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UsersIntent {
    object LoadUsers : UsersIntent
    object Refresh: UsersIntent
}

data class UserState(
    val loading: Boolean = false,
    val users: List<User> =  emptyList(),
    val error: String? = null
)

@HiltViewModel
class UsersViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {
    private val _state = MutableStateFlow(UserState())
    val state: StateFlow<UserState> = _state

    fun onUserIntent(intent: UsersIntent) {
        when (intent) {
            is UsersIntent.LoadUsers -> {loadUsers()}
            is UsersIntent.Refresh -> {loadUsers()}

        }
    }

    fun loadUsers() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        try {
            _state.value = _state.value.copy(loading = false, users = userRepository.getUsers())
        } catch (t: Throwable) {
            _state.value = _state.value.copy(loading = false, error = t.message ?: "Unknown error")
        }
    }

}