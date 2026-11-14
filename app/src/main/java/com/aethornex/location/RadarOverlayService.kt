package com.aethornex.location

import android.app.Service
import android.content.Intent
import android.graphics.*
import android.os.IBinder
import android.view.*
import kotlin.math.abs

class RadarOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlay: VisionPanel? = null
    private var params: WindowManager.LayoutParams? = null

    private var minimized = false
    private var aiEnabled = false
    private var boxesEnabled = true
    private var linesEnabled = true

    override fun onBind(intent: Intent?): IBinder? = null

    private fun dp(v: Int) =
        (v * resources.displayMetrics.density).toInt()

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        if (overlay != null) return START_STICKY

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val type = if (android.os.Build.VERSION.SDK_INT >= 26)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        params = WindowManager.LayoutParams(
            dp(300),
            dp(310),
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = dp(12)
            y = dp(120)
        }

        overlay = VisionPanel()
        windowManager?.addView(overlay, params)

        return START_STICKY
    }

    private fun resizePanel() {
        val p = params ?: return
        val v = overlay ?: return

        p.width = dp(if (minimized) 64 else 300)
        p.height = dp(if (minimized) 64 else 310)

        windowManager?.updateViewLayout(v, p)
        v.invalidate()
    }

    override fun onDestroy() {
        overlay?.let {
            try {
                windowManager?.removeView(it)
            } catch (_: Exception) {}
        }
        overlay = null
        params = null
        super.onDestroy()
    }

    private inner class VisionPanel :
        View(this@RadarOverlayService) {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        private var downX = 0f
        private var downY = 0f
        private var startX = 0
        private var startY = 0
        private var moved = false

        private val cyan = Color.rgb(0, 225, 255)
        private val green = Color.rgb(45, 245, 125)
        private val background = Color.rgb(5, 17, 29)

        private fun text(
            c: Canvas,
            value: String,
            x: Float,
            y: Float,
            size: Float,
            color: Int,
            bold: Boolean = false
        ) {
            paint.color = color
            paint.textSize = size * resources.displayMetrics.density
            paint.typeface = Typeface.create(
                "sans-serif",
                if (bold) Typeface.BOLD else Typeface.NORMAL
            )
            paint.style = Paint.Style.FILL
            c.drawText(value, x, y, paint)
        }

        private fun panel(
            c: Canvas,
            l: Float,
            t: Float,
            r: Float,
            b: Float,
            color: Int,
            radius: Float = 12f
        ) {
            paint.color = color
            paint.style = Paint.Style.FILL
            c.drawRoundRect(
                l, t, r, b,
                dp(radius.toInt()).toFloat(),
                dp(radius.toInt()).toFloat(),
                paint
            )
        }

        private fun toggle(
            c: Canvas,
            x: Float,
            y: Float,
            enabled: Boolean
        ) {
            val w = dp(43).toFloat()
            val h = dp(23).toFloat()

            panel(
                c, x, y, x + w, y + h,
                if (enabled) green else Color.DKGRAY,
                15f
            )

            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL

            val cx = if (enabled) x + w - dp(12) else x + dp(12)
            c.drawCircle(
                cx,
                y + h / 2f,
                dp(8).toFloat(),
                paint
            )
        }

        override fun onDraw(c: Canvas) {
            super.onDraw(c)

            if (minimized) {
                paint.color = Color.rgb(4, 25, 40)
                paint.style = Paint.Style.FILL
                c.drawCircle(
                    width / 2f,
                    height / 2f,
                    dp(29).toFloat(),
                    paint
                )

                paint.color = cyan
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = dp(2).toFloat()
                c.drawCircle(
                    width / 2f,
                    height / 2f,
                    dp(29).toFloat(),
                    paint
                )

                text(
                    c, "A",
                    dp(18).toFloat(),
                    dp(43).toFloat(),
                    28f, cyan, true
                )
                return
            }

            val w = width.toFloat()

            panel(c, 0f, 0f, w, height.toFloat(), background, 18f)

            paint.color = cyan
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(1).toFloat()
            c.drawRoundRect(
                1f, 1f,
                w - 1f, height - 1f,
                dp(18).toFloat(),
                dp(18).toFloat(),
                paint
            )

            text(c, "A", dp(14).toFloat(), dp(34).toFloat(), 25f, cyan, true)
            text(c, "AETHORNEX", dp(48).toFloat(), dp(26).toFloat(), 16f, Color.WHITE, true)
            text(c, "VISION", dp(48).toFloat(), dp(43).toFloat(), 10f, cyan, true)

            text(c, "—", w - dp(62).toFloat(), dp(27).toFloat(), 21f, Color.WHITE, true)
            text(c, "×", w - dp(29).toFloat(), dp(28).toFloat(), 20f, Color.RED, true)

            panel(
                c, dp(12).toFloat(), dp(57).toFloat(),
                w - dp(12), dp(91).toFloat(),
                Color.rgb(9, 38, 48), 8f
            )

            text(c, "●", dp(21).toFloat(), dp(79).toFloat(), 11f,
                if (aiEnabled) green else Color.GRAY)

            text(
                c,
                if (aiEnabled) "AI CONTROL: ON" else "AI CONTROL: OFF",
                dp(38).toFloat(), dp(80).toFloat(),
                12f, Color.WHITE, true
            )

            text(c, "MODEL: NOT CONNECTED", dp(18).toFloat(),
                dp(107).toFloat(), 10f, Color.YELLOW, true)

            text(c, "VISIBLE PLAYER DETECTION", dp(15).toFloat(),
                dp(140).toFloat(), 11f, Color.WHITE, true)
            toggle(c, w - dp(59), dp(122).toFloat(), aiEnabled)

            text(c, "DETECTION BOXES", dp(15).toFloat(),
                dp(185).toFloat(), 12f, Color.WHITE)
            toggle(c, w - dp(59), dp(167).toFloat(), boxesEnabled)

            text(c, "ANTENNA LINES", dp(15).toFloat(),
                dp(230).toFloat(), 12f, Color.WHITE)
            toggle(c, w - dp(59), dp(212).toFloat(), linesEnabled)

            panel(
                c, dp(12).toFloat(), dp(249).toFloat(),
                w - dp(12), dp(278).toFloat(),
                Color.rgb(13, 34, 48), 7f
            )

            text(c, "LIVE CAPTURE SERVICE", dp(19).toFloat(),
                dp(268).toFloat(), 10f, cyan, true)

            text(c, "DRAG HEADER  •  TAP — TO MINIMIZE",
                dp(14).toFloat(), dp(297).toFloat(),
                8f, Color.LTGRAY)
        }

        override fun onTouchEvent(e: MotionEvent): Boolean {
            val p = params ?: return true

            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX
                    downY = e.rawY
                    startX = p.x
                    startY = p.y
                    moved = false
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = e.rawX - downX
                    val dy = e.rawY - downY

                    if (minimized || downY <= dp(55)) {
                        if (abs(dx) > dp(4) || abs(dy) > dp(4))
                            moved = true

                        if (moved) {
                            p.x = startX - dx.toInt()
                            p.y = startY + dy.toInt()
                            windowManager?.updateViewLayout(this, p)
                        }
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (moved) return true

                    if (minimized) {
                        minimized = false
                        resizePanel()
                        return true
                    }

                    val x = e.x
                    val y = e.y

                    if (y < dp(48) && x > width - dp(78)) {
                        if (x > width - dp(43)) {
                            stopSelf()
                        } else {
                            minimized = true
                            resizePanel()
                        }
                    } else if (y in dp(115).toFloat()..dp(155).toFloat()) {
                        aiEnabled = !aiEnabled
                    } else if (y in dp(160).toFloat()..dp(200).toFloat()) {
                        boxesEnabled = !boxesEnabled
                    } else if (y in dp(205).toFloat()..dp(245).toFloat()) {
                        linesEnabled = !linesEnabled
                    }

                    invalidate()
                    return true
                }
            }

            return true
        }
    }
}
