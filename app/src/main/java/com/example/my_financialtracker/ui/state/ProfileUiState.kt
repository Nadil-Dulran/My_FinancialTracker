package com.example.my_financialtracker.ui.state

data class ProfileUiState(
    val displayName: String = "User",
    val email: String = "",
    val preferredCurrency: String = "LKR",
    val notificationCaptureEnabled: Boolean = false,
)
