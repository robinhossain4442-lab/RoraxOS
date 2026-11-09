package com.roraxos.linuxenv.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.roraxos.linuxenv.MainActivity
import com.roraxos.linuxenv.R
import com.roraxos.linuxenv.RoraxApp
import com.roraxos.linuxenv.core.LinuxEnvironment

/**
 * Foreground service that keeps the shared [LinuxEnvironment] (owned by
 * [RoraxApp]) alive while the user is running tools, independent of
 * whether the Activity is on screen. Android requires a visible
 * notification for any foreground service.
 */
class LinuxService : Service() {

    companion object {
        const val CHANNEL_ID = "linux_environment_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.roraxos.linuxenv.action.STOP"
    }

    private val binder = LocalBinder()

    val environment: LinuxEnvironment
        get() = (application as RoraxApp).environment

    inner class LocalBinder : Binder() {
        fun getService(): LinuxService = this@LinuxService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            environment.stop()
            stopSelf()
            return START_NOT_STICKY
        }
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onDestroy() {
        environment.stop()
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, LinuxService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val openIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Linux environment is running")
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Linux Environment",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Keeps the local Linux environment running" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
