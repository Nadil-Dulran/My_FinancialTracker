package com.example.my_financialtracker.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.example.my_financialtracker.ui.components.FrostedBadge
import com.example.my_financialtracker.ui.components.GradientHeroCard
import com.example.my_financialtracker.ui.components.MetricCard
import com.example.my_financialtracker.ui.state.ProfileUiState

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onOpenSettings: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onSignOut: () -> Unit,
    onAddIncomeClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onBottomNavClick: (String) -> Unit,
    currentRoute: String,
) {
    AppScaffold(
        title = stringResource(R.string.profile_title),
        currentRoute = currentRoute,
        showBottomBar = true,
        onBottomNavClick = onBottomNavClick,
        onAddIncomeClick = onAddIncomeClick,
        onAddExpenseClick = onAddExpenseClick,
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            GradientHeroCard(
                eyebrow = "PROFILE",
                title = uiState.displayName,
                amount = uiState.preferredCurrency,
                subtitle = uiState.email,
                modifier = Modifier.fillMaxWidth(),
                accent = {
                    FrostedBadge(
                        text = "Account",
                        icon = Icons.Outlined.Person,
                    )
                },
            )

            MetricCard(
                title = "Notification Access",
                amount = if (uiState.notificationCaptureEnabled) "Enabled" else "Disabled",
                description = if (uiState.notificationCaptureEnabled) {
                    stringResource(R.string.profile_notifications_enabled)
                } else {
                    stringResource(R.string.profile_notifications_disabled)
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Card(
                shape = MaterialTheme.shapes.large,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.profile_account_heading),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4F46E5),
                    )
                    Text(
                        text = stringResource(R.string.profile_preferred_currency, uiState.preferredCurrency),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

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
                        text = stringResource(R.string.profile_how_it_works_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4F46E5),
                    )
                    Text(
                        text = stringResource(R.string.profile_how_it_works_intro),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(stringResource(R.string.profile_how_it_works_step_1))
                    Text(stringResource(R.string.profile_how_it_works_step_2))
                    Text(stringResource(R.string.profile_how_it_works_step_3))
                    Text(stringResource(R.string.profile_how_it_works_step_4))
                    Text(stringResource(R.string.profile_how_it_works_step_5))
                }
            }

            Button(onClick = onOpenNotificationAccess, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.profile_notification_access))
            }
            Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.profile_open_settings))
            }
            Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.profile_sign_out))
            }
        }
    }
}
