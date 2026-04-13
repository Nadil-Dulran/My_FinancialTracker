package com.example.my_financialtracker.repository.local

import com.example.my_financialtracker.data.currency.CurrencyConverter
import com.example.my_financialtracker.data.currency.ExchangeRateRepository
import com.example.my_financialtracker.data.local.dao.DetectedTransactionDao
import com.example.my_financialtracker.data.local.dao.ExpenseDao
import com.example.my_financialtracker.data.local.dao.GoalDao
import com.example.my_financialtracker.data.local.dao.IncomeDao
import com.example.my_financialtracker.data.notification.NotificationTransactionParser
import com.example.my_financialtracker.model.DetectedTransactionItem
import com.example.my_financialtracker.data.local.entity.ExpenseEntity
import com.example.my_financialtracker.data.local.entity.IncomeEntity
import com.example.my_financialtracker.data.preferences.UserPreferencesRepository
import com.example.my_financialtracker.data.remote.FirestoreSyncService
import com.example.my_financialtracker.model.ChartDatum
import com.example.my_financialtracker.model.InsightItem
import com.example.my_financialtracker.model.SummaryCard
import com.example.my_financialtracker.model.TransactionItem
import com.example.my_financialtracker.model.TransactionType
import com.example.my_financialtracker.repository.FinanceRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import java.util.UUID
import kotlin.math.max

