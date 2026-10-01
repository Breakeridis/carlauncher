package com.carlauncher.ui

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewConfiguration
import com.carlauncher.R
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * [3] Circular moving map portal.
 *
 * The placeholder map engine draws a procedural road network and a rotating
 * compass bezel; it tracks GPS heading and supports pan/pinch zoom. Replace
 * the canvas drawing with a real map SDK (Google Maps / Mapbox) while keeping
 * the same touch + GPS plumbing.
 */
class MapPortalView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    var onSearchClick: (() -> Unit)? = null

    private val density = resources.displayMetrics.density
    private val bezelWidth = 42f * density

    private var headingDegrees = 0f
    private var hasFix = false
    private var satellites = 0

    private var panX = 0f
    private var panY = 0f
    private var scale = 1f

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val buttonRadius = 26f * density

    private val scaleDetector =
        ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scale = (scale * detector.scaleFactor).coerceIn(1f, 4f)
                clampPan()
                invalidate()
                return true
            }
        })

    private var downX = 0f
    private var downY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var moved = false
    private var isPanning = false

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val roadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 15f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val roads: List<List<PointF>> = buildRoads()

    private val searchCenter = PointF()
    private val recenterCenter = PointF()

    private data class Portal(val cx: Float, val cy: Float, val radius: Float)

    private fun portal(): Portal {
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) / 2f - bezelWidth - 10f * density
        return Portal(cx, cy, radius)
    }

    fun updateGps(heading: Float, fix: Boolean, sats: Int) {
        headingDegrees = heading
        hasFix = fix
        satellites = sats
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val portal = portal()
        val cx = portal.cx
        val cy = portal.cy
        val r = portal.radius
        val night = isNightMode()

        val mapBg = resources.getColor(R.color.map_bg, null)
        val mapRoad = resources.getColor(R.color.map_road, null)
        val mapRoadMinor = resources.getColor(R.color.map_road_minor, null)
        val surface = resources.getColor(R.color.surface, null)
        val surfaceVariant = resources.getColor(R.color.surface_variant, null)
        val textSecondary = resources.getColor(R.color.text_secondary, null)
        val gpsOk = resources.getColor(R.color.gps_ok, null)
        val gpsBad = resources.getColor(R.color.gps_bad, null)
        val accent = resources.getColor(R.color.accent, null)

        // ---- map surface, clipped to the portal circle ----
        canvas.save()
        canvas.clipPath(Path().apply { addCircle(cx, cy, r, Path.Direction.CW) })
        fillPaint.color = mapBg
        canvas.drawCircle(cx, cy, r, fillPaint)

        canvas.save()
        canvas.translate(cx + panX, cy + panY)
        canvas.scale(scale, scale)
        canvas.translate(-cx, -cy)
        drawRoads(canvas, mapRoadMinor, mapRoad)
        canvas.restore()
        canvas.restore()

        // ---- compass bezel (rotates with heading) ----
        fillPaint.color = surface
        canvas.drawCircle(cx, cy, r + bezelWidth, fillPaint)
        canvas.save()
        canvas.rotate(-headingDegrees, cx, cy)
        val labels = arrayOf("N", "E", "S", "W")
        for (i in 0 until 24) {
            val angle = Math.toRadians((i * 15).toDouble())
            val isMajor = i % 6 == 0
            val innerR = r + 6f * density
            val outerR = r + if (isMajor) 22f * density else 14f * density
            val sinA = sin(angle).toFloat()
            val cosA = cos(angle).toFloat()
            val x1 = cx + sinA * innerR
            val y1 = cy - cosA * innerR
            val x2 = cx + sinA * outerR
            val y2 = cy - cosA * outerR
            fillPaint.color = if (isMajor) textSecondary else Color.argb(90, 255, 255, 255)
            if (night && !isMajor) fillPaint.color = Color.argb(50, 255, 255, 255)
            fillPaint.strokeWidth = if (isMajor) 2.5f * density else 1.2f * density
            canvas.drawLine(x1, y1, x2, y2, fillPaint)
            if (isMajor) {
                val lr = r + 35f * density
                textPaint.color = textSecondary
                canvas.drawText(labels[i / 6], cx + sinA * lr, cy - cosA * lr + 5f * density, textPaint)
            }
        }
        canvas.restore()

        // ---- vehicle marker (rotates with travel direction) ----
        canvas.save()
        canvas.translate(cx, cy)
        canvas.rotate(headingDegrees)
        drawVehicleMarker(canvas, accent)
        canvas.restore()

        // ---- GPS lock indicator & portal buttons ----
        drawGpsIndicator(canvas, cx, cy, r, gpsOk, gpsBad, textSecondary)
        drawPortalButtons(canvas, cx, cy, r, surfaceVariant, textSecondary)
    }

    private fun drawVehicleMarker(canvas: Canvas, accent: Int) {
        val size = 30f * density
        val path = Path().apply {
            moveTo(0f, -size)
            lineTo(-size * 0.8f, size * 0.7f)
            lineTo(0f, size * 0.35f)
            lineTo(size * 0.8f, size * 0.7f)
            close()
        }
        fillPaint.style = Paint.Style.FILL
        fillPaint.color = Color.WHITE
        canvas.drawPath(path, fillPaint)
        canvas.save()
        canvas.scale(0.82f, 0.82f)
        fillPaint.color = accent
        canvas.drawPath(path, fillPaint)
        canvas.restore()
    }

    private fun drawGpsIndicator(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        r: Float,
        gpsOk: Int,
        gpsBad: Int,
        textColor: Int,
    ) {
        val ix = cx + r * 0.62f
        val iy = cy - r * 0.62f
        fillPaint.color = if (hasFix) gpsOk else gpsBad
        canvas.drawCircle(ix, iy, 7f * density, fillPaint)
        textPaint.textSize = 13f * density
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = textColor
        val label = if (hasFix) "GPS LOCK \u2022 $satellites" else "NO GPS"
        canvas.drawText(label, ix + 12f * density, iy + 5f * density, textPaint)
        textPaint.textAlign = Paint.Align.CENTER
    }

    private fun drawPortalButtons(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        r: Float,
        bgColor: Int,
        iconColor: Int,
    ) {
        val offset = r * 0.66f
        searchCenter.set(cx - offset, cy - offset)
        recenterCenter.set(cx + offset, cy + offset)
        drawPortalButton(canvas, searchCenter.x, searchCenter.y, bgColor, iconColor, R.drawable.ic_search)
        drawPortalButton(canvas, recenterCenter.x, recenterCenter.y, bgColor, iconColor, R.drawable.ic_recenter)
    }

    private fun drawPortalButton(
        canvas: Canvas,
        x: Float,
        y: Float,
        bgColor: Int,
        iconColor: Int,
        iconRes: Int,
    ) {
        fillPaint.color = bgColor
        canvas.drawCircle(x, y, buttonRadius, fillPaint)
        val icon = resources.getDrawable(iconRes, null)
        val half = buttonRadius * 0.55f
        icon.setBounds(
            (x - half).toInt(),
            (y - half).toInt(),
            (x + half).toInt(),
            (y + half).toInt(),
        )
        icon.setTint(iconColor)
        icon.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                lastX = event.x
                lastY = event.y
                moved = false
                isPanning = true
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastX
                val dy = event.y - lastY
                if (abs(event.x - downX) > touchSlop || abs(event.y - downY) > touchSlop) moved = true
                if (moved && isPanning && !scaleDetector.isInProgress) {
                    panX += dx
                    panY += dy
                    clampPan()
                    invalidate()
                }
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_UP -> {
                isPanning = false
                if (!moved) {
                    handleTap(event.x, event.y)
                    performClick()
                }
                parent?.requestDisallowInterceptTouchEvent(false)
            }
            MotionEvent.ACTION_CANCEL -> {
                isPanning = false
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    private fun handleTap(x: Float, y: Float) {
        val dSearch = hypot(x - searchCenter.x, y - searchCenter.y)
        val dRecenter = hypot(x - recenterCenter.x, y - recenterCenter.y)
        when {
            dSearch <= buttonRadius * 1.25f -> onSearchClick?.invoke()
            dRecenter <= buttonRadius * 1.25f -> {
                panX = 0f
                panY = 0f
                scale = 1f
                invalidate()
            }
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun clampPan() {
        val maxPan = 1500f * density * scale
        panX = panX.coerceIn(-maxPan, maxPan)
        panY = panY.coerceIn(-maxPan, maxPan)
    }

    private fun buildRoads(): List<List<PointF>> {
        val rnd = Random(42)
        val roads = ArrayList<List<PointF>>()
        roads.add(listOf(PointF(-3200f, 0f), PointF(3200f, 0f)))
        roads.add(listOf(PointF(0f, -3200f), PointF(0f, 3200f)))
        repeat(26) {
            val points = ArrayList<PointF>()
            var x = rnd.nextFloat() * 6000f - 3000f
            var y = rnd.nextFloat() * 6000f - 3000f
            val step = 260f + rnd.nextFloat() * 420f
            repeat(3 + rnd.nextInt(5)) {
                points.add(PointF(x, y))
                x += step * (0.6f + rnd.nextFloat() * 0.8f) * if (rnd.nextBoolean()) 1f else -1f
                y += (rnd.nextFloat() - 0.5f) * 900f
            }
            roads.add(points)
        }
        return roads
    }

    private fun drawRoads(canvas: Canvas, minorColor: Int, majorColor: Int) {
        val portal = portal()
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = portal.cx
        val cy = portal.cy
        val minWorldX = cx - (cx + panX) / scale
        val maxWorldX = cx + (w - cx - panX) / scale
        val minWorldY = cy - (cy + panY) / scale
        val maxWorldY = cy + (h - cy - panY) / scale
        val margin = 100f

        roads.forEachIndexed { index, points ->
            val visible = points.any {
                it.x in (minWorldX - margin)..(maxWorldX + margin) &&
                    it.y in (minWorldY - margin)..(maxWorldY + margin)
            }
            if (!visible) return@forEachIndexed

            val isMajor = index < 2
            roadPaint.color = if (isMajor) majorColor else minorColor
            roadPaint.strokeWidth =
                ((if (isMajor) 16f else 7f) * density * scale).coerceAtMost(44f * density)

            val path = Path()
            points.forEachIndexed { i, p ->
                if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            canvas.drawPath(path, roadPaint)
        }
    }

    private fun isNightMode(): Boolean {
        val mode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }
}
