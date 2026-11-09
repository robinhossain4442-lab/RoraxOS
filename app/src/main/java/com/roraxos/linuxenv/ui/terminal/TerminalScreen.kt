package com.roraxos.linuxenv.ui.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.roraxos.linuxenv.core.TerminalLine
import com.roraxos.linuxenv.ui.theme.AccentGreen
import com.roraxos.linuxenv.ui.theme.ErrorRed
import kotlinx.coroutines.launch

/**
 * Shows a live-updating terminal view of a command running inside the
 * PRoot environment. [lines] is expected to be fed from
 * LinuxEnvironment.output, collected into a List by the caller's ViewModel.
 */
@Composable
fun TerminalScreen(
    toolName: String,
    lines: List<TerminalLine>,
    isRunning: Boolean,
    onBack: () -> Unit,
    onStop: () -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(lines.size - 1) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(toolName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isRunning) {
                        TextButton(onClick = onStop) {
                            Text("STOP", color = ErrorRed)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(androidx.compose.ui.graphics.Color.Black)
                .padding(12.dp)
        ) {
            LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
                items(lines) { line ->
                    Text(
                        text = line.text,
                        color = if (line.isError) ErrorRed else AccentGreen,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            if (isRunning) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
