package com.test.hyrocoptertestapp.utils

import android.graphics.*
import android.util.TypedValue
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.test.hyrocoptertestapp.model.InputModel

object MarkerGenerator {
    private val bmSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, DISPLAY_METRICS).toInt()

    private val bitmapManual = Bitmap.createBitmap(bmSize, bmSize, Bitmap.Config.ARGB_8888).also {
        val c = Canvas(it)
        c.drawCircle(bmSize/2f, bmSize/2f, bmSize/2f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GREEN
            style = Paint.Style.FILL
        })
    }

    private val bitmapAuto = Bitmap.createBitmap(bmSize, bmSize, Bitmap.Config.ARGB_8888).also {
        val c2 = Canvas(it)
        c2.drawCircle(bmSize/2f, bmSize/2f, bmSize/2f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.MAGENTA
            style = Paint.Style.FILL
        })
    }
    private val descriptorManual = BitmapDescriptorFactory.fromBitmap(bitmapManual)
    private val descriptorAuto = BitmapDescriptorFactory.fromBitmap(bitmapAuto)


    fun generateMarker(data: InputModel):MarkerOptions{
        return MarkerOptions()
                .flat(true)
                .icon(if(data.mode == INPUT_MODE_MANUAL) descriptorManual else descriptorAuto)
                .position(data.getLatLng())
                .anchor(0.5f, 0.5f)
    }

    private val paintBearing = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 3f
    }

    private val paintAutopilot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.YELLOW
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 3f
    }

    private val paintYaw = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 3f
    }


    private fun generateArrowBitmapDescriptor(data: InputModel): BitmapDescriptor{
        val bmDim = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, DISPLAY_METRICS).toInt()
        val bmR = bmDim/2f
        val bmT = bmR/5f
        val bm = Bitmap.createBitmap(bmDim, bmDim, Bitmap.Config.ARGB_8888).also {
            val c = Canvas(it)
            val path = getArrowPath(bmDim.toFloat(), bmR,bmDim.toFloat()-bmT,bmR-bmT/2f,bmR+bmT/2f)
            c.rotate(data.bearing.toFloat()-90f, bmR, bmR)
            c.drawLine(bmR, bmR, bmDim.toFloat(), bmR, paintBearing)
            c.drawPath(path, paintBearing)
            c.rotate(-(data.bearing.toFloat()-90f) + (data.autopilotDir.toFloat()-90f), bmR, bmR)
            c.drawLine(bmR, bmR, bmDim.toFloat(), bmR, paintAutopilot)
            c.drawPath(path, paintAutopilot)
            c.rotate(-(data.autopilotDir.toFloat()-90f)+(data.imuYaw.toFloat()-90f), bmR, bmR)
            c.drawLine(bmR, bmR, bmDim.toFloat(), bmR, paintYaw)
            c.drawPath(path, paintYaw)
            c.rotate(-(data.imuYaw.toFloat()-90f), bmR, bmR)
        }
        return BitmapDescriptorFactory.fromBitmap(bm)
    }

    private fun getArrowPath(xTop: Float, yTop: Float, xLeft: Float, yLeftT: Float, yLeftB: Float):Path{
        return Path().apply {
            moveTo(xTop, yTop)
            lineTo(xLeft, yLeftB)
            lineTo(xLeft, yLeftT)
            lineTo(xTop, yTop)
            close()
        }
    }

    fun generateArrowMarker(data: InputModel): MarkerOptions{
        return MarkerOptions()
                .flat(true)
                .icon(generateArrowBitmapDescriptor(data))
                .position(data.getLatLng())
                .anchor(0.5f, 0.5f)
    }
}