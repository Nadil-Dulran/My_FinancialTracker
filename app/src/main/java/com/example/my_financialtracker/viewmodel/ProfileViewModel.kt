package com.example.my_financialtracker.viewmodel

import android.app.Application
import android.provider.Settings
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_financialtracker.data.AppContainer
import com.example.my_financialtracker.ui.state.ProfileUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ProfileViewModel : ViewModel() {
    private val auth = AppContainer.firebaseAuth
    private val preferences = AppContainer.userPreferencesRepository
    private val context = AppContainer.appContext

    val uiState: StateFlow<ProfileUiState> = preferences.preferredCurrency
        .map { currency ->
            val user = auth.currentUser
            ProfileUiState(
                displayName = user?.displayName ?: user?.email ?: "User",
                email = user?.email.orEmpty(),
                preferredCurrency = currency,
                notificationCaptureEnabled = isNotificationListenerEnabled(),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileUiState(),
        )

    fun signOut() {
        auth.signOut()
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ).orEmpty()
        return enabled.contains(context.packageName, ignoreCase = true)
    }
}
