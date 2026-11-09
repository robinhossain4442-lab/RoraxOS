package com.roraxos.linuxenv.core

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Owns the lifecycle of a single PRoot-based Linux environment:
 * launching it, running a command inside it, and streaming output
 * back to callers as a Flow so the UI can render a live terminal.
 *
 * This class only knows how to run commands. It deliberately has no
 * knowledge of what any particular tool/script does — that's defined
 * entirely in data/Tool.kt + assets/tools.json.
 */
class LinuxEnvironment(private val context: Context) {

    private val rootfsDir: File
        get() = File(context.filesDir, "rootfs")

    private val homeDir: File
        get() = File(context.filesDir, "home").apply { if (!exists()) mkdirs() }

    private val prootBinary: File
        get() = File(context.applicationInfo.nativeLibraryDir, "libproot.so")

    @Volatile
    private var activeProcess: Process? = null

    private val _output = MutableSharedFlow<TerminalLine>(extraBufferCapacity = 512)
    val output: SharedFlow<TerminalLine> = _output.asSharedFlow()

    val isInstalled: Boolean
        get() = rootfsDir.exists() && File(rootfsDir, "bin").exists()

    val isRunning: Boolean
        get() = activeProcess?.isAlive == true

    /**
     * Builds the PRoot invocation that wraps a command.
     *
     * -0           fake root UID inside the sandbox (no real root needed)
     * -r <rootfs>  the extracted rootfs to use as the new root
     * -b <bind>    bind-mount a host path into the environment
     * -w <dir>     working directory inside the environment once entered
     */
    private fun buildProotCommand(command: List<String>): List<String> {
        val binds = listOf(
            "/dev" to "/dev",
            "/proc" to "/proc",
            "/sys" to "/sys",
            homeDir.absolutePath to "/root"
        )

        val cmd = mutableListOf(
            prootBinary.absolutePath,
            "-0",
            "-r", rootfsDir.absolutePath,
            "-w", "/root"
        )
        binds.forEach { (hostPath, guestPath) ->
            cmd.add("-b")
            cmd.add("$hostPath:$guestPath")
        }
        cmd.add("/usr/bin/env")
        cmd.addAll(command)
        return cmd
    }

    /**
     * Runs [command] inside the sandbox, emitting each line of
     * stdout/stderr to [output] as it arrives. Suspends until the
     * process exits and returns its exit code.
     */
    suspend fun execute(command: List<String>): Int = withContext(Dispatchers.IO) {
        check(isInstalled) { "Rootfs is not installed yet — run RootfsInstaller first." }

        val fullCommand = buildProotCommand(command)
        val builder = ProcessBuilder(fullCommand)
            .redirectErrorStream(true)
            .directory(rootfsDir)
        builder.environment()["PROOT_TMP_DIR"] = context.cacheDir.absolutePath

        val process = builder.start()
        activeProcess = process

        BufferedReader(InputStreamReader(process.inputStream)).useLines { lines ->
            lines.forEach { line ->
                _output.emit(TerminalLine(line, isError = false))
            }
        }

        val exitCode = process.waitFor()
        activeProcess = null
        _output.emit(TerminalLine("[process exited with code $exitCode]", isError = exitCode != 0))
        exitCode
    }

    /** Convenience wrapper for running a raw shell string via /bin/sh -c. */
    suspend fun executeShell(shellCommand: String): Int =
        execute(listOf("/bin/sh", "-c", shellCommand))

    /** Sanity check once the rootfs is installed — confirms PRoot can boot it. */
    suspend fun verifyEnvironment(): Int = execute(listOf("uname", "-a"))

    /** Kills any currently running command. Does not uninstall the rootfs. */
    fun stop() {
        activeProcess?.let { proc ->
            try {
                proc.destroy()
                if (proc.isAlive) proc.destroyForcibly()
            } catch (_: Exception) {
                // process already gone
            }
        }
        activeProcess = null
    }
}

data class TerminalLine(val text: String, val isError: Boolean)
