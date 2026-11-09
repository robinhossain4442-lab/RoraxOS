package com.roraxos.linuxenv.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.roraxos.linuxenv.data.Tool
import com.roraxos.linuxenv.ui.theme.AccentGreen
import com.roraxos.linuxenv.ui.theme.WarningAmber

@Composable
fun AppDrawerContent(
    toolsByCategory: Map<String, List<Tool>>,
    onToolClick: (Tool) -> Unit,
    onSettingsClick: () -> Unit
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("RoraxOS", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            toolsByCategory.forEach { (category, tools) ->
                Text(
                    category.uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentGreen,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                tools.forEach { tool ->
                    NavigationDrawerItem(
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tool.name)
                                if (tool.requiresRoot) {
                                    Spacer(Modifier.width(6.dp))
                                    RootBadge()
                                }
                            }
                        },
                        selected = false,
                        onClick = { onToolClick(tool) },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            Divider()
            NavigationDrawerItem(
                label = { Text("Settings") },
                selected = false,
                onClick = onSettingsClick,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun RootBadge() {
    Box(
        modifier = Modifier
            .background(WarningAmber.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text("ROOT", style = MaterialTheme.typography.labelSmall, color = WarningAmber)
    }
}
