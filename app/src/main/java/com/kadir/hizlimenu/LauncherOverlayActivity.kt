package com.kadir.hizlimenu

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LauncherOverlayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("hizli_menu_prefs", Context.MODE_PRIVATE)
        val selectedApps = prefs.getStringSet("selected_apps", emptySet()) ?: emptySet()

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(40, 32, 40, 32)
            setBackgroundColor(Color.parseColor("#E61A1A1A"))
        }

        val header = TextView(this).apply {
            text = "Hızlı Erişim"
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }
        container.addView(header)

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val pm = packageManager
        if (selectedApps.isEmpty()) {
            val empty = TextView(this).apply {
                text = "Henüz uygulama seçilmedi. Hızlı Menü uygulamasından seçim yapın."
                setTextColor(Color.LTGRAY)
            }
            row.addView(empty)
        } else {
            selectedApps.forEachIndexed { index, pkg ->
                try {
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    val icon = pm.getApplicationIcon(appInfo)
                    val label = pm.getApplicationLabel(appInfo).toString()

                    val card = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        isFocusable = true
                        isFocusableInTouchMode = true
                        setPadding(24, 20, 24, 20)
                        val bg = GradientDrawable().apply {
                            cornerRadius = 20f
                            setColor(Color.parseColor("#2C2C2C"))
                        }
                        background = bg

                        setOnFocusChangeListener { v, hasFocus ->
                            bg.setColor(if (hasFocus) Color.parseColor("#1E88E5") else Color.parseColor("#2C2C2C"))
                            v.scaleX = if (hasFocus) 1.1f else 1.0f
                            v.scaleY = if (hasFocus) 1.1f else 1.0f
                        }

                        setOnClickListener {
                            val launch = pm.getLeanbackLaunchIntentForPackage(pkg)
                                ?: pm.getLaunchIntentForPackage(pkg)
                            if (launch != null) {
                                startActivity(launch)
                            }
                            finish()
                        }
                    }

                    val params = LinearLayout.LayoutParams(220, 200).apply {
                        setMargins(16, 8, 16, 8)
                    }
                    card.layoutParams = params

                    val img = ImageView(this).apply {
                        setImageDrawable(icon)
                        layoutParams = LinearLayout.LayoutParams(96, 96)
                    }
                    val txt = TextView(this).apply {
                        text = label
                        setTextColor(Color.WHITE)
                        textSize = 14f
                        gravity = Gravity.CENTER
                        maxLines = 1
                        setPadding(0, 12, 0, 0)
                    }

                    card.addView(img)
                    card.addView(txt)
                    row.addView(card)
                    if (index == 0) card.requestFocus()
                } catch (_: Exception) {}
            }
        }

        container.addView(row)
        setContentView(container)
    }
}
