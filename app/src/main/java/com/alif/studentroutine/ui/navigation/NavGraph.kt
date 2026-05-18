package com.alif.studentroutine.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.alif.studentroutine.data.DataStoreManager
import com.alif.studentroutine.data.repository.RoutineRepository
import com.alif.studentroutine.ui.screens.*

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Timetable : Screen("timetable")
    data object Tasks : Screen("tasks")
    data object AddClass : Screen("add_class?classId={classId}") {
        fun createRoute(classId: Int = -1) = "add_class?classId=$classId"
    }
    data object AddTask : Screen("add_task?taskId={taskId}") {
        fun createRoute(taskId: Int = -1) = "add_task?taskId=$taskId"
    }
    data object Settings : Screen("settings")
    data object NearbyLibraries : Screen("nearby_libraries")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    repository: RoutineRepository,
    dataStoreManager: DataStoreManager
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                repository = repository,
                onNavigateToTimetable = { navController.navigate(Screen.Timetable.route) },
                onNavigateToAddClass = { navController.navigate(Screen.AddClass.createRoute()) },
                onNavigateToAddTask = { navController.navigate(Screen.AddTask.createRoute()) }
            )
        }

        composable(Screen.Timetable.route) {
            TimetableScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() },
                onAddClass = { navController.navigate(Screen.AddClass.createRoute()) },
                onEditClass = { classId ->
                    navController.navigate(Screen.AddClass.createRoute(classId))
                }
            )
        }

        composable(Screen.Tasks.route) {
            TasksScreen(
                repository = repository,
                onAddTask = { navController.navigate(Screen.AddTask.createRoute()) },
                onEditTask = { taskId ->
                    navController.navigate(Screen.AddTask.createRoute(taskId))
                }
            )
        }

        composable(
            route = Screen.AddClass.route,
            arguments = listOf(navArgument("classId") { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getInt("classId") ?: -1
            AddEditClassScreen(
                repository = repository,
                dataStoreManager = dataStoreManager,
                classId = classId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddTask.route,
            arguments = listOf(navArgument("taskId") { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: -1
            AddEditTaskScreen(
                repository = repository,
                dataStoreManager = dataStoreManager,
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                dataStoreManager = dataStoreManager,
                repository = repository,
                onNavigateToNearbyLibraries = { navController.navigate(Screen.NearbyLibraries.route) }
            )
        }

        composable(Screen.NearbyLibraries.route) {
            NearbyLibrariesScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
