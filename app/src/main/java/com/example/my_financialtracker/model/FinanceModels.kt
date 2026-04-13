package com.example.my_financialtracker.model

data class SummaryCard(
    val title: String,
    val amountLabel: String,
    val description: String,
)

data class TransactionItem(
    val id: String,
    val type: TransactionType,
    val title: String,
    val amountLabel: String,
    val meta: String,
    val originalAmount: Double,
    val originalCurrency: String,
    val spendingType: String? = null,
    val recurrenceType: String? = null,
    val paymentMethod: String? = null,
    val note: String = "",
)

data class GoalOverview(
    val id: String,
    val title: String,
    val targetAmountLabel: String,
    val currentSavedLabel: String,
    val remainingAmountLabel: String,
    val deadlineLabel: String,
    val monthlyNeedLabel: String,
    val monthlyContributionLabel: String,
    val contributionScheduleLabel: String,
    val emergencyUseLabel: String,
    val emergencyUsedLabel: String,
    val targetAmountLkr: Double,
    val currentSavedLkr: Double,
    val monthlyContributionLkr: Double,
    val contributionDayOfMonth: Int,
    val monthsRemaining: Int,
    val allowEmergencyUse: Boolean,
    val progress: Float,
    val isCompleted: Boolean,
)

enum class TransactionType {
    INCOME,
    EXPENSE,
    GOAL_TRANSFER,
}

data class ChartDatum(
    val label: String,
    val value: Double,
    val valueLabel: String,
)
