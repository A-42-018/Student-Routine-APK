package com.alif.studentroutine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.alif.studentroutine.ui.navigation.NavGraph
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
                    NavGraph(
                        navController = navController,
                        repository = app.repository,
                        dataStoreManager = app.dataStoreManager
                    )
                }
            }
        }
    }
}
