package com.roraxos.linuxenv.core

import android.content.Context

/**
 * Small wrapper around SharedPreferences for the handful of settings
 * this app needs: whether onboarding/install has completed, which
 * engine was chosen, and the rootfs URL to install from.
 *
 * No rootfs URL ships with this app — set your own in Settings before
 * running the installer.
 */
class AppPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("rorax_prefs", Context.MODE_PRIVATE)

    var onboardingComplete: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, value).apply()

    var rootfsUrl: String?
        get() = prefs.getString(KEY_ROOTFS_URL, null)
        set(value) = prefs.edit().putString(KEY_ROOTFS_URL, value).apply()

    var engine: String
        get() = prefs.getString(KEY_ENGINE, "PROOT_ROOTLESS") ?: "PROOT_ROOTLESS"
        set(value) = prefs.edit().putString(KEY_ENGINE, value).apply()

    companion object {
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        private const val KEY_ROOTFS_URL = "rootfs_url"
        private const val KEY_ENGINE = "engine"
    }
}
