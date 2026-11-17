package com.kadir.hizlimenu

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("hizli_menu_prefs", Context.MODE_PRIVATE)
        val selectedApps = prefs.getStringSet("selected_apps", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 36, 48, 36)
            setBackgroundColor(Color.parseColor("#121212"))
        }

        val title = TextView(this).apply {
            text = "Hızlı Menü - Kumanda Tuş Ayarı"
            textSize = 22f
            setTextColor(Color.WHITE)
        }
        root.addView(title)

        val statusText = TextView(this).apply {
            val savedCode = prefs.getInt("target_scan_code", -1)
            text = if (savedCode == -1) "Durum: Henüz tuş tanımlanmadı." else "Tanımlı Tuş Kodu (scanCode): $savedCode"
            textSize = 15f
            setTextColor(Color.parseColor("#4CAF50"))
            setPadding(0, 12, 0, 24)
        }
        root.addView(statusText)

        val recordBtn = Button(this).apply {
            text = "1. Kumanda Tuşunu Tanımla (Bas ve Bekle)"
            isFocusable = true
            setOnClickListener {
                KeyInterceptorService.isRecordingMode = true
                statusText.text = "DİNLENİYOR... Şimdi kumandadaki Xiaomi TV+ tuşuna basın!"
                statusText.setTextColor(Color.YELLOW)
                KeyInterceptorService.onKeyRecorded = { code ->
                    runOnUiThread {
                        statusText.text = "BAŞARILI! Kaydedilen Tuş Kodu: $code"
                        statusText.setTextColor(Color.parseColor("#4CAF50"))
                    }
                }
            }
        }
        root.addView(recordBtn)

        val subTitle = TextView(this).apply {
            text = "2. Menüde Görünecek Uygulamaları Seçin:"
            textSize = 16f
            setTextColor(Color.LTGRAY)
            setPadding(0, 28, 0, 12)
        }
        root.addView(subTitle)

        val scrollView = ScrollView(this)
        val listLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val leanbackIntent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER) }

        val appMap = mutableMapOf<String, String>()
        (pm.queryIntentActivities(mainIntent, 0) + pm.queryIntentActivities(leanbackIntent, 0)).forEach {
            val pkg = it.activityInfo.packageName
            if (pkg != packageName) {
                appMap[pkg] = it.loadLabel(pm).toString()
            }
        }

        appMap.toList().sortedBy { it.second.lowercase() }.forEach { (pkg, label) ->
            val cb = CheckBox(this).apply {
                text = "$label  ($pkg)"
                setTextColor(Color.WHITE)
                textSize = 15f
                isChecked = selectedApps.contains(pkg)
                setPadding(16, 16, 16, 16)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedApps.add(pkg) else selectedApps.remove(pkg)
                    prefs.edit().putStringSet("selected_apps", selectedApps).apply()
                }
            }
            listLayout.addView(cb)
        }

        scrollView.addView(listLayout)
        root.addView(scrollView)
        setContentView(root)
    }
}
