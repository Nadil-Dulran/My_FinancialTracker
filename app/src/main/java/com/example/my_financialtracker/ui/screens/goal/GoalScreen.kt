package com.example.my_financialtracker.ui.screens.goal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.example.my_financialtracker.model.GoalOverview
import com.example.my_financialtracker.ui.components.AppScaffold
import com.example.my_financialtracker.ui.components.EmptyStateCard
import com.example.my_financialtracker.ui.components.FrostedBadge
import com.example.my_financialtracker.ui.components.GradientHeroCard
import com.example.my_financialtracker.ui.components.MetricCard
import com.example.my_financialtracker.ui.state.GoalUiState

@Composable
fun GoalScreen(
    uiState: GoalUiState,
    onAddGoal: (String, String, String, String, String, String, Boolean) -> Unit,
    onUpdateGoal: (String, String, String, String, String, String, String, Boolean) -> Unit,
    onDeleteGoal: (String) -> Unit,
    onEmergencyWithdraw: (String, String) -> Unit,
    onAddIncomeClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onBottomNavClick: (String) -> Unit,
    currentRoute: String,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var emergencyGoalId by remember { mutableStateOf<String?>(null) }
    var editingGoal by remember { mutableStateOf<GoalOverview?>(null) }

    AppScaffold(
        title = stringResource(R.string.goal_title),
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
                    eyebrow = "GOALS",
                    title = "Savings Plan",
                    amount = uiState.overview?.currentSavedLabel ?: "LKR 0.00",
                    subtitle = uiState.overview?.remainingAmountLabel?.let { "Remaining: $it" } ?: "Set a goal and start tracking progress",
                    modifier = Modifier.fillMaxWidth(),
                    accent = {
                        FrostedBadge(
                            text = "Active goals",
                            icon = Icons.Outlined.Flag,
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
                        title = "Current Saved",
                        amount = uiState.overview?.currentSavedLabel ?: "LKR 0.00",
                        modifier = Modifier.weight(1f),
                    )
                    MetricCard(
                        title = "Target Amount",
                        amount = uiState.overview?.targetAmountLabel ?: "LKR 0.00",
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.goal_add_goal))
                    }
                }
            }

            item {
                uiState.message?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            if (uiState.goals.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = stringResource(R.string.goal_empty_title),
                        subtitle = stringResource(R.string.goal_empty_copy),
                    )
                }
            }

            if (uiState.goals.isNotEmpty()) {
                item {
                    Text(
                        text = "YOUR GOALS",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4F46E5),
                    )
                }
            }

            items(uiState.goals) { goal ->
                GoalCard(
                    goal = goal,
                    onEdit = { editingGoal = goal },
                    onDelete = { onDeleteGoal(goal.id) },
                    onEmergencyWithdraw = { emergencyGoalId = goal.id },
                )
            }
        }
    }

    if (showAddDialog) {
        AddGoalDialog(
            isSaving = uiState.isSaving,
            onDismiss = { showAddDialog = false },
            onSave = { title, target, current, months, monthlyContribution, contributionDay, allowEmergencyUse ->
                onAddGoal(
                    title,
                    target,
                    current,
                    months,
                    monthlyContribution,
                    contributionDay,
                    allowEmergencyUse,
                )
                showAddDialog = false
            },
        )
    }

    emergencyGoalId?.let { goalId ->
        EmergencyWithdrawDialog(
            onDismiss = { emergencyGoalId = null },
            onSave = { amount ->
                onEmergencyWithdraw(goalId, amount)
                emergencyGoalId = null
            },
        )
    }

    editingGoal?.let { goal ->
        AddGoalDialog(
            isSaving = uiState.isSaving,
            initialGoal = goal,
            onDismiss = { editingGoal = null },
            onSave = { title, target, current, months, monthlyContribution, contributionDay, allowEmergencyUse ->
                onUpdateGoal(
                    goal.id,
                    title,
                    target,
                    current,
                    months,
                    monthlyContribution,
                    contributionDay,
                    allowEmergencyUse,
                )
                editingGoal = null
            },
        )
    }
}

@Composable
private fun GoalCard(
    goal: GoalOverview,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onEmergencyWithdraw: () -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = goal.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            LinearProgressIndicator(
                progress = { goal.progress },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(stringResource(R.string.goal_target, goal.targetAmountLabel))
            Text(stringResource(R.string.goal_current, goal.currentSavedLabel))
            Text(stringResource(R.string.goal_remaining, goal.remainingAmountLabel))
            Text(stringResource(R.string.goal_deadline, goal.deadlineLabel))
            Text(goal.monthlyNeedLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = stringResource(R.string.goal_monthly_transfer, goal.monthlyContributionLabel),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(goal.contributionScheduleLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(goal.emergencyUseLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(goal.emergencyUsedLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Edit")
                }
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Delete")
                }
            }
            if (goal.emergencyUseLabel.contains("allowed", ignoreCase = true)) {
                Button(
                    onClick = onEmergencyWithdraw,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.goal_emergency_withdraw))
                }
            }
        }
    }
}

@Composable
private fun AddGoalDialog(
    isSaving: Boolean,
    initialGoal: GoalOverview? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, Boolean) -> Unit,
) {
    var title by remember(initialGoal?.id) { mutableStateOf(initialGoal?.title.orEmpty()) }
    var targetAmount by remember(initialGoal?.id) { mutableStateOf(initialGoal?.targetAmountLkr?.toString().orEmpty()) }
    var currentSaved by remember(initialGoal?.id) { mutableStateOf(initialGoal?.currentSavedLkr?.toString().orEmpty()) }
    var monthsToDeadline by remember(initialGoal?.id) { mutableStateOf((initialGoal?.monthsRemaining ?: 12).toString()) }
    var monthlyContribution by remember(initialGoal?.id) { mutableStateOf(initialGoal?.monthlyContributionLkr?.toString().orEmpty()) }
    var contributionDay by remember(initialGoal?.id) { mutableStateOf((initialGoal?.contributionDayOfMonth ?: 5).toString()) }
    var allowEmergencyUse by remember(initialGoal?.id) { mutableStateOf(initialGoal?.allowEmergencyUse ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialGoal == null) stringResource(R.string.goal_new_goal_title) else "Edit Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.field_name)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { targetAmount = it },
                    label = { Text(stringResource(R.string.goal_target_amount_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = currentSaved,
                    onValueChange = { currentSaved = it },
                    label = { Text(stringResource(R.string.goal_current_saved_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = monthsToDeadline,
                    onValueChange = { monthsToDeadline = it },
                    label = { Text(stringResource(R.string.goal_months_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = monthlyContribution,
                    onValueChange = { monthlyContribution = it },
                    label = { Text(stringResource(R.string.goal_monthly_transfer_field)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = contributionDay,
                    onValueChange = { contributionDay = it },
                    label = { Text(stringResource(R.string.goal_transfer_day_field)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Checkbox(
                        checked = allowEmergencyUse,
                        onCheckedChange = { allowEmergencyUse = it },
                    )
                    Text(
                        text = stringResource(R.string.goal_allow_emergency_use),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSaving,
                onClick = {
                    onSave(
                        title,
                        targetAmount,
                        currentSaved,
                        monthsToDeadline,
                        monthlyContribution,
                        contributionDay,
                        allowEmergencyUse,
                    )
                },
            ) {
                Text(if (isSaving) stringResource(R.string.button_saving) else stringResource(R.string.button_save))
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
private fun EmergencyWithdrawDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.goal_emergency_withdraw)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.goal_emergency_withdraw_copy),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.goal_emergency_amount_field)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(amount) }) {
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
