package com.alif.studentroutine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alif.studentroutine.ui.components.BubbleBottomBar
import com.alif.studentroutine.ui.navigation.NavGraph
import com.alif.studentroutine.ui.navigation.Screen
import com.alif.studentroutine.ui.theme.StudentRoutineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as StudentRoutineApp

        setContent {
            val darkModeEnabled by app.dataStoreManager.darkModeFlow
                .collectAsState(initial = isSystemInDarkTheme())

            StudentRoutineTheme(darkTheme = darkModeEnabled) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val backStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = backStackEntry?.destination?.route

                    val tabRoutes = setOf(
                        Screen.Dashboard.route,
                        Screen.Tasks.route,
                        Screen.NearbyLibraries.route,
                        Screen.Settings.route
                    )
                    val showBubbleBar = currentRoute in tabRoutes

                    Box(modifier = Modifier.fillMaxSize()) {
                        NavGraph(
                            navController = navController,
                            repository = app.repository,
                            dataStoreManager = app.dataStoreManager
                        )
                        if (showBubbleBar) {
                            BubbleBottomBar(
                                currentRoute = currentRoute,
                                onTabSelected = { route ->
                                    navController.navigate(route) {
                                        popUpTo(Screen.Dashboard.route) {
                                            saveState = true
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
