package com.praveencoder

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.praveencoder.ui.screens.CodeStudioScreen
import com.praveencoder.ui.screens.HomeScreen
import com.praveencoder.ui.screens.MediaStudioScreen
import com.praveencoder.ui.screens.PermissionsScreen
import com.praveencoder.ui.screens.VoiceAssistantScreen
import com.praveencoder.ui.screens.WebBuilderScreen

@Composable
fun PraveencoderApp() {
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(navController = navController)
            }
            composable("voice") {
                VoiceAssistantScreen(navController = navController)
            }
            composable("web") {
                WebBuilderScreen(navController = navController)
            }
            composable("code") {
                CodeStudioScreen(navController = navController)
            }
            composable("media") {
                MediaStudioScreen(navController = navController)
            }
            composable("permissions") {
                PermissionsScreen(navController = navController)
            }
        }
    }
}
