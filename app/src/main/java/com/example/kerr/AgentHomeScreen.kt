package com.example.kerr.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.kerr.VoiceInputManager


@Composable
fun AgentHomeScreen(
    onExecuteCommand: (String) -> Unit= {}
) {
    val context= LocalContext.current
    val voiceManager=remember { VoiceInputManager(context) }
    var commandText by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        onDispose { voiceManager.destroy() }
    }

    LaunchedEffect(voiceManager.spokenText) {
        if (voiceManager.spokenText.isNotEmpty()) {
            commandText=voiceManager.spokenText
        }
    }

    val permissionLauncher= rememberLauncherForActivityResult(
        contract= ActivityResultContracts.RequestPermission()
    )
    {
        isGranted->
        if (isGranted){
            voiceManager.startListening()
        }
    }
    val normalizedVoiceLevel by animateFloatAsState(
        targetValue = if (voiceManager.isListening) {
            (voiceManager.rmsDb / 10f).coerceIn(0f, 1f)
        } else {
            0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "voiceEarReactivity"
    )

    val isListening = voiceManager.isListening
    val backgroundColor= Color(0xFFFFF8F0)
    val accentOrange= Color(0xFFE87A3E)

    Column(modifier = Modifier
        .fillMaxSize()
        .background(backgroundColor)
        .imePadding()
        .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(
            text = if (isListening) "Listening closely..." else "Tap mic or type a task",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color=if (isListening) accentOrange else Color(0xFF6D5D50)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // PLACEHOLDER FOR CHUNK 2: Cute Fennec Fox Face
        Box(
            modifier = Modifier
                .size(220.dp)
                .background(Color(0xFFF3E5D8), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            FennecFoxFace(isListening = isListening)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Next: we will add the Mic button right here!

        FilledIconButton(
            onClick = {
                if (isListening) {
                    voiceManager.stopListening()
                } else {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        voiceManager.startListening()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            },
            modifier = Modifier
                .size(72.dp)
                .shadow(8.dp, CircleShape),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (isListening) Color(0xFFD9381E) else accentOrange,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = "Toggle Microphone",
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = commandText,
            onValueChange = {commandText=it},
            modifier = Modifier.fillMaxWidth(),
            placeholder = {Text("Give Command")},
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentOrange,
                unfocusedBorderColor = Color(0xFFD6C5B5),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            trailingIcon = {
                IconButton(onClick = {if (commandText.isNotBlank()){
                    onExecuteCommand(commandText)
                }
                },
                    enabled = commandText.isNotBlank()
                    )
                {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Run Command",
                        tint = if (commandText.isNotBlank()) accentOrange else Color.Gray
                    )
                }
            }
        )
    }
}
