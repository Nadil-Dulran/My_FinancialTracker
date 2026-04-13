package com.example.my_financialtracker.repository

import com.example.my_financialtracker.model.GoalOverview
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observePrimaryGoal(): Flow<GoalOverview?>
    fun observeGoals(): Flow<List<GoalOverview>>
    suspend fun addGoal(
        title: String,
        targetAmount: Double,
        currentSaved: Double,
        monthsToDeadline: Int,
        monthlyContribution: Double,
        contributionDayOfMonth: Int,
        allowEmergencyUse: Boolean,
    ): Result<Unit>
    suspend fun applyEmergencyWithdrawal(goalId: String, amount: Double): Result<Unit>
    suspend fun refreshGoalContributionsIfNeeded()
    suspend fun seedDemoGoalIfNeeded()
}
