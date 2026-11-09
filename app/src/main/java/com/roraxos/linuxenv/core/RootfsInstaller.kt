package com.roraxos.linuxenv.core

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream

enum class InstallStage { PREPARING, DOWNLOADING, EXTRACTING, FINALIZING, DONE }

data class InstallProgress(
    val stage: InstallStage,
    val percent: Int = 0,
    val logLine: String? = null
)

/**
 * Downloads a rootfs tarball (e.g. a Debian/Ubuntu arm64 rootfs hosted on
 * GitHub Releases) and extracts it into the app's internal storage so
 * LinuxEnvironment can boot it with PRoot.
 *
 * NOTE: set [rootfsUrl] to a rootfs you are entitled to redistribute/use —
 * this class does not bundle or point at any specific tarball itself.
 */
class RootfsInstaller(
    private val context: Context,
    private val rootfsUrl: String
) {
    private val rootfsDir: File get() = File(context.filesDir, "rootfs")
    private val tarballFile: File get() = File(context.cacheDir, "rootfs.tar.gz")

    fun install(): Flow<InstallProgress> = flow {
        emit(InstallProgress(InstallStage.PREPARING, 0, "Preparing installation..."))
        if (rootfsDir.exists()) rootfsDir.deleteRecursively()
        rootfsDir.mkdirs()

        emit(InstallProgress(InstallStage.DOWNLOADING, 0, "Connecting to $rootfsUrl"))
        downloadWithProgress()

        emit(InstallProgress(InstallStage.EXTRACTING, 0, "Extracting rootfs..."))
        extractWithProgress()

        emit(InstallProgress(InstallStage.FINALIZING, 95, "Setting permissions..."))
        finalize()

        tarballFile.delete()
        emit(InstallProgress(InstallStage.DONE, 100, "Installation complete."))
    }.flowOn(Dispatchers.IO)

    private suspend fun kotlinx.coroutines.flow.FlowCollector<InstallProgress>.downloadWithProgress() {
        val connection = URL(rootfsUrl).openConnection() as HttpURLConnection
        connection.connect()

        val totalBytes = connection.contentLengthLong.coerceAtLeast(1L)
        var downloaded = 0L

        connection.inputStream.use { input ->
            FileOutputStream(tarballFile).use { output ->
                val buffer = ByteArray(64 * 1024)
                var lastPercent = -1
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    output.write(buffer, 0, read)
                    downloaded += read

                    val percent = ((downloaded * 100) / totalBytes).toInt().coerceIn(0, 100)
                    if (percent != lastPercent) {
                        lastPercent = percent
                        emit(
                            InstallProgress(
                                InstallStage.DOWNLOADING,
                                percent,
                                "Downloaded ${downloaded / (1024 * 1024)}MB / ${totalBytes / (1024 * 1024)}MB"
                            )
                        )
                    }
                }
            }
        }
        connection.disconnect()
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<InstallProgress>.extractWithProgress() {
        // Rough total-entry estimate isn't known ahead of time with a streamed
        // tar.gz, so we report by entry count processed rather than true percent.
        var entries = 0
        TarArchiveInputStream(GZIPInputStream(tarballFile.inputStream())).use { tarStream ->
            var entry = tarStream.nextTarEntry
            while (entry != null) {
                val outFile = File(rootfsDir, entry.name)
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { out -> tarStream.copyTo(out) }
                    if (entry.mode and 0b001001001 != 0) {
                        outFile.setExecutable(true, false)
                    }
                }
                entries++
                if (entries % 200 == 0) {
                    emit(InstallProgress(InstallStage.EXTRACTING, logLine = "Extracted $entries entries..."))
                }
                entry = tarStream.nextTarEntry
            }
        }
    }

    private fun finalize() {
        // Ensure standard directories exist even if the tarball was minimal.
        listOf("dev", "proc", "sys", "tmp", "root").forEach { name ->
            File(rootfsDir, name).mkdirs()
        }
    }
}
