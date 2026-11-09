package com.roraxos.linuxenv

import android.app.Application
import com.roraxos.linuxenv.core.AppPreferences
import com.roraxos.linuxenv.core.LinuxEnvironment
import com.roraxos.linuxenv.data.ToolRepository

/**
 * Holds app-wide singletons so the Dashboard, Terminal screens, and the
 * foreground LinuxService all operate on the SAME LinuxEnvironment and
 * see consistent running/stopped state.
 */
class RoraxApp : Application() {

    val preferences: AppPreferences by lazy { AppPreferences(this) }
    val environment: LinuxEnvironment by lazy { LinuxEnvironment(this) }
    val toolRepository: ToolRepository by lazy { ToolRepository(this) }
}
