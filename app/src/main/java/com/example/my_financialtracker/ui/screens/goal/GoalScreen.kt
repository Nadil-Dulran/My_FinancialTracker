package com.example.my_financialtracker.ui.screens.goal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.my_financialtracker.R
import com.example.my_financialtracker.model.GoalOverview
import com.example.my_financialtracker.ui.components.AppScaffold
import com.example.my_financialtracker.ui.state.GoalUiState

@Composable
fun GoalScreen(
    uiState: GoalUiState,
    onAddGoal: (String, String, String, String, String, String, Boolean) -> Unit,
    onEmergencyWithdraw: (String, String) -> Unit,
    onBottomNavClick: (String) -> Unit,
    currentRoute: String,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var emergencyGoalId by remember { mutableStateOf<String?>(null) }

    AppScaffold(
        title = stringResource(R.string.goal_title),
        currentRoute = currentRoute,
        showBottomBar = true,
        onBottomNavClick = onBottomNavClick,
    ) { modifier ->
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    )
                }
            }

            if (uiState.goals.isEmpty()) {
                item {
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.goal_empty_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = stringResource(R.string.goal_empty_copy),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            items(uiState.goals) { goal ->
                GoalCard(
                    goal = goal,
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
}

@Composable
private fun GoalCard(
    goal: GoalOverview,
    onEmergencyWithdraw: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, Boolean) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var currentSaved by remember { mutableStateOf("") }
    var monthsToDeadline by remember { mutableStateOf("12") }
    var monthlyContribution by remember { mutableStateOf("") }
    var contributionDay by remember { mutableStateOf("5") }
    var allowEmergencyUse by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.goal_new_goal_title)) },
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
