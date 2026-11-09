package com.roraxos.linuxenv.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.roraxos.linuxenv.RoraxApp
import com.roraxos.linuxenv.core.InstallProgress
import com.roraxos.linuxenv.core.InstallStage
import com.roraxos.linuxenv.core.RootfsInstaller
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InstallUiState(
    val stage: InstallStage = InstallStage.PREPARING,
    val percent: Int = 0,
    val logLines: List<String> = emptyList(),
    val error: String? = null
)

class InstallViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as RoraxApp

    private val _uiState = MutableStateFlow(InstallUiState())
    val uiState: StateFlow<InstallUiState> = _uiState.asStateFlow()

    /** True once the install flow has finished successfully. Observe to auto-advance. */
    private val _finished = MutableStateFlow(false)
    val finished: StateFlow<Boolean> = _finished.asStateFlow()

    fun startInstall() {
        val url = app.preferences.rootfsUrl
        if (url.isNullOrBlank()) {
            _uiState.value = InstallUiState(
                stage = InstallStage.PREPARING,
                error = "No rootfs URL set. Go to Settings and set the URL to download from first."
            )
            return
        }

        viewModelScope.launch {
            val installer = RootfsInstaller(app, url)
            val log = mutableListOf<String>()
            try {
                installer.install().collect { progress: InstallProgress ->
                    progress.logLine?.let { log.add(it) }
                    _uiState.value = InstallUiState(
                        stage = progress.stage,
                        percent = progress.percent,
                        logLines = log.toList()
                    )
                    if (progress.stage == InstallStage.DONE) {
                        app.preferences.onboardingComplete = true
                        _finished.value = true
                    }
                }
            } catch (e: Exception) {
                log.add("[error] ${e.message}")
                _uiState.value = _uiState.value.copy(
                    logLines = log.toList(),
                    error = e.message ?: "Installation failed"
                )
            }
        }
    }
}
