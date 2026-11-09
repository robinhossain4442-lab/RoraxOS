package com.roraxos.linuxenv.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class DisclaimerCheck(val text: String, val checked: Boolean)

@Composable
fun WelcomeStep(
    features: List<String>,
    onContinue: () -> Unit
) {
    var check1 by remember { mutableStateOf(false) }
    var check2 by remember { mutableStateOf(false) }
    var check3 by remember { mutableStateOf(false) }

    val allChecked = check1 && check2 && check3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Welcome", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "This app runs a local, non-root Linux environment on your device and gives you a dashboard for command-line tools you configure yourself.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(20.dp))
        Text("Features", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        features.forEach { feature ->
            Text("• $feature", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(24.dp))
        Text("Before you continue", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        CheckboxRow(
            text = "I will only use this app, and any tools I configure, on systems and networks I own or am explicitly authorized to test.",
            checked = check1,
            onCheckedChange = { check1 = it }
        )
        CheckboxRow(
            text = "I understand this app provides a generic Linux sandbox and script runner, and that I am responsible for any tools or scripts I add to it.",
            checked = check2,
            onCheckedChange = { check2 = it }
        )
        CheckboxRow(
            text = "I understand misuse of network or security tools against systems without authorization may be illegal in my jurisdiction.",
            checked = check3,
            onCheckedChange = { check3 = it }
        )

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onContinue,
            enabled = allChecked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Installation")
        }
    }
}

@Composable
private fun CheckboxRow(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}
