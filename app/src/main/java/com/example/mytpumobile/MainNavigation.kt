package com.example.mytpumobile

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class BottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomTab("home", "Главная", Icons.Rounded.Home, Icons.Outlined.Home)
    object Services : BottomTab("services", "Сервисы", Icons.Rounded.GridView, Icons.Outlined.GridView)
    object Assistant : BottomTab("assistant", "Помощник", Icons.Rounded.ChatBubble, Icons.Outlined.ChatBubbleOutline)
    object Profile : BottomTab("profile", "Профиль", Icons.Rounded.Person, Icons.Outlined.PersonOutline)
}

@Composable
fun MainAppScaffold(user: UserSession, viewModel: AuthViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val tabs = listOf(BottomTab.Home, BottomTab.Services, BottomTab.Assistant, BottomTab.Profile)
    // Скрываем нижний бар только на экранах ввода формы/деталей, чтобы не мешал клавиатуре
    val showBottomBar = currentRoute !in listOf("customize_board", "create_ticket") &&
            currentRoute?.startsWith("ticket_detail/") != true

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = (route != BottomTab.Home.route)
            }
            launchSingleTop = true
            restoreState = (route != BottomTab.Home.route)
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEach { tab ->
                        val selected = currentRoute == tab.route ||
                                (tab == BottomTab.Services && currentRoute == "tickets")

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != tab.route) {
                                    navigateToTab(tab.route)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomTab.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomTab.Home.route) {
                HomeScreen(
                    user = user,
                    viewModel = viewModel,
                    onOpenCustomizeBoard = { navController.navigate("customize_board") },
                    onOpenTickets = { navController.navigate("tickets") }
                )
            }
            composable("customize_board") {
                CustomizeBoardScreen(
                    viewModel = viewModel,
                    onDone = { navController.popBackStack() }
                )
            }
            composable(BottomTab.Services.route) {
                ServicesScreen(
                    onOpenTickets = { navController.navigate("tickets") },
                    onOpenAssistantWithPrompt = { prompt ->
                        viewModel.updateMLInput(prompt)
                        viewModel.sendToNeuralNetwork()
                        navigateToTab(BottomTab.Assistant.route)
                    }
                )
            }
            composable("tickets") {
                TicketsListScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOpenTicketDetail = { id -> navController.navigate("ticket_detail/$id") },
                    onOpenCreateTicket = { navController.navigate("create_ticket") },
                    onAskAssistant = { navigateToTab(BottomTab.Assistant.route) }
                )
            }
            composable("ticket_detail/{ticketId}") { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId") ?: return@composable
                TicketDetailScreen(
                    ticketId = ticketId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("create_ticket") {
                CreateTicketScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(BottomTab.Assistant.route) {
                AssistantScreen(viewModel = viewModel)
            }
            composable(BottomTab.Profile.route) {
                ProfileScreen(user = user, viewModel = viewModel)
            }
        }
    }
}