package io.github.sushaant.messq.ui.customer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.sushaant.messq.data.model.SlotDto
import io.github.sushaant.messq.state.customer.StudentUiState
import io.github.sushaant.messq.state.staff.StaffUiState
import io.github.sushaant.messq.ui.staff.StaffRoute
import io.github.sushaant.messq.ui.staff.StaffScreen
import io.github.sushaant.messq.ui.theme.MessQTheme
import io.github.sushaant.messq.ui.theme.MonotypeCorsiva

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(
    uiState: StudentUiState,
    onBookSlot: (SlotDto) -> Unit,
    onQrPayloadDetected: (String) -> Unit,
    onRefreshData: () -> Unit,
    onClearMessages: () -> Unit,
    onToggleSelectMenuItem: (String) -> Unit,
    onRecommendMeals: (String, Int?, Double?) -> Unit,
    onLogout: () -> Unit
) {
    val innerNavController = rememberNavController()
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: StudentRoute.Slots.route
    val snackbarHostState = remember { SnackbarHostState() }
    val isInPreview = LocalInspectionMode.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        val msg = uiState.errorMessage ?: uiState.successMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            onClearMessages()
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out of MessQ?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    Column {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontFamily = MonotypeCorsiva,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 28.sp
                                    )
                                ) {
                                    append("MessQ ")
                                }
                                withStyle(
                                    SpanStyle(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                ) {
                                    append(" Dining")
                                }
                            },
                            style = MaterialTheme.typography.titleLarge
                        )
                        uiState.latestAiPrediction?.let { pred ->
                            Text(
                                text = "Crowd: ${pred.crowd_level} • ~${pred.expected_wait_minutes} min wait",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onRefreshData) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 10.dp
            ) {
                val items = listOf(
                    Triple(StudentRoute.Slots, "Slots", Icons.Default.EventSeat),
                    Triple(StudentRoute.Menu, "Menu", Icons.Default.RestaurantMenu),
                    Triple(StudentRoute.Scan, "Scan QR", Icons.Default.QrCodeScanner),
                    Triple(StudentRoute.Suggest, "AI Picks", Icons.Default.AutoAwesome)
                )
                val itemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )

                items.forEach { (dest, label, icon) ->
                    NavigationBarItem(
                        selected = currentRoute == dest.route,
                        onClick = {
                            innerNavController.navigate(dest.route) {
                                popUpTo(innerNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        colors = itemColors
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }

            if (isInPreview) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Preview: ${StudentRoute.Menu.route}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                StudentNavGraph(
                    navController = innerNavController,
                    uiState = uiState,
                    onBookSlot = onBookSlot,
                    onQrPayloadDetected = onQrPayloadDetected,
                    onToggleSelectMenuItem = onToggleSelectMenuItem,
                    onRecommendMeals = onRecommendMeals
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ShowStaffScreen() {
    MessQTheme {
        StudentScreen(
            uiState = StudentUiState(
                isLoading = false,
            ),
            onBookSlot = {},
            onQrPayloadDetected = {},
            onRefreshData = {},
            onClearMessages = {},
            onRecommendMeals = { _, _, _ -> },
            onToggleSelectMenuItem = {},
            onLogout = {}
        )
    }
}