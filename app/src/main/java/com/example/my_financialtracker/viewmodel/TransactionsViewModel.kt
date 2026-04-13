package com.example.my_financialtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_financialtracker.data.AppContainer
import com.example.my_financialtracker.model.AppDefaults
import com.example.my_financialtracker.model.DetectedTransactionItem
import com.example.my_financialtracker.model.InsightItem
import com.example.my_financialtracker.model.TransactionItem
import com.example.my_financialtracker.repository.FinanceRepository
import com.example.my_financialtracker.repository.local.LocalFinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransactionsViewModel(
    private val repository: FinanceRepository = AppContainer.financeRepository,
) : ViewModel() {
    private val localRepository = repository as? LocalFinanceRepository

    init {
        viewModelScope.launch {
            runCatching { localRepository?.cleanupLegacyDemoData() }
                .onFailure { _message.value = it.message ?: "Could not clean old demo records." }
        }
    }

    val transactions: StateFlow<List<TransactionItem>> = repository.observeRecentTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val detectedTransactions: StateFlow<List<DetectedTransactionItem>> = repository.observeDetectedTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val insights: StateFlow<List<InsightItem>> = (localRepository?.observeInsights() ?: flowOf(emptyList()))
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val spendingStatus: StateFlow<String> = (localRepository?.observeSpendVsLeftMessage() ?: flowOf(""))
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "",
        )

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun updateTransaction(transaction: TransactionItem) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
                .onSuccess { _message.value = AppDefaults.SUCCESS_TRANSACTION_UPDATED }
                .onFailure { _message.value = it.message ?: AppDefaults.ERROR_TRANSACTION_UPDATE }
        }
    }

    fun deleteTransaction(transaction: TransactionItem) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
                .onSuccess { _message.value = AppDefaults.SUCCESS_TRANSACTION_DELETED }
                .onFailure { _message.value = it.message ?: AppDefaults.ERROR_TRANSACTION_DELETE }
        }
    }

    fun consumeMessage() {
        _message.update { null }
    }

    fun confirmDetectedTransaction(
        id: String,
        chosenType: String,
        chosenCategoryOrSource: String,
        chosenSpendingType: String,
        note: String,
    ) {
        viewModelScope.launch {
            repository.confirmDetectedTransaction(
                id = id,
                chosenType = chosenType,
                chosenCategoryOrSource = chosenCategoryOrSource,
                chosenSpendingType = chosenSpendingType,
                note = note,
            ).onSuccess {
                _message.value = "Detected transaction confirmed."
            }.onFailure {
                _message.value = it.message ?: "Could not confirm detected transaction."
            }
        }
    }

    fun ignoreDetectedTransaction(id: String) {
        viewModelScope.launch {
            repository.ignoreDetectedTransaction(id)
                .onSuccess { _message.value = "Detected transaction ignored." }
                .onFailure { _message.value = it.message ?: "Could not ignore detected transaction." }
        }
    }
}