class LocalFinanceRepository(
    private val incomeDao: IncomeDao,
    private val expenseDao: ExpenseDao,
    private val goalDao: GoalDao,
    private val detectedTransactionDao: DetectedTransactionDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
    private val firebaseAuth: FirebaseAuth,
    private val syncService: FirestoreSyncService,
) : FinanceRepository {

    override fun observeDashboardSummary(): Flow<List<SummaryCard>> {
        return combine(
            incomeDao.observeAll(),
            expenseDao.observeAll(),
            goalDao.observeAllGoals(),
            userPreferencesRepository.preferredCurrency,
            exchangeRateRepository.ratesToLkr,
        ) { incomes, expenses, goals, preferredCurrency, rates ->
            val actualExpenses = expenses.filterNot { it.isRecurringTemplate }
            val incomeTotal = incomes.sumOf { it.amountLkr }
            val expenseTotal = actualExpenses.sumOf { it.amountLkr }
            val reservedTotal = goals.sumOf { it.currentSavedLkr }
            val available = incomeTotal - expenseTotal - reservedTotal

            listOf(
                SummaryCard(
                    title = "Income recorded",
                    amountLabel = formatDisplayCurrency(incomeTotal, preferredCurrency, rates),
                    description = "Salary, freelance, AdSense, and crypto in one view",
                ),
                SummaryCard(
                    title = "Expenses recorded",
                    amountLabel = formatDisplayCurrency(expenseTotal, preferredCurrency, rates),
                    description = "Committed, discretionary, and recurring expenses combined",
                ),
                SummaryCard(
                    title = "Estimated free cash",
                    amountLabel = formatDisplayCurrency(max(available, 0.0), preferredCurrency, rates),
                    description = "Remaining amount after expenses and goal reserves",
                ),
            )
        }
    }

    override fun observeExpenseChart(): Flow<List<ChartDatum>> {
        return combine(
            expenseDao.observeAll(),
            userPreferencesRepository.preferredCurrency,
            exchangeRateRepository.ratesToLkr,
        ) { expenses, preferredCurrency, rates ->
            expenses.filterNot { it.isRecurringTemplate }
                .groupBy { it.category }
                .map { (label, items) ->
                    val amountLkr = items.sumOf { it.amountLkr }
                    ChartDatum(
                        label = label,
                        value = amountLkr,
                        valueLabel = formatDisplayCurrency(amountLkr, preferredCurrency, rates),
                    )
                }
                .sortedByDescending { it.value }
                .take(6)
        }
    }

    override fun observeIncomeChart(): Flow<List<ChartDatum>> {
        return combine(
            incomeDao.observeAll(),
            userPreferencesRepository.preferredCurrency,
            exchangeRateRepository.ratesToLkr,
        ) { incomes, preferredCurrency, rates ->
            incomes.groupBy { it.sourceType.replaceFirstChar(Char::uppercase) }
                .map { (label, items) ->
                    val amountLkr = items.sumOf { it.amountLkr }
                    ChartDatum(
                        label = label,
                        value = amountLkr,
                        valueLabel = formatDisplayCurrency(amountLkr, preferredCurrency, rates),
                    )
                }
                .sortedByDescending { it.value }
                .take(6)
        }
    }

    fun observeSpendingSplitChart(): Flow<List<ChartDatum>> {
        return combine(
            expenseDao.observeAll(),
            userPreferencesRepository.preferredCurrency,
            exchangeRateRepository.ratesToLkr,
        ) { expenses, preferredCurrency, rates ->
            val actualExpenses = expenses.filterNot { it.isRecurringTemplate }
            val committed = actualExpenses.filter { it.spendingType.equals("Committed", true) }.sumOf { it.amountLkr }
            val discretionary = actualExpenses.filter { it.spendingType.equals("Discretionary", true) }.sumOf { it.amountLkr }
            listOf(
                ChartDatum("Committed", committed, formatDisplayCurrency(committed, preferredCurrency, rates)),
                ChartDatum("Discretionary", discretionary, formatDisplayCurrency(discretionary, preferredCurrency, rates)),
            )
        }
    }

    fun observeSpendVsLeftChart(): Flow<List<ChartDatum>> {
        return combine(
            incomeDao.observeAll(),
            expenseDao.observeAll(),
            goalDao.observeAllGoals(),
            userPreferencesRepository.preferredCurrency,
            exchangeRateRepository.ratesToLkr,
        ) { incomes, expenses, goals, preferredCurrency, rates ->
            val actualExpenses = expenses.filterNot { it.isRecurringTemplate }
            val spent = actualExpenses.sumOf { it.amountLkr }
            val reserved = goals.sumOf { it.currentSavedLkr }
            val income = incomes.sumOf { it.amountLkr }
            val left = max(income - spent - reserved, 0.0)

            listOf(
                ChartDatum("Spent", spent, formatDisplayCurrency(spent, preferredCurrency, rates)),
                ChartDatum("Reserved", reserved, formatDisplayCurrency(reserved, preferredCurrency, rates)),
                ChartDatum("Left", left, formatDisplayCurrency(left, preferredCurrency, rates)),
            )
        }
    }

    fun observeSpendVsLeftMessage(): Flow<String> {
        return combine(
            incomeDao.observeAll(),
            expenseDao.observeAll(),
            goalDao.observeAllGoals(),
            userPreferencesRepository.preferredCurrency,
            exchangeRateRepository.ratesToLkr,
        ) { incomes, expenses, goals, preferredCurrency, rates ->
            val actualExpenses = expenses.filterNot { it.isRecurringTemplate }
            val spent = actualExpenses.sumOf { it.amountLkr }
            val reserved = goals.sumOf { it.currentSavedLkr }
            val income = incomes.sumOf { it.amountLkr }
            val left = income - spent - reserved

            if (left >= 0.0) {
                "You've spent ${formatDisplayCurrency(spent, preferredCurrency, rates)}, reserved ${formatDisplayCurrency(reserved, preferredCurrency, rates)} for goals, and still have ${formatDisplayCurrency(left, preferredCurrency, rates)} left."
            } else {
                "You've spent ${formatDisplayCurrency(spent, preferredCurrency, rates)} and reserved ${formatDisplayCurrency(reserved, preferredCurrency, rates)}, which puts you ${formatDisplayCurrency(kotlin.math.abs(left), preferredCurrency, rates)} over recorded income."
            }
        }
    }

    fun observeInsights(): Flow<List<InsightItem>> {
        return combine(
            incomeDao.observeAll(),
            expenseDao.observeAll(),
            userPreferencesRepository.preferredCurrency,
            exchangeRateRepository.ratesToLkr,
        ) { incomes, expenses, preferredCurrency, rates ->
            val actualExpenses = expenses.filterNot { it.isRecurringTemplate }
            val recurringCount = expenses.count { it.isRecurringTemplate }
            val committed = actualExpenses.filter { it.spendingType.equals("Committed", ignoreCase = true) }
                .sumOf { it.amountLkr }
            val discretionary = actualExpenses.filter { it.spendingType.equals("Discretionary", ignoreCase = true) }
                .sumOf { it.amountLkr }
            val topCategory = actualExpenses.groupBy { it.category }
                .maxByOrNull { (_, items) -> items.sumOf { it.amountLkr } }
                ?.key ?: "No category yet"
            val monthlyIncome = incomes.sumOf { it.amountLkr }

            listOf(
                InsightItem(
                    title = "Committed vs discretionary",
                    description = "${formatDisplayCurrency(committed, preferredCurrency, rates)} committed and ${formatDisplayCurrency(discretionary, preferredCurrency, rates)} discretionary.",
                ),
                InsightItem(
                    title = "Top expense category",
                    description = topCategory,
                ),
                InsightItem(
                    title = "Recurring expenses",
                    description = "$recurringCount recurring plans are active in the tracker.",
                ),
                InsightItem(
                    title = "Income spread",
                    description = "You've recorded ${formatDisplayCurrency(monthlyIncome, preferredCurrency, rates)} across ${incomes.size} income entries.",
                ),
            )
        }
    }

    override fun observeRecentTransactions(): Flow<List<TransactionItem>> {
        return combine(
            incomeDao.observeAll(),
            expenseDao.observeAll(),
            goalDao.observeAllGoals(),
            userPreferencesRepository.preferredCurrency,
            exchangeRateRepository.ratesToLkr,
        ) { incomes, expenses, goals, preferredCurrency, rates ->
            val mappedIncome = incomes.map {
                TransactionItem(
                    id = it.id,
                    type = TransactionType.INCOME,
                    title = it.sourceType.replaceFirstChar(Char::uppercase),
                    amountLabel = "+ ${formatDisplayCurrency(it.amountLkr, preferredCurrency, rates)}",
                    meta = "Income · ${it.currency}",
                    originalAmount = it.amountOriginal,
                    originalCurrency = it.currency,
                    note = it.note,
                )
            }
            val mappedExpenses = expenses.filterNot { it.isRecurringTemplate }.map {
                val recurrenceMeta = if (it.recurrenceType != "None") " · ${it.recurrenceType}" else ""
                TransactionItem(
                    id = it.id,
                    type = TransactionType.EXPENSE,
                    title = it.category,
                    amountLabel = "- ${formatDisplayCurrency(it.amountLkr, preferredCurrency, rates)}",
                    meta = "${it.spendingType} · ${it.paymentMethod}$recurrenceMeta",
                    originalAmount = it.originalAmount,
                    originalCurrency = it.originalCurrency,
                    spendingType = it.spendingType,
                    recurrenceType = it.recurrenceType,
                    paymentMethod = it.paymentMethod,
                    note = it.note,
                )
            }
            val mappedGoalTransfers = goals.filter { it.lastContributionAt > 0L && it.monthlyContributionLkr > 0.0 }.map {
                TransactionItem(
                    id = "goal_transfer_${it.id}",
                    type = TransactionType.GOAL_TRANSFER,
                    title = "${it.title} reserve",
                    amountLabel = "- ${formatDisplayCurrency(it.monthlyContributionLkr, preferredCurrency, rates)}",
                    meta = "Auto-saved on day ${it.contributionDayOfMonth} from ${it.contributionSource}",
                    originalAmount = it.monthlyContributionLkr,
                    originalCurrency = preferredCurrency,
                    note = "Reserved automatically for this goal.",
                )
            }

            (mappedIncome + mappedExpenses + mappedGoalTransfers)
                .sortedByDescending { item ->
                    when (item.type) {
                        TransactionType.INCOME -> incomes.firstOrNull { it.id == item.id }?.receivedAt ?: 0L
                        TransactionType.EXPENSE -> expenses.firstOrNull { it.id == item.id }?.spentAt ?: 0L
                        TransactionType.GOAL_TRANSFER -> goals.firstOrNull { "goal_transfer_${it.id}" == item.id }?.lastContributionAt ?: 0L
                    }
                }
                .take(20)
        }
    }

    override fun observeDetectedTransactions(): Flow<List<DetectedTransactionItem>> {
        return combine(
            detectedTransactionDao.observePending(),
            userPreferencesRepository.preferredCurrency,
            exchangeRateRepository.ratesToLkr,
        ) { items, preferredCurrency, rates ->
            items.map {
                DetectedTransactionItem(
                    id = it.id,
                    title = it.title,
                    amountLabel = formatDisplayCurrency(it.amountLkr, preferredCurrency, rates),
                    merchant = it.merchant,
                    detectedType = it.detectedType,
                    suggestedCategoryOrSource = it.suggestedCategoryOrSource,
                    rawText = it.rawText,
                    currency = it.currency,
                    amountOriginal = it.amountOriginal,
                    occurredAt = it.occurredAt,
                )
            }
        }
    }

    override suspend fun seedDemoDataIfNeeded() {
        if (incomeDao.getAll().isNotEmpty() || expenseDao.getAll().isNotEmpty()) return

        incomeDao.upsertAll(
            listOf(
                IncomeEntity(
                    id = "income_salary_apr",
                    sourceType = "salary",
                    amountOriginal = 132000.0,
                    currency = "LKR",
                    exchangeRateToLkr = 1.0,
                    amountLkr = 132000.0,
                    note = "Monthly salary",
                    receivedAt = 1713974400000,
                    createdAt = 1713974400000,
                ),
                IncomeEntity(
                    id = "income_freelance_apr",
                    sourceType = "freelance",
                    amountOriginal = 35000.0,
                    currency = "LKR",
                    exchangeRateToLkr = 1.0,
                    amountLkr = 35000.0,
                    note = "React SME milestone",
                    receivedAt = 1713715200000,
                    createdAt = 1713715200000,
                ),
                IncomeEntity(
                    id = "income_adsense_apr",
                    sourceType = "adsense",
                    amountOriginal = 42.0,
                    currency = "USD",
                    exchangeRateToLkr = 300.0,
                    amountLkr = 12600.0,
                    note = "Blog revenue",
                    receivedAt = 1713542400000,
                    createdAt = 1713542400000,
                ),
            ),
        )

        val recurringTemplateId = "recurring_rent_template"
        expenseDao.upsertAll(
            listOf(
                ExpenseEntity(
                    id = recurringTemplateId,
                    category = "Rent",
                    spendingType = "Committed",
                    recurrenceType = "Monthly",
                    recurrenceGroupId = recurringTemplateId,
                    isRecurringTemplate = true,
                    originalCurrency = "LKR",
                    originalAmount = 34000.0,
                    amountLkr = 34000.0,
                    paymentMethod = "Bank transfer",
                    accountName = "Main account",
                    note = "Monthly rent",
                    spentAt = 1713638800000,
                    createdAt = 1713638800000,
                ),
                ExpenseEntity(
                    id = "expense_rent_apr",
                    category = "Rent",
                    spendingType = "Committed",
                    recurrenceType = "Monthly",
                    recurrenceGroupId = recurringTemplateId,
                    isRecurringTemplate = false,
                    originalCurrency = "LKR",
                    originalAmount = 34000.0,
                    amountLkr = 34000.0,
                    paymentMethod = "Bank transfer",
                    accountName = "Main account",
                    note = "Monthly rent",
                    spentAt = 1713638800000,
                    createdAt = 1713638800000,
                ),
                ExpenseEntity(
                    id = "expense_food",
                    category = "Coffee & Dining",
                    spendingType = "Discretionary",
                    recurrenceType = "None",
                    originalCurrency = "LKR",
                    originalAmount = 6400.0,
                    amountLkr = 6400.0,
                    paymentMethod = "Card",
                    accountName = "Main account",
                    note = "Casual food spending cluster",
                    spentAt = 1713801600000,
                    createdAt = 1713801600000,
                ),
                ExpenseEntity(
                    id = "expense_transport",
                    category = "PickMe",
                    spendingType = "Discretionary",
                    recurrenceType = "None",
                    originalCurrency = "LKR",
                    originalAmount = 1800.0,
                    amountLkr = 1800.0,
                    paymentMethod = "Card",
                    accountName = "Main account",
                    note = "Ride expenses",
                    spentAt = 1713888000000,
                    createdAt = 1713888000000,
                ),
            ),
        )

        refreshRecurringExpensesIfNeeded()
    }

    override suspend fun addIncome(
        sourceType: String,
        amount: Double,
        currency: String,
        note: String,
    ): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        val rateToLkr = exchangeRateRepository.getRateToLkr(currency)
        val amountLkr = CurrencyConverter.toLkr(amount, currency, rateToLkr)
        val entity = IncomeEntity(
            id = "income_${UUID.randomUUID()}",
            sourceType = sourceType.lowercase(),
            amountOriginal = amount,
            currency = currency,
            exchangeRateToLkr = rateToLkr,
            amountLkr = amountLkr,
            note = note,
            receivedAt = now,
            createdAt = now,
        )
        incomeDao.upsert(entity)
        firebaseAuth.currentUser?.uid?.let { uid -> syncService.pushIncome(uid, entity) }
    }

    override suspend fun addExpense(
        category: String,
        amount: Double,
        currency: String,
        spendingType: String,
        recurrenceType: String,
        paymentMethod: String,
        accountName: String,
        note: String,
    ): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        val rateToLkr = exchangeRateRepository.getRateToLkr(currency)
        val amountLkr = CurrencyConverter.toLkr(amount, currency, rateToLkr)

        if (recurrenceType == "None") {
            val entity = ExpenseEntity(
                id = "expense_${UUID.randomUUID()}",
                category = category,
                spendingType = spendingType,
                recurrenceType = recurrenceType,
                originalCurrency = currency,
                originalAmount = amount,
                amountLkr = amountLkr,
                paymentMethod = paymentMethod,
                accountName = accountName,
                note = note,
                spentAt = now,
                createdAt = now,
            )
            expenseDao.upsert(entity)
            firebaseAuth.currentUser?.uid?.let { uid -> syncService.pushExpense(uid, entity) }
        } else {
            val groupId = "recurring_${UUID.randomUUID()}"
            val template = ExpenseEntity(
                id = groupId,
                category = category,
                spendingType = spendingType,
                recurrenceType = recurrenceType,
                recurrenceGroupId = groupId,
                isRecurringTemplate = true,
                originalCurrency = currency,
                originalAmount = amount,
                amountLkr = amountLkr,
                paymentMethod = paymentMethod,
                accountName = accountName,
                note = note,
                spentAt = now,
                createdAt = now,
            )
            val firstOccurrence = template.copy(
                id = "expense_${UUID.randomUUID()}",
                recurrenceGroupId = groupId,
                isRecurringTemplate = false,
            )
            expenseDao.upsertAll(listOf(template, firstOccurrence))
            firebaseAuth.currentUser?.uid?.let { uid ->
                syncService.pushExpense(uid, template)
                syncService.pushExpense(uid, firstOccurrence)
            }
        }
    }

    override suspend fun refreshRecurringExpensesIfNeeded() {
        val allExpenses = expenseDao.getAll()
        val templates = allExpenses.filter { it.isRecurringTemplate && it.recurrenceType != "None" }
        val generated = mutableListOf<ExpenseEntity>()
        val userId = firebaseAuth.currentUser?.uid
        val now = System.currentTimeMillis()

        templates.forEach { template ->
            val latestOccurrence = allExpenses
                .filter { !it.isRecurringTemplate && it.recurrenceGroupId == template.id }
                .maxByOrNull { it.spentAt }
                ?: return@forEach

            var nextDueAt = nextOccurrenceTime(latestOccurrence.spentAt, template.recurrenceType)
            while (nextDueAt <= now) {
                val occurrence = template.copy(
                    id = "expense_${UUID.randomUUID()}",
                    isRecurringTemplate = false,
                    recurrenceGroupId = template.id,
                    spentAt = nextDueAt,
                    createdAt = now,
                )
                generated += occurrence
                if (userId != null) {
                    syncService.pushExpense(userId, occurrence)
                }
                nextDueAt = nextOccurrenceTime(nextDueAt, template.recurrenceType)
            }
        }

        if (generated.isNotEmpty()) {
            expenseDao.upsertAll(generated)
        }
    }

    override suspend fun ingestDetectedTransaction(
        packageName: String,
        title: String,
        body: String,
        postedAt: Long,
    ) {
        val rates = userPreferencesRepository.getCachedRates()
        val parsed = NotificationTransactionParser.parse(
            packageName = packageName,
            title = title,
            text = body,
            postedAt = postedAt,
        ) { amount, currency ->
            val rateToLkr = (rates[currency.uppercase()] ?: rates["LKR"] ?: 1.0)
            CurrencyConverter.toLkr(amount, currency, rateToLkr)
        } ?: return
        detectedTransactionDao.upsert(parsed)
    }

    override suspend fun confirmDetectedTransaction(
        id: String,
        chosenType: String,
        chosenCategoryOrSource: String,
        chosenSpendingType: String,
        note: String,
    ): Result<Unit> = runCatching {
        val detected = detectedTransactionDao.getById(id) ?: error("Detected transaction not found.")
        if (chosenType == "INCOME") {
            addIncome(
                sourceType = chosenCategoryOrSource,
                amount = detected.amountOriginal,
                currency = detected.currency,
                note = buildString {
                    append("Verified from bank notification")
                    if (note.isNotBlank()) append(": $note")
                },
            ).getOrThrow()
        } else {
            addExpense(
                category = chosenCategoryOrSource,
                amount = detected.amountOriginal,
                currency = detected.currency,
                spendingType = chosenSpendingType,
                recurrenceType = "None",
                paymentMethod = "Card",
                accountName = "Detected from bank alert",
                note = buildString {
                    append("Verified from bank notification")
                    if (detected.merchant.isNotBlank()) append(" at ${detected.merchant}")
                    if (note.isNotBlank()) append(": $note")
                },
            ).getOrThrow()
        }
        detectedTransactionDao.upsert(detected.copy(status = "CONFIRMED"))
    }

    override suspend fun ignoreDetectedTransaction(id: String): Result<Unit> = runCatching {
        val detected = detectedTransactionDao.getById(id) ?: error("Detected transaction not found.")
        detectedTransactionDao.upsert(detected.copy(status = "IGNORED"))
    }

    override suspend fun updateTransaction(transaction: TransactionItem): Result<Unit> = runCatching {
        when (transaction.type) {
            TransactionType.INCOME -> {
                val existing = incomeDao.getById(transaction.id) ?: error("Income not found.")
                val rateToLkr = exchangeRateRepository.getRateToLkr(transaction.originalCurrency)
                val updated = existing.copy(
                    sourceType = transaction.title.lowercase(),
                    amountOriginal = transaction.originalAmount,
                    currency = transaction.originalCurrency,
                    exchangeRateToLkr = rateToLkr,
                    amountLkr = CurrencyConverter.toLkr(transaction.originalAmount, transaction.originalCurrency, rateToLkr),
                    note = transaction.note,
                )
                incomeDao.upsert(updated)
                firebaseAuth.currentUser?.uid?.let { uid -> syncService.pushIncome(uid, updated) }
            }
            TransactionType.EXPENSE -> {
                val existing = expenseDao.getById(transaction.id) ?: error("Expense not found.")
                val rateToLkr = exchangeRateRepository.getRateToLkr(transaction.originalCurrency)
                val updated = existing.copy(
                    category = transaction.title,
                    spendingType = transaction.spendingType ?: existing.spendingType,
                    originalCurrency = transaction.originalCurrency,
                    originalAmount = transaction.originalAmount,
                    amountLkr = CurrencyConverter.toLkr(transaction.originalAmount, transaction.originalCurrency, rateToLkr),
                    paymentMethod = transaction.paymentMethod ?: existing.paymentMethod,
                    note = transaction.note,
                )
                expenseDao.upsert(updated)
                firebaseAuth.currentUser?.uid?.let { uid -> syncService.pushExpense(uid, updated) }
            }
            TransactionType.GOAL_TRANSFER -> error("Automatic goal reserve entries cannot be edited here.")
        }
    }

    override suspend fun deleteTransaction(transaction: TransactionItem): Result<Unit> = runCatching {
        when (transaction.type) {
            TransactionType.INCOME -> {
                val existing = incomeDao.getById(transaction.id) ?: error("Income not found.")
                incomeDao.delete(existing)
                firebaseAuth.currentUser?.uid?.let { uid -> syncService.deleteIncome(uid, transaction.id) }
            }
            TransactionType.EXPENSE -> {
                val existing = expenseDao.getById(transaction.id) ?: error("Expense not found.")
                expenseDao.delete(existing)
                firebaseAuth.currentUser?.uid?.let { uid -> syncService.deleteExpense(uid, transaction.id) }
            }
            TransactionType.GOAL_TRANSFER -> error("Automatic goal reserve entries cannot be deleted here.")
        }
    }

    private fun nextOccurrenceTime(baseTime: Long, recurrenceType: String): Long {
        return Calendar.getInstance().apply {
            timeInMillis = baseTime
            when (recurrenceType) {
                "Monthly" -> add(Calendar.MONTH, 1)
                "Yearly" -> add(Calendar.YEAR, 1)
            }
        }.timeInMillis
    }

    private fun formatDisplayCurrency(
        amountLkr: Double,
        preferredCurrency: String,
        rates: Map<String, Double>,
    ): String {
        val rateToLkr = rates[preferredCurrency.uppercase()] ?: 1.0
        val converted = CurrencyConverter.fromLkr(amountLkr, rateToLkr)
        return CurrencyConverter.format(converted, preferredCurrency)
    }
}
