package com.roraxos.linuxenv.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * This app doesn't bundle or point at any specific rootfs — the user
 * supplies their own tarball URL (e.g. a GitHub Release asset they
 * control) before the installer runs.
 */
@Composable
fun RootfsUrlStep(
    initialUrl: String,
    onContinue: (String) -> Unit
) {
    var url by remember { mutableStateOf(initialUrl) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Rootfs source", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "Paste the URL of the Linux rootfs tarball (.tar.gz) to install — for example, a release asset from your own GitHub repo. This app doesn't ship with one built in.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Rootfs URL") },
            placeholder = { Text("https://github.com/you/repo/releases/download/v1/rootfs.tar.gz") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.weight(1f))
        Button(
            onClick = { onContinue(url.trim()) },
            enabled = url.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue")
        }
    }
}
