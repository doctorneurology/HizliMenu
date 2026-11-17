package com.kadir.hizlimenu

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class KeyInterceptorService : AccessibilityService() {

    companion object {
        var isRecordingMode = false
        var onKeyRecorded: ((Int) -> Unit)? = null
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val scanCode = event.scanCode
            if (isRecordingMode && scanCode > 0) {
                val ignored = listOf(KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN,
                    KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT,
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_BACK)
                if (event.keyCode !in ignored) {
                    val prefs = getSharedPreferences("hizli_menu_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putInt("target_scan_code", scanCode).apply()
                    isRecordingMode = false
                    onKeyRecorded?.invoke(scanCode)
                    return true
                }
            }

            val prefs = getSharedPreferences("hizli_menu_prefs", Context.MODE_PRIVATE)
            val savedScanCode = prefs.getInt("target_scan_code", -1)
            if (savedScanCode != -1 && scanCode == savedScanCode) {
                val intent = Intent(this, LauncherOverlayActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(intent)
                return true
            }
        }
        return super.onKeyEvent(event)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}
