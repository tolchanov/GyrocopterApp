package com.test.hyrocoptertestapp.utils.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.test.hyrocoptertestapp.utils.boundTo
import timber.log.Timber

class CompassView@JvmOverloads constructor(context: Context, attributeSet: AttributeSet, defStyle: Int = 0) : View(context, attributeSet, defStyle) {

    var degrees: Double = 0.0
    set(value) {
        field = if(value>360.0) value - 360.0 else if(value<0.0) value + 360.0 else value
        invalidate()
    }


    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, android.R.color.holo_blue_light)
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 3f
    }

    private val meashurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, android.R.color.holo_blue_dark)
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 3f
    }

    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 3f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10f, resources.displayMetrics)
    }

    private val pointers = mapOf(0 to "N", 90 to "E", 180 to "S", 270 to "W", 360 to "N")

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawLine(0f, measuredHeight/2f, measuredWidth.toFloat(), measuredHeight/2f, linePaint)

        val left = degrees.toInt() - 180
        val textY = measuredHeight/3f
        for((temp, ang) in (left..(left+360)).withIndex()){
            if(ang%10 == 0){
                canvas?.drawLine(calculatePosition(temp.toFloat()), measuredHeight-measuredHeight/1.5f, calculatePosition(temp.toFloat()), measuredHeight/1.5f, meashurePaint)
            }
            val bounded = ang.boundTo(360)
            if (bounded in pointers.keys) {
                    canvas?.drawText(pointers[bounded]?:"", calculatePosition(temp.toFloat()), textY, textPaint)
            }else{
                if(bounded%30 == 0){
                    canvas?.drawText("${bounded/10}", calculatePosition(temp.toFloat()), textY, textPaint)
                }
            }
        }
        canvas?.drawLine(measuredWidth/2f, 0f, measuredWidth/2f, measuredHeight.toFloat(), pointerPaint)
    }


    private fun calculatePosition(deg: Float) = (measuredWidth/360f)*deg
}