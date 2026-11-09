package com.roraxos.linuxenv.ui.onboarding

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.roraxos.linuxenv.ui.theme.AccentGreen

enum class Engine { PROOT_ROOTLESS, CHROOT_ROOT }

@Composable
fun EngineStep(
    selected: Engine,
    onSelect: (Engine) -> Unit,
    onContinue: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Choose Engine", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "Pick how the Linux environment will run on this device.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))

        EngineOption(
            title = "Rootless (PRoot)",
            description = "Works on any device, no root required. Recommended.",
            selected = selected == Engine.PROOT_ROOTLESS,
            onClick = { onSelect(Engine.PROOT_ROOTLESS) }
        )
        Spacer(Modifier.height(12.dp))
        EngineOption(
            title = "Root (chroot)",
            description = "Requires a rooted device. Faster, but modifies more of the system.",
            selected = selected == Engine.CHROOT_ROOT,
            onClick = { onSelect(Engine.CHROOT_ROOT) }
        )

        Spacer(Modifier.weight(1f))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("Continue")
        }
    }
}

@Composable
private fun EngineOption(title: String, description: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) AccentGreen else androidx.compose.ui.graphics.Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }
}
