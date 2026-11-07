package com.praveencoder.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebBuilderScreen(navController: NavController) {
    val context = LocalContext.current
    var prompt by remember { mutableStateOf("Create a portfolio landing page for a designer") }
    var generatedHtml by remember { mutableStateOf("") }
    val encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8.toString())

    Scaffold(topBar = { TopAppBar(title = { Text("Web Builder") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Website generator (HTML/CSS/JS)")
            OutlinedTextField(value = prompt, onValueChange = { prompt = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Design prompt") })
            Button(onClick = {
                generatedHtml = "<!-- Prompt: $prompt -->\n<!doctype html>\n<html><head><meta name=\"viewport\" content=\"width=device-width\"><style>body{font-family:sans-serif;padding:2rem}button{padding:.7rem 1rem}</style></head><body><h1>Praveencoder Website</h1><p>Your generated preview starts here.</p><button>Get started</button></body></html>"
            }, modifier = Modifier.fillMaxWidth()) { Text("Generate starter website") }
            if (generatedHtml.isNotEmpty()) {
                Text("Preview generated. Connect an AI provider to replace the starter template.")
                Button(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$encodedPrompt")))
                }, modifier = Modifier.fillMaxWidth()) { Text("Research this website idea on Google") }
            }
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) { Text("Back") }
        }
    }
}
