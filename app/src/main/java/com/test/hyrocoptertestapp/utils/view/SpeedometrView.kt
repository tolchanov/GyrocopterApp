package com.test.hyrocoptertestapp.utils.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.test.hyrocoptertestapp.utils.toRad
import kotlin.math.cos
import kotlin.math.sin
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth


/**
 * Currently unused
 * Beautiful view for speed indication
 */
class SpeedometrView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet, defStyle: Int = 0) : View(context, attributeSet, defStyle) {

    private val speedMin = 0.0
    private val speedMax = 20.0


    // background init
    private val bgPath = Path()
    private val bgRadius = 20f
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    // bg circle
    private val bgCircleColor = Color.parseColor("#262626")
    private val bgCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        color = bgCircleColor
    }


    // zones
    private val zonePath = Path()
    private val zoneGreen = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#36601c")
        style = Paint.Style.FILL
    }
    private val zoneYellow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#cbcf30")
        style = Paint.Style.FILL
    }
    private val zoneRed = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#b82301")
        style = Paint.Style.FILL
    }


    // measures
    private val bigLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 20f
        textAlign = Paint.Align.CENTER
    }

    private val bigTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 25f
        textAlign = Paint.Align.CENTER
    }

    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 7f
    }

    var speed: Double = 8.0
    set(value) {
        if (value < speedMin) field = speedMin
        else if (value > speedMax) field = speedMax
        else field = value
        invalidate()
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // draw bg
        bgPath.apply {
            moveTo(0f, bgRadius)
            arcTo(0f, 0f, bgRadius*2, bgRadius*2, -180f, 90f, false)
            lineTo(width - bgRadius, 0f)
            arcTo(width - bgRadius*2, 0f, width.toFloat(), bgRadius*2, -90f, 90f, false)
            lineTo(width.toFloat(), height-bgRadius)
            arcTo(width - 2*bgRadius, height-2*bgRadius, width.toFloat(), height.toFloat(), 0f, 90f, false)
            lineTo(bgRadius, height.toFloat())
            arcTo(0f, height-2*bgRadius, 2*bgRadius, height.toFloat(), 90f, 90f, false)
            lineTo(0f, bgRadius)
            close()
        }
        canvas?.drawPath(bgPath, bgPaint)
        bgPath.reset()

        // draw bg circle
        canvas?.drawCircle(measuredWidth/2f, measuredHeight/2f, measuredWidth/2f-20f, bgCirclePaint)

        // draw zones
        val radius = measuredWidth/2f-25f
        val cx = measuredWidth/2f
        val cy = measuredHeight/2f
        val zLen = 7f

        // green
        zonePath.apply {
            val edgeXs = cx+cos((-30f).toRad())*radius
            val edgeYs = cy+sin((-30f).toRad())*radius
            val edgeXe = cx+cos((110f).toRad())*(radius-zLen)
            val edgeYe = cy+sin((110f).toRad())*(radius-zLen)
            moveTo(edgeXs, edgeYs)
            arcTo(cx-radius, cy-radius, cx+radius, cy+radius, -30f, 140f, false)
            lineTo(edgeXe, edgeYe)
            arcTo(cx-radius+zLen, cy-radius+zLen, cx+radius-zLen, cy+radius-zLen, 110f, -140f, false)
            lineTo(edgeXs, edgeYs)
            close()
        }
        canvas?.drawPath(zonePath, zoneGreen)
        zonePath.reset()

        // yellow
        zonePath.apply {
            val edgeXs = cx+cos((110f).toRad())*radius
            val edgeYs = cy+sin((110f).toRad())*radius
            val edgeXe = cx+cos((190f).toRad())*(radius-zLen)
            val edgeYe = cy+sin((190f).toRad())*(radius-zLen)
            moveTo(edgeXs, edgeYs)
            arcTo(cx-radius, cy-radius, cx+radius, cy+radius, 110f, 80f, false)
            lineTo(edgeXe, edgeYe)
            arcTo(cx-radius+zLen, cy-radius+zLen, cx+radius-zLen, cy+radius-zLen, 190f, -80f, false)
            lineTo(edgeXs, edgeYs)
            close()
        }
        canvas?.drawPath(zonePath, zoneYellow)
        zonePath.reset()

        // red
        zonePath.apply {
            val edgeXs = cx+cos((190f).toRad())*radius
            val edgeYs = cy+sin((190f).toRad())*radius
            val edgeXe = cx+cos((230f).toRad())*(radius-zLen)
            val edgeYe = cy+sin((230f).toRad())*(radius-zLen)
            moveTo(edgeXs, edgeYs)
            arcTo(cx-radius, cy-radius, cx+radius, cy+radius, 190f, 40f, false)
            lineTo(edgeXe, edgeYe)
            arcTo(cx-radius+zLen, cy-radius+zLen, cx+radius-zLen, cy+radius-zLen, 230f, -40f, false)
            lineTo(edgeXs, edgeYs)
            close()
        }
        canvas?.drawPath(zonePath, zoneRed)
        zonePath.reset()

        // draw measure and text
        val angleDrop = -90f
        val textPadding = 40f
        for(ang in 0 .. 320 step 5){
            val angle = ang.toFloat() + angleDrop
            val edgeX = cx+cos(angle.toRad())*radius
            val edgeY = cy+sin(angle.toRad())*radius
            val len = if(ang%20 == 0) 15f else if (ang%10 == 0) 10f else 5f
            val dx = cx+cos(angle.toRad())*(radius-len)
            val dy = cy+sin(angle.toRad())*(radius-len)
            canvas?.drawLine(dx, dy, edgeX, edgeY, if(ang%20==0) bigLinePaint else linePaint)

            if(ang%40==0){
                val tx = cx+cos(angle.toRad())*(radius-textPadding)
                val ty = cy+sin(angle.toRad())*(radius-textPadding)
                canvas?.drawText((ang/16).toString(), tx, ty, textPaint)
            }
        }

        // draw text

        canvas?.drawText("AIR SPEED", cx, cy-40f, bigTextPaint)
        canvas?.drawText("KNOTS", cx, cy + 65f, bigTextPaint)

        // draw arrow
        canvas?.drawCircle(cx, cy, 10f, bigLinePaint)
        val arrowAngle = (speed*18f+angleDrop).toFloat()
        val arrowLen = radius - 30f
        canvas?.drawLine(cx, cy, cx+ cos(arrowAngle.toRad())*arrowLen, cy+sin(arrowAngle.toRad())*arrowLen, arrowPaint)

    }

}