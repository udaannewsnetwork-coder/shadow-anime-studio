package com.praveencoder.ui.features

import androidx.compose.ui.graphics.Color

data class FeatureModel(
    val title: String,
    val description: String,
    val accent: Color,
    val route: String
)

val featureCatalog = listOf(
    FeatureModel(
        title = "Voice Assistant",
        description = "Command the app through voice, record audio, and convert it to text.",
        accent = Color(0xFF4A90E2),
        route = "voice"
    ),
    FeatureModel(
        title = "Web Builder",
        description = "Generate HTML, CSS and JavaScript websites from a simple prompt.",
        accent = Color(0xFF4CAF50),
        route = "web"
    ),
    FeatureModel(
        title = "Code Studio",
        description = "Generate, explain, and fix code in multiple languages and frameworks.",
        accent = Color(0xFFFFA000),
        route = "code"
    ),
    FeatureModel(
        title = "Media Studio",
        description = "Create scripts, notes, visuals, and media-ready output for content.",
        accent = Color(0xFF9C27B0),
        route = "media"
    ),
    FeatureModel(
        title = "Permissions",
        description = "User-approved access flow, confirmation dialogs, and activity logs.",
        accent = Color(0xFF607D8B),
        route = "permissions"
    )
)
