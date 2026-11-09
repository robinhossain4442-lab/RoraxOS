package com.roraxos.linuxenv.ui.dashboard

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.roraxos.linuxenv.RoraxApp
import com.roraxos.linuxenv.data.Tool
import com.roraxos.linuxenv.service.LinuxService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as RoraxApp

    private val _vmStatus = MutableStateFlow(VmStatus.STOPPED)
    val vmStatus: StateFlow<VmStatus> = _vmStatus.asStateFlow()

    private val _ramUsageMb = MutableStateFlow(0)
    val ramUsageMb: StateFlow<Int> = _ramUsageMb.asStateFlow()

    private val _recentActivity = MutableStateFlow<List<ActivityEntry>>(emptyList())
    val recentActivity: StateFlow<List<ActivityEntry>> = _recentActivity.asStateFlow()

    val tools: List<Tool> get() = app.toolRepository.loadTools()

    fun startVm() {
        if (!app.environment.isInstalled) return
        _vmStatus.value = VmStatus.STARTING
        val intent = Intent(app, LinuxService::class.java)
        ContextCompat.startForegroundService(app, intent)
        _vmStatus.value = VmStatus.RUNNING
        pollRam()
    }

    fun stopVm() {
        val intent = Intent(app, LinuxService::class.java).apply {
            action = LinuxService.ACTION_STOP
        }
        app.startService(intent)
        _vmStatus.value = VmStatus.STOPPED
        _ramUsageMb.value = 0
    }

    fun recordActivity(label: String, success: Boolean) {
        val entry = ActivityEntry(
            label = label,
            timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date()),
            success = success
        )
        _recentActivity.value = (listOf(entry) + _recentActivity.value).take(10)
    }

    /**
     * Approximate memory usage by sampling device-wide available memory.
     * PRoot processes don't expose a clean per-sandbox RSS total without
     * walking /proc entries for each pid, so this is a coarse indicator,
     * not a precise figure for the sandbox alone.
     */
    private fun pollRam() {
        viewModelScope.launch {
            val am = app.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            while (_vmStatus.value == VmStatus.RUNNING) {
                val info = ActivityManager.MemoryInfo()
                am.getMemoryInfo(info)
                val usedMb = ((info.totalMem - info.availMem) / (1024 * 1024)).toInt()
                _ramUsageMb.value = usedMb
                delay(3000)
            }
        }
    }
}
