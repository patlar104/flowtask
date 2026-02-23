package com.patrick.flowtask.app

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.patrick.flowtask.R
import com.patrick.flowtask.ui.screens.ConversationScreen
import com.patrick.flowtask.ui.screens.HomeScreen
import com.patrick.flowtask.ui.screens.SettingsScreen
import com.patrick.flowtask.ui.screens.TasksScreen
import com.patrick.flowtask.ui.viewmodel.ConversationViewModel
import com.patrick.flowtask.ui.viewmodel.ConversationViewModelFactory
import com.patrick.flowtask.ui.viewmodel.HomeViewModel
import com.patrick.flowtask.ui.viewmodel.HomeViewModelFactory
import com.patrick.flowtask.ui.viewmodel.SettingsViewModel
import com.patrick.flowtask.ui.viewmodel.TasksViewModel
import com.patrick.flowtask.ui.viewmodel.TasksViewModelFactory

private enum class AppDestination(
    val route: String,
    val labelRes: Int
) {
    HOME("home", R.string.nav_home),
    TASKS("tasks", R.string.nav_tasks),
    CONVERSATION("conversation", R.string.nav_conversational_flow),
    SETTINGS("settings", R.string.nav_settings)
}

@Composable
fun FlowTaskApp(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val destinations = AppDestination.entries
    val isWideLayout = LocalConfiguration.current.screenWidthDp >= 600

    if (isWideLayout) {
        Row(modifier = modifier.fillMaxSize()) {
            NavigationRail {
                destinations.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                    NavigationRailItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(stringResource(destination.labelRes).take(1)) },
                        label = { Text(stringResource(destination.labelRes)) }
                    )
                }
            }
            AppNavHost(
                container = container,
                modifier = Modifier.fillMaxSize(),
                navController = navController
            )
        }
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    destinations.forEach { destination ->
                        val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Text(stringResource(destination.labelRes).take(1)) },
                            label = { Text(stringResource(destination.labelRes)) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            AppNavHost(
                container = container,
                modifier = Modifier.padding(innerPadding),
                navController = navController
            )
        }
    }
}

@Composable
private fun AppNavHost(
    container: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(container.taskRepository)
    )
    val tasksViewModel: TasksViewModel = viewModel(
        factory = TasksViewModelFactory(container.taskRepository)
    )
    val conversationViewModel: ConversationViewModel = viewModel(
        factory = ConversationViewModelFactory(container.taskRepository, container.conversationUseCase)
    )
    val settingsViewModel: SettingsViewModel = viewModel()

    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val tasksState by tasksViewModel.uiState.collectAsStateWithLifecycle()
    val conversationState by conversationViewModel.uiState.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = AppDestination.HOME.route,
        modifier = modifier.fillMaxSize()
    ) {
        composable(AppDestination.HOME.route) {
            HomeScreen(state = homeState)
        }
        composable(AppDestination.TASKS.route) {
            TasksScreen(
                state = tasksState,
                onQueryChange = tasksViewModel::onQueryChange,
                onAddTask = { tasksViewModel.addTask(it) },
                onToggleTask = tasksViewModel::toggleTask,
                onDeleteTask = tasksViewModel::deleteTask
            )
        }
        composable(AppDestination.CONVERSATION.route) {
            ConversationScreen(
                state = conversationState,
                onSubmit = conversationViewModel::submitMessage
            )
        }
        composable(AppDestination.SETTINGS.route) {
            SettingsScreen(state = settingsState)
        }
    }
}
