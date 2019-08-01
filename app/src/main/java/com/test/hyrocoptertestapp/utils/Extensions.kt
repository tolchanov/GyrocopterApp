package com.test.hyrocoptertestapp.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import com.test.hyrocoptertestapp.model.InputModel
import java.text.ParseException
import java.util.*


var toast: Toast? = null

fun Context.toast(message: String?, length: Int = Toast.LENGTH_SHORT){
    toast?.cancel()
    toast = Toast.makeText(this, message, length)
    toast?.show()
}

fun Context.toast(@StringRes message: Int, length: Int = Toast.LENGTH_SHORT){
    toast?.cancel()
    toast = Toast.makeText(this, message, length)
    toast?.show()
}

fun String.getInputModel() : InputModel{
    try {
        val splitted = this.split(",")
        val gpsTime: Long = splitted[0].trim().toLong()
        val lat: Long = splitted[1].trim().toLong()
        val lon: Long = splitted[2].trim().toLong()
        val alt: Double = splitted[3].trim().toDouble()
        val hading: Double = splitted[4].trim().toDouble()
        val speed: Double = splitted[5].trim().toDouble()
        val range: Double = splitted[6].trim().toDouble()
        val bearing: Double = splitted[7].trim().toDouble()
        val autopilotDir: Double = splitted[8].trim().toDouble()
        val angleError: Double = splitted[9].trim().toDouble()
        val imuYaw: Double = splitted[10].trim().toDouble()
        val imuPitch: Double = splitted[11].trim().toDouble()
        val imuRoll: Double = splitted[12].trim().toDouble()
        val barAlt: Double = splitted[13].trim().toDouble()
        val steering: Int = splitted[14].trim().toInt()
        val mode: String = splitted[15].trim()
        return InputModel(gpsTime, lat, lon, alt, hading, speed, range, bearing, autopilotDir, angleError, imuYaw, imuPitch, imuRoll, barAlt, steering, mode)
    }catch (e: Exception){
        //return getInputHeader()
        throw ParseException("Parse exception!", -1)
    }
}

fun Int.boundTo(to: Int, from: Int = 0): Int{
    val range = to - from
    return if (this < from){
        this+range
    }else {
        this % range
    }
}

fun Long.toPosixDate(): String{
    return SIMPLE_DATE_FORMAT.format(Date(BASE_CALENDAR.time.time + this * 1000))
}

fun Float.toRad(): Float{
    return Math.toRadians(this.toDouble()).toFloat()
}