package com.roraxos.linuxenv.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.roraxos.linuxenv.core.InstallStage
import com.roraxos.linuxenv.ui.theme.AccentGreen
import com.roraxos.linuxenv.ui.theme.TextSecondary

@Composable
fun InstallStep(
    currentStage: InstallStage,
    percent: Int,
    logLines: List<String>,
    error: String? = null
) {
    val stages = listOf(
        InstallStage.PREPARING to "Preparing",
        InstallStage.DOWNLOADING to "Downloading",
        InstallStage.EXTRACTING to "Extracting",
        InstallStage.FINALIZING to "Finalizing",
        InstallStage.DONE to "Done"
    )

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Setting up your environment", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = percent / 100f,
            modifier = Modifier.fillMaxWidth(),
            color = AccentGreen
        )
        Spacer(Modifier.height(20.dp))

        stages.forEach { (stage, label) ->
            StageRow(
                label = label,
                state = when {
                    stage.ordinal < currentStage.ordinal -> StageState.DONE
                    stage.ordinal == currentStage.ordinal -> StageState.ACTIVE
                    else -> StageState.PENDING
                }
            )
        }

        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                error,
                color = androidx.compose.ui.graphics.Color(0xFFFF5252),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(20.dp))
        Text("Log", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
                .padding(10.dp)
        ) {
            LazyColumn {
                items(logLines) { line ->
                    Text(
                        line,
                        color = AccentGreen,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

private enum class StageState { PENDING, ACTIVE, DONE }

@Composable
private fun StageRow(label: String, state: StageState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        when (state) {
            StageState.DONE -> Icon(Icons.Filled.Check, contentDescription = null, tint = AccentGreen)
            StageState.ACTIVE -> CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            StageState.PENDING -> Box(modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            color = if (state == StageState.PENDING) TextSecondary else Color.Unspecified
        )
    }
}
