package com.example.my_financialtracker.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.my_financialtracker.R
import com.example.my_financialtracker.ui.components.AppScaffold
import com.example.my_financialtracker.ui.components.EmptyStateCard
import com.example.my_financialtracker.ui.components.GradientHeroCard
import com.example.my_financialtracker.ui.components.HorizontalBarChartCard
import com.example.my_financialtracker.ui.components.MetricCard
import com.example.my_financialtracker.ui.components.PieChartCard
import com.example.my_financialtracker.ui.components.QuickActionCard
import com.example.my_financialtracker.ui.components.TransactionHighlightRow
import com.example.my_financialtracker.ui.state.DashboardUiState

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onAddIncomeClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onGoalClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onBottomNavClick: (String) -> Unit,
    currentRoute: String,
) {
    val incomeSummary = uiState.summaryCards.getOrNull(0)
    val expenseSummary = uiState.summaryCards.getOrNull(1)
    val freeCashSummary = uiState.summaryCards.getOrNull(2)

    AppScaffold(
        title = stringResource(R.string.dashboard_title),
        currentRoute = currentRoute,
        showBottomBar = true,
        onBottomNavClick = onBottomNavClick,
        onAddIncomeClick = onAddIncomeClick,
        onAddExpenseClick = onAddExpenseClick,
        showTopBar = false,
    ) { modifier ->
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                GradientHeroCard(
                    eyebrow = "STATISTICS",
                    title = "Spending Analysis",
                    amount = incomeSummary?.amountLabel ?: "LKR 0.00",
                    subtitle = expenseSummary?.let { "Spent: ${it.amountLabel}" } ?: "Spent: LKR 0.00",
                    modifier = Modifier.fillMaxWidth(),
                    accent = {
                        AssistChip(
                            onClick = {},
                            label = { Text("This Month") },
                            leadingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                        )
                    },
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MetricCard(
                        title = "Income Recorded",
                        amount = incomeSummary?.amountLabel ?: "LKR 0.00",
                        modifier = Modifier.weight(1f),
                    )
                    MetricCard(
                        title = "Total Saved",
                        amount = uiState.featuredGoal?.currentSavedLabel ?: "LKR 0.00",
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MetricCard(
                        title = "Spent Amount",
                        amount = expenseSummary?.amountLabel ?: "LKR 0.00",
                        modifier = Modifier.weight(1f),
                    )
                    MetricCard(
                        title = "Free Cash",
                        amount = freeCashSummary?.amountLabel ?: "LKR 0.00",
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                Text(
                    text = "SPENDING TREND",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4F46E5),
                )
            }

            item {
                HorizontalBarChartCard(
                    title = "Income vs expenses",
                    items = listOfNotNull(
                        incomeSummary?.let { com.example.my_financialtracker.model.ChartDatum("Income", 1.0, it.amountLabel) },
                        expenseSummary?.let { com.example.my_financialtracker.model.ChartDatum("Expenses", 1.0, it.amountLabel) },
                    ),
                )
            }

            item {
                Text(
                    text = "CATEGORY BREAKDOWN",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4F46E5),
                )
            }

            item {
                PieChartCard(
                    title = "How your spending is split",
                    items = uiState.expenseChart,
                )
            }

            item {
                Text(
                    text = "INSIGHTS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4F46E5),
                )
            }

            item {
                QuickActionCard(
                    title = "This month",
                    description = uiState.spendVsLeftMessage.ifBlank { "Your latest balance between income and spending." },
                    icon = Icons.Outlined.Analytics,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onTransactionsClick,
                )
            }

            if (uiState.recentTransactions.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No transactions yet",
                        subtitle = "Add your first income or expense to start building your financial picture.",
                    )
                }
            } else {
                item {
                    Text(
                        text = "RECENT ACTIVITY",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4F46E5),
                    )
                }
                items(uiState.recentTransactions.take(4)) { item ->
                    TransactionHighlightRow(item = item)
                }
            }
        }
    }
}
