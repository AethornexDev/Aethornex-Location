package com.aethornex.location

import android.app.Service
import android.content.Intent
import android.graphics.*
import android.os.IBinder
import android.view.*
import android.widget.FrameLayout
import kotlin.math.*

class RadarOverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var overlay: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (overlay != null) return START_STICKY
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val view = RadarView()
        overlay = view
        val type = if (android.os.Build.VERSION.SDK_INT >= 26)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            dp(230), dp(250), type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = dp(12); y = dp(120)
        }
        windowManager?.addView(view, params)
        return START_STICKY
    }

    override fun onDestroy() {
        overlay?.let { windowManager?.removeView(it) }
        overlay = null
        super.onDestroy()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private inner class RadarView : View(this@RadarOverlayService) {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG)
        private var downX = 0f
        private var downY = 0f
        private var startX = 0
        private var startY = 0
        private val targets = listOf(
            PointF(0.55f, -0.42f), PointF(-0.62f, -0.18f), PointF(0.18f, 0.72f)
        )

        override fun onDraw(c: Canvas) {
            super.onDraw(c)
            val w = width.toFloat()
            val h = height.toFloat()
            p.color = Color.argb(220, 7, 13, 24)
            p.style = Paint.Style.FILL
            c.drawRoundRect(0f, 0f, w, h, 22f, 22f, p)

            p.color = Color.rgb(57, 230, 165)
            p.textSize = 13f
            p.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            c.drawText("AETHORNEX  •  DEMO", 14f, 23f, p)

            val cx = w / 2f
            val cy = h * 0.57f
            val r = min(w * 0.39f, h * 0.34f)
            p.style = Paint.Style.STROKE
            p.strokeWidth = 1.5f
            p.color = Color.argb(190, 57, 230, 165)
            c.drawCircle(cx, cy, r, p)
            c.drawCircle(cx, cy, r * .66f, p)
            c.drawCircle(cx, cy, r * .33f, p)
            c.drawLine(cx-r, cy, cx+r, cy, p)
            c.drawLine(cx, cy-r, cx, cy+r, p)

            p.style = Paint.Style.STROKE
            p.strokeWidth = 2f
            p.color = Color.rgb(57, 230, 165)
            c.drawCircle(cx, cy, 5f, p)
            p.style = Paint.Style.FILL
            c.drawCircle(cx, cy, 3f, p)

            targets.forEachIndexed { index, pt ->
                val tx = cx + pt.x * r
                val ty = cy + pt.y * r
                p.color = Color.argb(180, 255, 70, 80)
                p.strokeWidth = 1.5f
                p.style = Paint.Style.STROKE
                c.drawLine(cx, cy, tx, ty, p)
                p.style = Paint.Style.FILL
                c.drawCircle(tx, ty, 5f, p)
                p.color = Color.rgb(255, 115, 120)
                p.textSize = 9f
                c.drawText("SIM ${index+1}", tx + 7f, ty - 5f, p)
            }
            p.color = Color.LTGRAY
            p.textSize = 10f
            c.drawText("DRAG TO MOVE  •  X TO CLOSE", 14f, h - 12f, p)
            p.color = Color.rgb(255, 90, 90)
            p.textSize = 18f
            c.drawText("×", w - 23f, 23f, p)
        }

        override fun onTouchEvent(e: android.view.MotionEvent): Boolean {
            val params = layoutParams as? WindowManager.LayoutParams ?: return true
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX; downY = e.rawY
                    startX = params.x; startY = params.y
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = startX - (e.rawX - downX).toInt()
                    params.y = startY + (e.rawY - downY).toInt()
                    windowManager?.updateViewLayout(this, params)
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    if (e.x > width - dp(42) && e.y < dp(42)) stopSelf()
                    return true
                }
            }
            return true
        }
    }
}
