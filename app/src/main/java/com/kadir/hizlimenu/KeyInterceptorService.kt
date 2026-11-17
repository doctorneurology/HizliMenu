package com.kadir.hizlimenu

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class KeyInterceptorService : AccessibilityService() {

    companion object {
        var isRecordingMode = false
        var onSignalDetected: ((String) -> Unit)? = null
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val scanCode = event.scanCode
            val keyCode = event.keyCode
            val blocked = listOf(
                KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN,
                KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT,
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_BACK,
                KeyEvent.KEYCODE_HOME, KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN
            )
            if (keyCode in blocked) return super.onKeyEvent(event)

            val prefs = getSharedPreferences("hizli_menu_prefs", Context.MODE_PRIVATE)
            if (isRecordingMode && (scanCode > 0 || keyCode > 0)) {
                prefs.edit()
                    .putString("trigger_type", "KEY")
                    .putInt("target_scan_code", scanCode)
                    .putInt("target_key_code", keyCode)
                    .apply()
                isRecordingMode = false
                onSignalDetected?.invoke("Tuş Yakalandı! (scanCode=$scanCode, keyCode=$keyCode)")
                return true
            }

            if (prefs.getString("trigger_type", "") == "KEY") {
                val savedScan = prefs.getInt("target_scan_code", -1)
                val savedKey = prefs.getInt("target_key_code", -1)
                if ((savedScan > 0 && scanCode == savedScan) || (savedKey > 0 && keyCode == savedKey)) {
                    openLauncher()
                    return true
                }
            }
        }
        return super.onKeyEvent(event)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (pkg == packageName || pkg.contains("launcher") || pkg.contains("systemui")) return

        val prefs = getSharedPreferences("hizli_menu_prefs", Context.MODE_PRIVATE)
        if (isRecordingMode) {
            prefs.edit()
                .putString("trigger_type", "PKG")
                .putString("target_pkg", pkg)
                .apply()
            isRecordingMode = false
            onSignalDetected?.invoke("Sistem Hedefi Yakalandı! ($pkg)")
            openLauncher()
            return
        }

        if (prefs.getString("trigger_type", "") == "PKG" && pkg == prefs.getString("target_pkg", "")) {
            openLauncher()
        }
    }

    private fun openLauncher() {
        val intent = Intent(this, LauncherOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}
}
