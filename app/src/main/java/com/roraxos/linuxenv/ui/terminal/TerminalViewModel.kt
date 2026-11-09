package com.roraxos.linuxenv.ui.terminal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.roraxos.linuxenv.RoraxApp
import com.roraxos.linuxenv.core.TerminalLine
import com.roraxos.linuxenv.data.Tool
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TerminalViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as RoraxApp

    private val _lines = MutableStateFlow<List<TerminalLine>>(emptyList())
    val lines: StateFlow<List<TerminalLine>> = _lines.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var runJob: Job? = null

    /** Runs [tool]'s configured command inside the sandbox. */
    fun run(tool: Tool) {
        if (_isRunning.value) return
        _lines.value = emptyList()

        if (!app.environment.isInstalled) {
            _lines.value = listOf(
                TerminalLine("[error] Linux environment is not installed yet. Run setup from the dashboard first.", isError = true)
            )
            return
        }

        runJob = viewModelScope.launch {
            _isRunning.value = true

            val collector = launch {
                app.environment.output.collect { line ->
                    _lines.value = _lines.value + line
                }
            }

            try {
                app.environment.executeShell(tool.command)
            } catch (e: Exception) {
                _lines.value = _lines.value + TerminalLine("[error] ${e.message}", isError = true)
            } finally {
                collector.cancel()
                _isRunning.value = false
            }
        }
    }

    fun stop() {
        app.environment.stop()
        runJob?.cancel()
        _isRunning.value = false
    }

    override fun onCleared() {
        super.onCleared()
        if (_isRunning.value) {
            app.environment.stop()
        }
    }
}
