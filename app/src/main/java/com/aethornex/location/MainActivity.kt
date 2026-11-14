package com.aethornex.location

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private val captureRequest = 2001
    private lateinit var status: TextView

    private fun dp(v: Int) =
        (v * resources.displayMetrics.density).toInt()

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
            text = "AETHORNEX\nLIVE SYSTEM"
            textSize = 29f
            setTextColor(Color.rgb(230, 240, 250))
            gravity = Gravity.CENTER
        }

        val sub = TextView(this).apply {
            text = "VISUAL ANALYSIS PROTOTYPE"
            textSize = 12f
            setTextColor(Color.rgb(57, 230, 165))
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(24))
        }

        status = TextView(this).apply {
            text = "STATUS: READY\nWaiting for your permission"
            textSize = 14f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(24))
        }

        val capture = Button(this).apply {
            text = "START LIVE CAPTURE"
            setOnClickListener {
                requestScreenCapture()
            }
        }

        val radar = Button(this).apply {
            text = "ENABLE FLOATING RADAR"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    val i = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(i)
                } else {
                    startService(
                        Intent(
                            this@MainActivity,
                            RadarOverlayService::class.java
                        )
                    )
                }
            }
        }

        val stop = Button(this).apply {
            text = "STOP ALL"
            setOnClickListener {
                stopService(
                    Intent(this@MainActivity, ScreenCaptureService::class.java)
                )
                stopService(
                    Intent(this@MainActivity, RadarOverlayService::class.java)
                )
                status.text = "STATUS: STOPPED"
            }
        }

        root.addView(title)
        root.addView(sub)
        root.addView(status)
        root.addView(capture, LinearLayout.LayoutParams(-1, dp(54)))
        root.addView(radar, LinearLayout.LayoutParams(-1, dp(54)))
        root.addView(stop, LinearLayout.LayoutParams(-1, dp(54)))

        setContentView(root)
    }

    private fun requestScreenCapture() {
        val manager = getSystemService(
            Context.MEDIA_PROJECTION_SERVICE
        ) as MediaProjectionManager

        startActivityForResult(
            manager.createScreenCaptureIntent(),
            captureRequest
        )
    }

    @Deprecated("Using activity result callback for compatibility")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == captureRequest) {
            if (resultCode == RESULT_OK && data != null) {

                val serviceIntent = Intent(
                    this,
                    ScreenCaptureService::class.java
                ).apply {
                    putExtra(
                        ScreenCaptureService.EXTRA_RESULT_CODE,
                        resultCode
                    )
                    putExtra(
                        ScreenCaptureService.EXTRA_RESULT_DATA,
                        data
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }

                status.text = "STATUS: CAPTURE REQUEST ACCEPTED\nStarting live capture..."

            } else {
                status.text = "STATUS: CAPTURE CANCELLED\nPermission was not granted."
            }
        }
    }
}
