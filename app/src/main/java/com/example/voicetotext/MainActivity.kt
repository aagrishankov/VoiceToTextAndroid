package com.example.voicetotext

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {

    private val voiceToTextParser by lazy {
        VoiceToTextParser(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            var isAccessRecord by remember { mutableStateOf(false) }

            val audioLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    isAccessRecord = isGranted
                }
            )

            LaunchedEffect(audioLauncher) {
                audioLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }

            val state by voiceToTextParser.state.collectAsState()

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            if (state.isSpeaking)
                                voiceToTextParser.stopListening()
                            else
                                voiceToTextParser.startListening()
                        },
                        content = {
                            AnimatedContent(
                                targetState = state.isSpeaking,
                                label = "VoiceToTextAnimateIcon"
                            ) { isSpeaking ->
                                if (isSpeaking)
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                    )
                                else
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                    )
                            }
                        }
                    )
                },
                content = { innerPadding ->
                    Column(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        AnimatedContent(
                            targetState = state.isSpeaking,
                            label = "VoiceToTextAnimateText"
                        ) { isSpeaking ->
                            if (isSpeaking) {
                                Text(text = "Распознавание речи...")
                            } else {
                                Text(text = state.result
                                    .ifEmpty { state.error.orEmpty() }
                                    .ifEmpty { "Нажмите на кнопку для распознавания речи" })
                            }
                        }
                    }
                }
            )
        }
    }
}
