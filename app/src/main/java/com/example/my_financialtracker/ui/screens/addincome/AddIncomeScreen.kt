package com.example.my_financialtracker.ui.screens.addincome

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.my_financialtracker.R
import com.example.my_financialtracker.model.incomeSources
import com.example.my_financialtracker.model.supportedCurrencies
import com.example.my_financialtracker.ui.components.AppScaffold
import com.example.my_financialtracker.ui.components.DropdownField
import com.example.my_financialtracker.ui.components.FrostedBadge
import com.example.my_financialtracker.ui.components.GradientHeroCard
import com.example.my_financialtracker.ui.state.EntryFormUiState

@Composable
fun AddIncomeScreen(
    uiState: EntryFormUiState,
    onSourceChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onBottomNavClick: (String) -> Unit,
    currentRoute: String?,
) {
    AppScaffold(
        title = stringResource(R.string.add_income_title),
        currentRoute = currentRoute,
        showBottomBar = false,
        onBottomNavClick = onBottomNavClick,
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            GradientHeroCard(
                eyebrow = "ADD INCOME",
                title = "Capture Income",
                amount = if (uiState.amount.isBlank()) "LKR 0.00" else uiState.amount,
                subtitle = uiState.helperText.ifBlank { stringResource(R.string.add_income_headline) },
                modifier = Modifier.fillMaxWidth(),
                accent = {
                    FrostedBadge(
                        text = "Income",
                        icon = Icons.Outlined.ArrowUpward,
                    )
                },
            )

            Card(
                shape = MaterialTheme.shapes.large,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = "Income Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    DropdownField(
                        label = stringResource(R.string.field_income_source),
                        value = uiState.primaryField,
                        options = incomeSources,
                        onValueSelected = onSourceChange,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = uiState.amount,
                        onValueChange = onAmountChange,
                        label = { Text(stringResource(R.string.field_amount)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    DropdownField(
                        label = stringResource(R.string.field_currency),
                        value = uiState.secondaryField,
                        options = supportedCurrencies,
                        onValueSelected = onCurrencyChange,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = uiState.note,
                        onValueChange = onNoteChange,
                        label = { Text(stringResource(R.string.field_note)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                    )
                    uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    uiState.successMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                }
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving,
            ) {
                Text(
                    if (uiState.isSaving) stringResource(R.string.button_saving)
                    else stringResource(R.string.button_save_income),
                )
            }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.button_back))
            }
        }
    }
}
