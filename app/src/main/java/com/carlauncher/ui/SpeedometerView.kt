package com.carlauncher.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

/**
 * [4] Digital speedometer HUD overlaid on the map portal.
 * Tap the numbers to toggle KM/H <-> MPH; preference is persisted.
 */
class SpeedometerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    var useMph = false
    var onUnitToggle: (() -> Unit)? = null

    private val density = resources.displayMetrics.density

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(160, 0, 0, 0)
    }
    private val speedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        color = Color.WHITE
        setShadowLayer(6f * density, 0f, 2f * density, Color.argb(100, 0, 0, 0))
    }
    private val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.argb(200, 255, 255, 255)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private var speedKmh = 0f
    private var hasFix = false

    private var downX = 0f
    private var downY = 0f

    fun setSpeed(kmh: Float, fix: Boolean) {
        speedKmh = kmh
        hasFix = fix
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val displaySpeed = if (useMph) speedKmh * 0.6213712f else speedKmh
        val speedText =
            if (hasFix) String.format(Locale.US, "%.0f", displaySpeed) else "---"
        val unitText = if (useMph) "MPH" else "KM/H"

        val speedSize = 84f * density
        speedPaint.textSize = speedSize
        unitPaint.textSize = 18f * density

        val textWidth = speedPaint.measureText(speedText)
        val unitWidth = unitPaint.measureText(unitText)
        val totalWidth = max(textWidth, unitWidth) + 44f * density
        val totalHeight = speedSize + 30f * density

        val left = (width - totalWidth) / 2f
        val top = (height - totalHeight) / 2f
        canvas.drawRoundRect(
            RectF(left, top, left + totalWidth, top + totalHeight),
            20f * density,
            20f * density,
            bgPaint,
        )

        val baseline = top + speedSize * 0.82f
        canvas.drawText(speedText, width / 2f, baseline, speedPaint)
        canvas.drawText(unitText, width / 2f, baseline + 24f * density, unitPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
            }
            MotionEvent.ACTION_UP -> {
                val slop = ViewConfiguration.get(context).scaledTouchSlop
                if (abs(event.x - downX) <= slop && abs(event.y - downY) <= slop) {
                    onUnitToggle?.invoke()
                    performClick()
                }
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
