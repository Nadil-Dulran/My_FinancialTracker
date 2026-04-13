package com.example.my_financialtracker.data

import android.app.Application
import android.content.Context
import com.example.my_financialtracker.data.local.FinanceDatabase
import com.example.my_financialtracker.data.preferences.UserPreferencesRepository
import com.example.my_financialtracker.data.currency.ExchangeRateRepository
import com.example.my_financialtracker.data.remote.FirestoreSyncService
import com.example.my_financialtracker.data.storage.FileStorageManager
import com.example.my_financialtracker.repository.AuthRepository
import com.example.my_financialtracker.repository.FinanceRepository
import com.example.my_financialtracker.repository.GoalRepository
import com.example.my_financialtracker.repository.firebase.FirebaseAuthRepository
import com.example.my_financialtracker.repository.local.LocalFinanceRepository
import com.example.my_financialtracker.repository.local.LocalGoalRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore

object AppContainer {
    lateinit var appContext: Context
        private set
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initialize(application: Application) {
        appContext = application.applicationContext
        applicationScope.launch {
            financeRepository.seedDemoDataIfNeeded()
            financeRepository.refreshRecurringExpensesIfNeeded()
            goalRepository.refreshGoalContributionsIfNeeded()
            goalRepository.seedDemoGoalIfNeeded()
            exchangeRateRepository.refreshRatesIfNeeded()
        }
    }

    val authRepository: AuthRepository by lazy { FirebaseAuthRepository() }

    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val database: FinanceDatabase by lazy {
        FinanceDatabase.getInstance(appContext)
    }

    val financeRepository: FinanceRepository by lazy {
        LocalFinanceRepository(
            incomeDao = database.incomeDao(),
            expenseDao = database.expenseDao(),
            goalDao = database.goalDao(),
            detectedTransactionDao = database.detectedTransactionDao(),
            userPreferencesRepository = userPreferencesRepository,
            exchangeRateRepository = exchangeRateRepository,
            firebaseAuth = firebaseAuth,
            syncService = firestoreSyncService,
        )
    }

    val goalRepository: GoalRepository by lazy {
        LocalGoalRepository(
            goalDao = database.goalDao(),
            userPreferencesRepository = userPreferencesRepository,
            exchangeRateRepository = exchangeRateRepository,
            firebaseAuth = firebaseAuth,
            syncService = firestoreSyncService,
        )
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(appContext)
    }

    val exchangeRateRepository: ExchangeRateRepository by lazy {
        ExchangeRateRepository(userPreferencesRepository)
    }

    val fileStorageManager: FileStorageManager by lazy {
        FileStorageManager(appContext)
    }

    val firestoreSyncService: FirestoreSyncService by lazy {
        FirestoreSyncService(
            firestore = firestore,
            incomeDao = database.incomeDao(),
            expenseDao = database.expenseDao(),
            goalDao = database.goalDao(),
        )
    }
}
