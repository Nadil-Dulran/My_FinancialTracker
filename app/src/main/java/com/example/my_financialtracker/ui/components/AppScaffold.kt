package com.example.my_financialtracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.my_financialtracker.navigation.bottomNavItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    currentRoute: String?,
    showBottomBar: Boolean,
    onBottomNavClick: (String) -> Unit,
    onAddIncomeClick: (() -> Unit)? = null,
    onAddExpenseClick: (() -> Unit)? = null,
    showTopBar: Boolean = true,
    floatingActionButton: @Composable (() -> Unit)? = null,
    content: @Composable (Modifier) -> Unit,
    ) {
    var isAddMenuExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AmbientBackground()

        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                if (showTopBar) {
                    CenterAlignedTopAppBar(
                        title = { Text(title) },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        ),
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    Surface(
                        tonalElevation = 0.dp,
                        shadowElevation = 10.dp,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom,
                            ) {
                                bottomNavItems.forEach { item ->
                                    if (item.isAddAction) {
                                        BottomBarAddButton(
                                            expanded = isAddMenuExpanded,
                                            label = stringResource(item.labelRes),
                                            onClick = {
                                                isAddMenuExpanded = !isAddMenuExpanded
                                            },
                                            onAddIncomeClick = if (onAddIncomeClick != null && onAddExpenseClick != null) {
                                                {
                                                    isAddMenuExpanded = false
                                                    onAddIncomeClick.invoke()
                                                }
                                            } else {
                                                null
                                            },
                                            onAddExpenseClick = if (onAddIncomeClick != null && onAddExpenseClick != null) {
                                                {
                                                    isAddMenuExpanded = false
                                                    onAddExpenseClick.invoke()
                                                }
                                            } else {
                                                null
                                            },
                                        )
                                    } else {
                                        BottomBarItem(
                                            label = stringResource(item.labelRes),
                                            icon = {
                                                Icon(
                                                    item.icon,
                                                    contentDescription = stringResource(item.labelRes),
                                                )
                                            },
                                            selected = currentRoute == item.route,
                                            onClick = {
                                                isAddMenuExpanded = false
                                                onBottomNavClick(item.route)
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                floatingActionButton?.invoke()
            },
        ) { innerPadding ->
            content(
                Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun AddShortcutFab(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    SmallFloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(icon, contentDescription = null)
            Text(label)
        }
    }
}

@Composable
private fun BottomBarItem(
    label: String,
    icon: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .widthIn(min = 64.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    } else {
                        androidx.compose.ui.graphics.Color.Transparent
                    },
                    shape = CircleShape,
                )
                .padding(horizontal = 18.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            ) {
                icon()
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun BottomBarAddButton(
    expanded: Boolean,
    label: String,
    onClick: () -> Unit,
    onAddIncomeClick: (() -> Unit)?,
    onAddExpenseClick: (() -> Unit)?,
) {
    Column(
        modifier = Modifier.widthIn(min = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        color = if (expanded) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        },
                        shape = CircleShape,
                    )
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = label,
                    tint = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
            }

            DropdownMenu(
                expanded = expanded && onAddIncomeClick != null && onAddExpenseClick != null,
                onDismissRequest = onClick,
            ) {
                DropdownMenuItem(
                    text = { Text("Income") },
                    leadingIcon = {
                        Icon(Icons.Outlined.ArrowUpward, contentDescription = null)
                    },
                    onClick = {
                        onAddIncomeClick?.invoke()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Expense") },
                    leadingIcon = {
                        Icon(Icons.Outlined.ArrowDownward, contentDescription = null)
                    },
                    onClick = {
                        onAddExpenseClick?.invoke()
                    },
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(1.dp))
    }
}
