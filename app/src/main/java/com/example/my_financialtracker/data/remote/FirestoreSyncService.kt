package com.example.my_financialtracker.data.remote

import com.example.my_financialtracker.data.local.dao.ExpenseDao
import com.example.my_financialtracker.data.local.dao.GoalDao
import com.example.my_financialtracker.data.local.dao.IncomeDao
import com.example.my_financialtracker.data.local.entity.ExpenseEntity
import com.example.my_financialtracker.data.local.entity.GoalEntity
import com.example.my_financialtracker.data.local.entity.IncomeEntity
import com.example.my_financialtracker.data.remote.model.FirestoreExpense
import com.example.my_financialtracker.data.remote.model.FirestoreGoal
import com.example.my_financialtracker.data.remote.model.FirestoreIncome
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreSyncService(
    private val firestore: FirebaseFirestore,
    private val incomeDao: IncomeDao,
    private val expenseDao: ExpenseDao,
    private val goalDao: GoalDao,
) {

    suspend fun syncUserData(userId: String) {
        syncIncome(userId)
        syncExpenses(userId)
        syncGoals(userId)
    }

    suspend fun pushIncome(userId: String, entity: IncomeEntity) {
        userCollection(userId)
            .collection(COLLECTION_INCOME)
            .document(entity.id)
            .set(entity.toFirestore())
            .await()
    }

    suspend fun pushExpense(userId: String, entity: ExpenseEntity) {
        userCollection(userId)
            .collection(COLLECTION_EXPENSES)
            .document(entity.id)
            .set(entity.toFirestore())
            .await()
    }

    suspend fun pushGoal(userId: String, goal: GoalEntity) {
        userCollection(userId)
            .collection(COLLECTION_GOALS)
            .document(goal.id)
            .set(goal.toFirestore())
            .await()
    }

    suspend fun deleteIncome(userId: String, incomeId: String) {
        userCollection(userId)
            .collection(COLLECTION_INCOME)
            .document(incomeId)
            .delete()
            .await()
    }

    suspend fun deleteExpense(userId: String, expenseId: String) {
        userCollection(userId)
            .collection(COLLECTION_EXPENSES)
            .document(expenseId)
            .delete()
            .await()
    }

    private suspend fun syncIncome(userId: String) {
        val collection = userCollection(userId).collection(COLLECTION_INCOME)
        val remote = collection.get().await().documents.mapNotNull { doc ->
            doc.toObject(FirestoreIncome::class.java)?.copy(id = doc.id)
        }

        if (remote.isEmpty()) {
            incomeDao.getAll().forEach { entity ->
                collection.document(entity.id).set(entity.toFirestore()).await()
            }
        } else {
            incomeDao.upsertAll(remote.map { it.toEntity() })
        }
    }

    private suspend fun syncExpenses(userId: String) {
        val collection = userCollection(userId).collection(COLLECTION_EXPENSES)
        val remote = collection.get().await().documents.mapNotNull { doc ->
            doc.toObject(FirestoreExpense::class.java)?.copy(id = doc.id)
        }

        if (remote.isEmpty()) {
            expenseDao.getAll().forEach { entity ->
                collection.document(entity.id).set(entity.toFirestore()).await()
            }
        } else {
            expenseDao.upsertAll(remote.map { it.toEntity() })
        }
    }

    private suspend fun syncGoals(userId: String) {
        val collection = userCollection(userId).collection(COLLECTION_GOALS)
        val remote = collection.get().await().documents.mapNotNull { doc ->
            doc.toObject(FirestoreGoal::class.java)?.copy(id = doc.id)
        }

        if (remote.isEmpty()) {
            goalDao.getAllGoals().forEach { goal ->
                collection.document(goal.id).set(goal.toFirestore()).await()
            }
        } else {
            goalDao.upsertAll(remote.map { it.toEntity() })
        }
    }

    private fun userCollection(userId: String) = firestore.collection(COLLECTION_USERS).document(userId)

    private companion object {
        const val COLLECTION_USERS = "users"
        const val COLLECTION_INCOME = "income_entries"
        const val COLLECTION_EXPENSES = "expense_entries"
        const val COLLECTION_GOALS = "goals"
    }
}

private fun IncomeEntity.toFirestore() = FirestoreIncome(
    id = id,
    sourceType = sourceType,
    amountOriginal = amountOriginal,
    currency = currency,
    exchangeRateToLkr = exchangeRateToLkr,
    amountLkr = amountLkr,
    note = note,
    receivedAt = receivedAt,
    createdAt = createdAt,
)

private fun FirestoreIncome.toEntity() = IncomeEntity(
    id = id,
    sourceType = sourceType,
    amountOriginal = amountOriginal,
    currency = currency,
    exchangeRateToLkr = exchangeRateToLkr,
    amountLkr = amountLkr,
    note = note,
    receivedAt = receivedAt,
    createdAt = createdAt,
)

private fun ExpenseEntity.toFirestore() = FirestoreExpense(
    id = id,
    category = category,
    spendingType = spendingType,
    recurrenceType = recurrenceType,
    recurrenceGroupId = recurrenceGroupId,
    isRecurringTemplate = isRecurringTemplate,
    originalCurrency = originalCurrency,
    originalAmount = originalAmount,
    amountLkr = amountLkr,
    paymentMethod = paymentMethod,
    accountName = accountName,
    note = note,
    spentAt = spentAt,
    createdAt = createdAt,
)

private fun FirestoreExpense.toEntity() = ExpenseEntity(
    id = id,
    category = category,
    spendingType = spendingType,
    recurrenceType = recurrenceType,
    recurrenceGroupId = recurrenceGroupId,
    isRecurringTemplate = isRecurringTemplate,
    originalCurrency = originalCurrency,
    originalAmount = originalAmount,
    amountLkr = amountLkr,
    paymentMethod = paymentMethod,
    accountName = accountName,
    note = note,
    spentAt = spentAt,
    createdAt = createdAt,
)

private fun GoalEntity.toFirestore() = FirestoreGoal(
    id = id,
    title = title,
    targetAmountLkr = targetAmountLkr,
    currentSavedLkr = currentSavedLkr,
    monthlyContributionLkr = monthlyContributionLkr,
    contributionDayOfMonth = contributionDayOfMonth,
    contributionSource = contributionSource,
    allowEmergencyUse = allowEmergencyUse,
    emergencyUsedLkr = emergencyUsedLkr,
    lastContributionAt = lastContributionAt,
    deadlineAt = deadlineAt,
    createdAt = createdAt,
)

private fun FirestoreGoal.toEntity() = GoalEntity(
    id = id,
    title = title,
    targetAmountLkr = targetAmountLkr,
    currentSavedLkr = currentSavedLkr,
    monthlyContributionLkr = monthlyContributionLkr,
    contributionDayOfMonth = contributionDayOfMonth,
    contributionSource = contributionSource,
    allowEmergencyUse = allowEmergencyUse,
    emergencyUsedLkr = emergencyUsedLkr,
    lastContributionAt = lastContributionAt,
    deadlineAt = deadlineAt,
    createdAt = createdAt,
)
