package com.example.my_financialtracker.ui.screens.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AssistChip
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.my_financialtracker.R
import com.example.my_financialtracker.model.AppDefaults
import com.example.my_financialtracker.model.DetectedTransactionItem
import com.example.my_financialtracker.model.InsightItem
import com.example.my_financialtracker.model.TransactionItem
import com.example.my_financialtracker.model.TransactionType
import com.example.my_financialtracker.model.expenseCategories
import com.example.my_financialtracker.model.incomeSources
import com.example.my_financialtracker.model.paymentMethods
import com.example.my_financialtracker.model.spendingTypes
import com.example.my_financialtracker.model.supportedCurrencies
import com.example.my_financialtracker.ui.components.AppScaffold
import com.example.my_financialtracker.ui.components.DropdownField
import com.example.my_financialtracker.ui.components.FrostedBadge
import com.example.my_financialtracker.ui.components.GradientHeroCard

@Composable
fun TransactionsScreen(
    transactions: List<TransactionItem>,
    detectedTransactions: List<DetectedTransactionItem>,
    insights: List<InsightItem>,
    spendingStatus: String,
    message: String?,
    onConfirmDetectedTransaction: (String, String, String, String, String) -> Unit,
    onIgnoreDetectedTransaction: (String) -> Unit,
    onUpdateTransaction: (TransactionItem) -> Unit,
    onDeleteTransaction: (TransactionItem) -> Unit,
    onConsumeMessage: () -> Unit,
    onAddIncomeClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onBottomNavClick: (String) -> Unit,
    currentRoute: String,
) {
    var editingTransaction by remember { mutableStateOf<TransactionItem?>(null) }
    var confirmingDetected by remember { mutableStateOf<DetectedTransactionItem?>(null) }

    LaunchedEffect(message) {
        if (message != null) onConsumeMessage()
    }

    AppScaffold(
        title = stringResource(R.string.transactions_title),
        currentRoute = currentRoute,
        showBottomBar = true,
        onBottomNavClick = onBottomNavClick,
        onAddIncomeClick = onAddIncomeClick,
        onAddExpenseClick = onAddExpenseClick,
    ) { modifier ->
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                GradientHeroCard(
                    eyebrow = "HISTORY",
                    title = "Recent Activity",
                    amount = transactions.firstOrNull()?.amountLabel ?: "LKR 0.00",
                    subtitle = stringResource(R.string.history_copy),
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
                message?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (detectedTransactions.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.history_detected_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4F46E5),
                    )
                }
            }

            items(detectedTransactions) { item ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(item.title, fontWeight = FontWeight.SemiBold)
                        Text(item.amountLabel)
                        Text(
                            text = "${item.detectedType} · ${item.suggestedCategoryOrSource}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (item.merchant.isNotBlank()) {
                            Text(item.merchant, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(item.rawText, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { confirmingDetected = item }) {
                                Text(stringResource(R.string.button_confirm))
                            }
                            TextButton(onClick = { onIgnoreDetectedTransaction(item.id) }) {
                                Text(stringResource(R.string.button_ignore))
                            }
                        }
                    }
                }
            }

            if (spendingStatus.isNotBlank()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                        ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.dashboard_spend_vs_left_title),
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = spendingStatus,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.history_insights_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4F46E5),
                )
            }

            items(insights) { insight ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(insight.title, fontWeight = FontWeight.SemiBold)
                        Text(insight.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.history_recent_transactions_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4F46E5),
                )
            }

            items(transactions) { item ->
                Card(
                    shape = MaterialTheme.shapes.large,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(item.title, fontWeight = FontWeight.SemiBold)
                        Text(item.amountLabel)
                        Text(item.meta, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (item.note.isNotBlank()) {
                            Text(item.note, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (item.type != TransactionType.GOAL_TRANSFER) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(onClick = { editingTransaction = item }) {
                                    Text(stringResource(R.string.button_edit))
                                }
                                TextButton(onClick = { onDeleteTransaction(item) }) {
                                    Text(stringResource(R.string.button_delete))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editingTransaction?.let { transaction ->
        EditTransactionDialog(
            transaction = transaction,
            onDismiss = { editingTransaction = null },
            onSave = {
                onUpdateTransaction(it)
                editingTransaction = null
            },
        )
    }

    confirmingDetected?.let { detected ->
        ConfirmDetectedTransactionDialog(
            item = detected,
            onDismiss = { confirmingDetected = null },
            onConfirm = { chosenType, chosenCategoryOrSource, chosenSpendingType, note ->
                onConfirmDetectedTransaction(
                    detected.id,
                    chosenType,
                    chosenCategoryOrSource,
                    chosenSpendingType,
                    note,
                )
                confirmingDetected = null
            },
        )
    }
}

@Composable
private fun EditTransactionDialog(
    transaction: TransactionItem,
    onDismiss: () -> Unit,
    onSave: (TransactionItem) -> Unit,
) {
    var title by remember(transaction.id) { mutableStateOf(transaction.title) }
    var amount by remember(transaction.id) { mutableStateOf(transaction.originalAmount.toString()) }
    var currency by remember(transaction.id) { mutableStateOf(transaction.originalCurrency) }
    var spendingType by remember(transaction.id) { mutableStateOf(transaction.spendingType ?: AppDefaults.DEFAULT_SPENDING_TYPE) }
    var paymentMethod by remember(transaction.id) { mutableStateOf(transaction.paymentMethod ?: AppDefaults.DEFAULT_PAYMENT_METHOD) }
    var note by remember(transaction.id) { mutableStateOf(transaction.note) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_transaction_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DropdownField(
                    label = if (transaction.type == TransactionType.INCOME) {
                        stringResource(R.string.field_source)
                    } else {
                        stringResource(R.string.field_category)
                    },
                    value = title,
                    options = if (transaction.type == TransactionType.INCOME) incomeSources else expenseCategories,
                    onValueSelected = { title = it },
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.field_amount)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                DropdownField(
                    label = stringResource(R.string.field_currency),
                    value = currency,
                    options = supportedCurrencies,
                    onValueSelected = { currency = it },
                )
                if (transaction.type == TransactionType.EXPENSE) {
                    DropdownField(
                        label = stringResource(R.string.field_spending_type),
                        value = spendingType,
                        options = spendingTypes,
                        onValueSelected = { spendingType = it },
                    )
                    DropdownField(
                        label = stringResource(R.string.field_payment_method),
                        value = paymentMethod,
                        options = paymentMethods,
                        onValueSelected = { paymentMethod = it },
                    )
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.field_note)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        transaction.copy(
                            title = title,
                            originalAmount = amount.toDoubleOrNull() ?: transaction.originalAmount,
                            originalCurrency = currency,
                            spendingType = if (transaction.type == TransactionType.EXPENSE) spendingType else null,
                            paymentMethod = if (transaction.type == TransactionType.EXPENSE) paymentMethod else null,
                            note = note,
                        ),
                    )
                },
            ) {
                Text(stringResource(R.string.button_save))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        },
    )
}

@Composable
private fun ConfirmDetectedTransactionDialog(
    item: DetectedTransactionItem,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit,
) {
    var chosenType by remember(item.id) { mutableStateOf(item.detectedType) }
    var categoryOrSource by remember(item.id) { mutableStateOf(item.suggestedCategoryOrSource) }
    var spendingType by remember(item.id) { mutableStateOf(AppDefaults.DEFAULT_SPENDING_TYPE) }
    var note by remember(item.id) { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.history_confirm_detected_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DropdownField(
                    label = stringResource(R.string.history_detected_type_field),
                    value = chosenType,
                    options = listOf("EXPENSE", "INCOME"),
                    onValueSelected = {
                        chosenType = it
                        categoryOrSource = if (it == "INCOME") incomeSources.first() else expenseCategories.first()
                    },
                )
                DropdownField(
                    label = if (chosenType == "INCOME") {
                        stringResource(R.string.field_source)
                    } else {
                        stringResource(R.string.field_category)
                    },
                    value = categoryOrSource,
                    options = if (chosenType == "INCOME") incomeSources else expenseCategories,
                    onValueSelected = { categoryOrSource = it },
                )
                if (chosenType == "EXPENSE") {
                    DropdownField(
                        label = stringResource(R.string.field_spending_type),
                        value = spendingType,
                        options = spendingTypes,
                        onValueSelected = { spendingType = it },
                    )
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.field_note)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(chosenType, categoryOrSource, spendingType, note) }) {
                Text(stringResource(R.string.button_confirm))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        },
    )
}
