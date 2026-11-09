package com.roraxos.linuxenv.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.roraxos.linuxenv.data.Tool
import com.roraxos.linuxenv.ui.theme.TextSecondary

data class ActivityEntry(val label: String, val timestamp: String, val success: Boolean)

@Composable
fun DashboardScreen(
    vmStatus: VmStatus,
    ramUsageMb: Int,
    tools: List<Tool>,
    recentActivity: List<ActivityEntry>,
    onStartVm: () -> Unit,
    onStopVm: () -> Unit,
    onToolClick: (Tool) -> Unit,
    onSettingsClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RoraxOS") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                VmStatusCard(
                    engineName = "PRoot (Rootless)",
                    status = vmStatus,
                    ramUsageMb = ramUsageMb,
                    onStart = onStartVm,
                    onStop = onStopVm
                )
            }

            item {
                Text("Tools", style = MaterialTheme.typography.titleMedium)
            }

            item {
                ToolsGrid(tools = tools, onToolClick = onToolClick)
            }

            item {
                Text("Recent Activity", style = MaterialTheme.typography.titleMedium)
            }

            if (recentActivity.isEmpty()) {
                item {
                    Text(
                        "No activity yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            } else {
                items(recentActivity) { entry ->
                    Card(shape = RoundedCornerShape(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(entry.label, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                entry.timestamp,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
