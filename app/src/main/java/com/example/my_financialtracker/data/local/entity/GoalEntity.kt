package com.example.my_financialtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val targetAmountLkr: Double,
    val currentSavedLkr: Double,
    val monthlyContributionLkr: Double,
    val contributionDayOfMonth: Int,
    val contributionSource: String,
    val allowEmergencyUse: Boolean,
    val emergencyUsedLkr: Double,
    val lastContributionAt: Long,
    val deadlineAt: Long,
    val createdAt: Long,
)
