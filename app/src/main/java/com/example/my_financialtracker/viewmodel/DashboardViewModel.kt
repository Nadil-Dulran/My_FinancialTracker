package com.example.my_financialtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_financialtracker.data.AppContainer
import com.example.my_financialtracker.model.ChartDatum
import com.example.my_financialtracker.model.GoalOverview
import com.example.my_financialtracker.model.InsightItem
import com.example.my_financialtracker.model.SummaryCard
import com.example.my_financialtracker.model.TransactionItem
import com.example.my_financialtracker.repository.FinanceRepository
import com.example.my_financialtracker.repository.GoalRepository
import com.example.my_financialtracker.repository.local.LocalFinanceRepository
import com.example.my_financialtracker.ui.state.DashboardUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    repository: FinanceRepository = AppContainer.financeRepository,
    goalRepository: GoalRepository = AppContainer.goalRepository,
) : ViewModel() {
    private val localRepository = repository as? LocalFinanceRepository

    init {
        viewModelScope.launch {
            runCatching { goalRepository.refreshGoalContributionsIfNeeded() }
        }
    }

    private data class DashboardBundle(
        val summary: List<SummaryCard>,
        val transactions: List<TransactionItem>,
        val insights: List<InsightItem>,
        val expenseChart: List<ChartDatum>,
        val incomeChart: List<ChartDatum>,
        val spendingSplitChart: List<ChartDatum>,
        val spendVsLeftChart: List<ChartDatum>,
        val spendVsLeftMessage: String,
    )

    private val summaryAndTransactions = combine(
        repository.observeDashboardSummary(),
        repository.observeRecentTransactions(),
    ) { summary, transactions ->
        summary to transactions
    }

    private val summaryTransactionsInsights = combine(
        summaryAndTransactions,
        localRepository?.observeInsights() ?: flowOf(emptyList()),
    ) { (summary, transactions), insights ->
        Triple(summary, transactions, insights)
    }

    private val dashboardBundle = combine(
        summaryTransactionsInsights,
        repository.observeExpenseChart(),
        repository.observeIncomeChart(),
        localRepository?.observeSpendingSplitChart() ?: flowOf(emptyList()),
    ) { triple, expenseChart, incomeChart, spendingSplitChart ->
        DashboardBundle(
            summary = triple.first,
            transactions = triple.second,
            insights = triple.third,
            expenseChart = expenseChart,
            incomeChart = incomeChart,
            spendingSplitChart = spendingSplitChart,
            spendVsLeftChart = emptyList(),
            spendVsLeftMessage = "",
        )
    }

    private val dashboardBundleWithSpendState = combine(
        dashboardBundle,
        localRepository?.observeSpendVsLeftChart() ?: flowOf(emptyList()),
        localRepository?.observeSpendVsLeftMessage() ?: flowOf(""),
    ) { bundle, spendVsLeftChart, spendVsLeftMessage ->
        bundle.copy(
            spendVsLeftChart = spendVsLeftChart,
            spendVsLeftMessage = spendVsLeftMessage,
        )
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        dashboardBundleWithSpendState,
        goalRepository.observePrimaryGoal(),
    ) { bundle: DashboardBundle, featuredGoal: GoalOverview? ->
        DashboardUiState(
            summaryCards = bundle.summary,
            insightItems = bundle.insights,
            expenseChart = bundle.expenseChart,
            incomeChart = bundle.incomeChart,
            spendingSplitChart = bundle.spendingSplitChart,
            spendVsLeftChart = bundle.spendVsLeftChart,
            spendVsLeftMessage = bundle.spendVsLeftMessage,
            featuredGoal = featuredGoal,
            recentTransactions = bundle.transactions,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(),
    )
}
