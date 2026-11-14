package com.aethornex.location

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(8, 13, 22)
        window.navigationBarColor = Color.rgb(8, 13, 22)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(28), dp(24), dp(24))
            setBackgroundColor(Color.rgb(8, 13, 22))
        }
        val title = TextView(this).apply {
            text = "AETHORNEX\nLOCATION"
            textSize = 29f
            setTextColor(Color.rgb(230, 240, 250))
            gravity = Gravity.CENTER
            letterSpacing = 0.12f
        }
        val sub = TextView(this).apply {
            text = "EXTERNAL RADAR • VISUAL DEMO"
            textSize = 12f
            setTextColor(Color.rgb(57, 230, 165))
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(30))
        }
        val info = TextView(this).apply {
            text = "This prototype displays a floating radar with simulated targets. It does not read Free Fire MAX data, detect real enemies, reveal hidden players, or identify actual hitboxes."
            textSize = 14f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(24))
        }
        val start = Button(this).apply {
            text = "ENABLE FLOATING RADAR"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    val i = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName"))
                    startActivity(i)
                } else {
                    startService(Intent(this@MainActivity, RadarOverlayService::class.java))
                }
            }
        }
        val stop = Button(this).apply {
            text = "STOP RADAR"
            setOnClickListener {
                stopService(Intent(this@MainActivity, RadarOverlayService::class.java))
            }
        }
        root.addView(title)
        root.addView(sub)
        root.addView(info)
        root.addView(start, LinearLayout.LayoutParams(-1, dp(52)))
        root.addView(stop, LinearLayout.LayoutParams(-1, dp(52)))
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        // User returns from Android's overlay-permission screen and can tap Enable again.
    }
}
