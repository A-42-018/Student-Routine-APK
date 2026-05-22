package com.alif.studentroutine

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alif.studentroutine.ui.components.BubbleBottomBar
import com.alif.studentroutine.ui.navigation.NavGraph
import com.alif.studentroutine.ui.navigation.Screen
import com.alif.studentroutine.ui.theme.StudentRoutineTheme
import com.alif.studentroutine.widget.WidgetActions

class MainActivity : ComponentActivity() {

    private val widgetStartRoute = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        widgetStartRoute.value = extractStartRoute(intent)

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
                    val pendingRoute by widgetStartRoute

                    LaunchedEffect(pendingRoute) {
                        val route = pendingRoute ?: return@LaunchedEffect
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route) {
                                saveState = true
                                inclusive = false
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                        widgetStartRoute.value = null
                    }

                    val backStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = backStackEntry?.destination?.route

                    val tabRouteBases = setOf(
                        Screen.Dashboard.route,
                        Screen.Tasks.route,
                        Screen.Notes.route.substringBefore("?"),
                        Screen.Settings.route
                    )
                    val currentRouteBase = currentRoute?.substringBefore("?")
                    val showBubbleBar = currentRouteBase in tabRouteBases

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        widgetStartRoute.value = extractStartRoute(intent)
    }

    private fun extractStartRoute(intent: Intent?): String? =
        intent?.getStringExtra(WidgetActions.EXTRA_START_ROUTE)
}
