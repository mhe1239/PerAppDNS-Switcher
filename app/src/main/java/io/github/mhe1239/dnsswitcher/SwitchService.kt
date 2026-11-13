package io.github.mhe1239.dnsswitcher

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent

const val KEY_MODE = "private_dns_mode"
const val KEY_SPECIFIER = "private_dns_specifier"

object Prefs {
    private fun sp(c: Context) = c.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    fun targets(c: Context): Set<String> = sp(c).getStringSet("targets", null) ?: emptySet()
    fun setTargets(c: Context, targets: Set<String>) = sp(c).edit().putStringSet("targets", targets).apply()
}

/** Switches Private DNS to automatic while a target app is in front, back to the hostname otherwise. */
class SwitchService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        val cls = event.className?.toString() ?: return
        // Keyboards, toasts and system popups fire this event too; only an activity means an app switch.
        if (!isActivity(pkg, cls)) return
        apply(pkg)
    }

    override fun onInterrupt() {}

    override fun onUnbind(intent: Intent?): Boolean {
        apply("") // service turned off: never leave blocking disabled
        return super.onUnbind(intent)
    }

    private fun isActivity(pkg: String, cls: String) = try {
        packageManager.getActivityInfo(ComponentName(pkg, cls), 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    private fun apply(pkg: String) {
        val cr = contentResolver
        val mode = desiredMode(pkg, Prefs.targets(this), Settings.Global.getString(cr, KEY_SPECIFIER)) ?: return
        if (Settings.Global.getString(cr, KEY_MODE) == mode) return
        try {
            Settings.Global.putString(cr, KEY_MODE, mode)
        } catch (e: SecurityException) {
            Log.w("DnsSwitcher", "WRITE_SECURE_SETTINGS not granted", e)
        }
    }
}
