package com.example.my_financialtracker.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_financialtracker.data.AppContainer
import com.example.my_financialtracker.model.AppDefaults
import com.example.my_financialtracker.repository.AuthRepository
import com.example.my_financialtracker.repository.local.LocalFinanceRepository
import com.example.my_financialtracker.ui.state.AuthUiState
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuthViewModel(
    private val repository: AuthRepository = AppContainer.authRepository,
) : ViewModel() {
    private companion object {
        const val TAG = "AuthViewModel"
    }

    private val syncService = AppContainer.firestoreSyncService
    private val localFinanceRepository = AppContainer.financeRepository as? LocalFinanceRepository
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateName(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null) }
    }

    fun updateEmail(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun updatePassword(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.login(state.email, state.password)
                .onSuccess { user ->
                    runCatching { syncService.syncUserData(user.uid) }
                        .onFailure { Log.w(TAG, "Initial sync after login failed", it) }
                    runCatching { localFinanceRepository?.cleanupLegacyDemoData() }
                        .onFailure { Log.w(TAG, "Legacy data cleanup after login failed", it) }
                    _uiState.update { current -> current.copy(isLoading = false) }
                    onSuccess()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: AppDefaults.ERROR_SIGN_IN,
                        )
                    }
                }
        }
    }

    fun register(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.register(state.name, state.email, state.password)
                .onSuccess { user ->
                    runCatching { syncService.syncUserData(user.uid) }
                        .onFailure { Log.w(TAG, "Initial sync after registration failed", it) }
                    runCatching { localFinanceRepository?.cleanupLegacyDemoData() }
                        .onFailure { Log.w(TAG, "Legacy data cleanup after registration failed", it) }
                    _uiState.update { current -> current.copy(isLoading = false) }
                    onSuccess()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: AppDefaults.ERROR_REGISTER,
                        )
                    }
                }
        }
    }
}
