package com.praveencoder.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistantScreen(navController: NavController) {
    val context = LocalContext.current
    var command by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Ready") }

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (!matches.isNullOrEmpty()) {
            command = matches.first()
            status = "Voice command received"
        } else {
            status = "No speech detected"
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Praveencoder ko command boliye")
            }
            speechLauncher.launch(intent)
        } else status = "Microphone permission denied"
    }

    fun startVoiceInput() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                }
                speechLauncher.launch(intent)
            } else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else status = "Speech recognition is unavailable on this device"
    }

    fun searchGoogle() {
        val query = command.trim()
        if (query.isEmpty()) {
            status = "Enter a search command first"
            return
        }
        val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$encoded")))
        status = "Google search opened"
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Voice Assistant") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Hindi/English voice commands", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = command,
                onValueChange = { command = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Command or Google search") }
            )
            Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = ::startVoiceInput, modifier = Modifier.fillMaxWidth()) { Text("🎙 Record voice command") }
            Button(onClick = ::searchGoogle, modifier = Modifier.fillMaxWidth()) { Text("Search Google") }
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) { Text("Back") }
        }
    }
}
