package com.roraxos.linuxenv.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.roraxos.linuxenv.core.AppPreferences

@Composable
fun SettingsScreen(
    preferences: AppPreferences,
    onBack: () -> Unit,
    onReinstall: () -> Unit
) {
    var url by remember { mutableStateOf(preferences.rootfsUrl ?: "") }
    var saved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Text("Rootfs source", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "The URL of the Linux rootfs tarball this app installs. Point it at your own hosted rootfs (e.g. a GitHub Release asset).",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = url,
                onValueChange = { url = it; saved = false },
                label = { Text("Rootfs URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    preferences.rootfsUrl = url.trim()
                    saved = true
                },
                enabled = url.isNotBlank()
            ) {
                Text("Save")
            }
            if (saved) {
                Spacer(Modifier.height(8.dp))
                Text("Saved.", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(32.dp))
            Divider()
            Spacer(Modifier.height(20.dp))

            Text("Environment", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Re-download and re-extract the rootfs. Existing data inside it will be removed.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onReinstall) {
                Text("Reinstall environment")
            }
        }
    }
}
