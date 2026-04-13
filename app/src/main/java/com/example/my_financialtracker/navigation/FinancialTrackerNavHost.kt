package com.example.my_financialtracker.navigation

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.my_financialtracker.ui.screens.addexpense.AddExpenseScreen
import com.example.my_financialtracker.ui.screens.addincome.AddIncomeScreen
import com.example.my_financialtracker.ui.screens.auth.LoginScreen
import com.example.my_financialtracker.ui.screens.auth.RegisterScreen
import com.example.my_financialtracker.ui.screens.dashboard.DashboardScreen
import com.example.my_financialtracker.ui.screens.goal.GoalScreen
import com.example.my_financialtracker.ui.screens.profile.ProfileScreen
import com.example.my_financialtracker.ui.screens.settings.SettingsScreen
import com.example.my_financialtracker.ui.screens.transactions.TransactionsScreen
import com.example.my_financialtracker.viewmodel.AddExpenseViewModel
import com.example.my_financialtracker.viewmodel.AddIncomeViewModel
import com.example.my_financialtracker.viewmodel.AuthViewModel
import com.example.my_financialtracker.viewmodel.DashboardViewModel
import com.example.my_financialtracker.viewmodel.GoalViewModel
import com.example.my_financialtracker.viewmodel.ProfileViewModel
import com.example.my_financialtracker.viewmodel.SettingsViewModel
import com.example.my_financialtracker.viewmodel.TransactionsViewModel

@Composable
fun FinancialTrackerNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Login.route,
    ) {
        composable(AppDestination.Login.route) {
            val viewModel: AuthViewModel = viewModel()
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            LoginScreen(
                uiState = uiState,
                onEmailChange = viewModel::updateEmail,
                onPasswordChange = viewModel::updatePassword,
                onLogin = {
                    viewModel.login {
                        navController.navigate(AppDestination.Dashboard.route) {
                            popUpTo(AppDestination.Login.route) {
                                inclusive = true
                            }
                        }
                    }
                },
                onRegisterClick = {
                    navController.navigate(AppDestination.Register.route)
                },
            )
        }

        composable(AppDestination.Register.route) {
            val viewModel: AuthViewModel = viewModel()
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            RegisterScreen(
                uiState = uiState,
                onNameChange = viewModel::updateName,
                onEmailChange = viewModel::updateEmail,
                onPasswordChange = viewModel::updatePassword,
                onRegister = {
                    viewModel.register {
                        navController.navigate(AppDestination.Dashboard.route) {
                            popUpTo(AppDestination.Login.route) {
                                inclusive = true
                            }
                        }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                },
            )
        }

        composable(AppDestination.Dashboard.route) {
            val viewModel: DashboardViewModel = viewModel()
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            DashboardScreen(
                uiState = uiState,
                onAddIncomeClick = { navController.navigate(AppDestination.AddIncome.route) },
                onAddExpenseClick = { navController.navigate(AppDestination.AddExpense.route) },
                onTransactionsClick = { navController.navigate(AppDestination.Transactions.route) },
                onGoalClick = { navController.navigate(AppDestination.Goal.route) },
                onSettingsClick = { navController.navigate(AppDestination.Profile.route) },
                onBottomNavClick = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                currentRoute = AppDestination.Dashboard.route,
            )
        }

        composable(AppDestination.AddIncome.route) {
            val viewModel: AddIncomeViewModel = viewModel()
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            AddIncomeScreen(
                uiState = uiState,
                onSourceChange = viewModel::updateSource,
                onAmountChange = viewModel::updateAmount,
                onCurrencyChange = viewModel::updateCurrency,
                onNoteChange = viewModel::updateNote,
                onSave = {
                    viewModel.save {
                        navController.popBackStack()
                    }
                },
                onBack = { navController.popBackStack() },
                onBottomNavClick = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                currentRoute = null,
            )
        }

        composable(AppDestination.AddExpense.route) {
            val viewModel: AddExpenseViewModel = viewModel()
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            AddExpenseScreen(
                uiState = uiState,
                onCategoryChange = viewModel::updateCategory,
                onAmountChange = viewModel::updateAmount,
                onCurrencyChange = viewModel::updateCurrency,
                onTypeChange = viewModel::updateType,
                onPaymentMethodChange = viewModel::updatePaymentMethod,
                onRecurrenceChange = viewModel::updateRecurrence,
                onNoteChange = viewModel::updateNote,
                onSave = {
                    viewModel.save {
                        navController.popBackStack()
                    }
                },
                onBack = { navController.popBackStack() },
                onBottomNavClick = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                currentRoute = AppDestination.AddExpense.route,
            )
        }

        composable(AppDestination.Transactions.route) {
            val viewModel: TransactionsViewModel = viewModel()
            val transactions = viewModel.transactions.collectAsStateWithLifecycle().value
            val detectedTransactions = viewModel.detectedTransactions.collectAsStateWithLifecycle().value
            val insights = viewModel.insights.collectAsStateWithLifecycle().value
            val spendingStatus = viewModel.spendingStatus.collectAsStateWithLifecycle().value
            val message = viewModel.message.collectAsStateWithLifecycle().value
            TransactionsScreen(
                transactions = transactions,
                detectedTransactions = detectedTransactions,
                insights = insights,
                spendingStatus = spendingStatus,
                message = message,
                onConfirmDetectedTransaction = viewModel::confirmDetectedTransaction,
                onIgnoreDetectedTransaction = viewModel::ignoreDetectedTransaction,
                onUpdateTransaction = viewModel::updateTransaction,
                onDeleteTransaction = viewModel::deleteTransaction,
                onConsumeMessage = viewModel::consumeMessage,
                onAddIncomeClick = { navController.navigate(AppDestination.AddIncome.route) },
                onAddExpenseClick = { navController.navigate(AppDestination.AddExpense.route) },
                onBottomNavClick = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                currentRoute = AppDestination.Transactions.route,
            )
        }

        composable(AppDestination.Goal.route) {
            val viewModel: GoalViewModel = viewModel()
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            GoalScreen(
                uiState = uiState,
                onAddGoal = viewModel::addGoal,
                onUpdateGoal = viewModel::updateGoal,
                onDeleteGoal = viewModel::deleteGoal,
                onEmergencyWithdraw = viewModel::applyEmergencyWithdrawal,
                onAddIncomeClick = { navController.navigate(AppDestination.AddIncome.route) },
                onAddExpenseClick = { navController.navigate(AppDestination.AddExpense.route) },
                onBottomNavClick = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                currentRoute = AppDestination.Goal.route,
            )
        }

        composable(AppDestination.Settings.route) {
            val viewModel: SettingsViewModel = viewModel()
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            SettingsScreen(
                uiState = uiState,
                onSaveCurrency = viewModel::savePreferredCurrency,
                onBack = { navController.popBackStack() },
            )
        }

        composable(AppDestination.Profile.route) {
            val viewModel: ProfileViewModel = viewModel()
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            val context = LocalContext.current
            ProfileScreen(
                uiState = uiState,
                onOpenSettings = { navController.navigate(AppDestination.Settings.route) },
                onOpenNotificationAccess = {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                },
                onSignOut = {
                    viewModel.signOut()
                    navController.navigate(AppDestination.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onAddIncomeClick = { navController.navigate(AppDestination.AddIncome.route) },
                onAddExpenseClick = { navController.navigate(AppDestination.AddExpense.route) },
                onBottomNavClick = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                currentRoute = AppDestination.Profile.route,
            )
        }
    }
}
