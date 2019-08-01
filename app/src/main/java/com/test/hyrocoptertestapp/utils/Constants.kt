package com.test.hyrocoptertestapp.utils

import android.util.DisplayMetrics
import com.test.hyrocoptertestapp.BuildConfig
import java.text.SimpleDateFormat
import java.util.*

const val INTENT_ACTION_DISCONNECT = BuildConfig.APPLICATION_ID + ".Disconnect"
const val NOTIFICATION_CHANNEL = BuildConfig.APPLICATION_ID + ".Channel"
const val INTENT_CLASS_MAIN_ACTIVITY = BuildConfig.APPLICATION_ID + ".MainActivity"

// values have to be unique within each app
const val NOTIFY_MANAGER_START_FOREGROUND_SERVICE = 1001

const val INTENT_ACTION_GRANT_USB = "${BuildConfig.APPLICATION_ID}.GRANT_USB"


enum class Connected{FALSE, PENDING, TRUE}

// di constants
const val DI_USB_DATA_PROVIDER = "usb"
const val DI_LOG_DATA_PROVIDER = "log"

// bundle constants
const val BUNDLE_IS_USB = "isusb"
const val BUNDLE_DEVICE_ID = "device"
const val BUNDLE_PORT = "port"
const val BUNDLE_BAUD_RATE = "baud"
const val BUNDLE_FILENAME = "filename"

val DISPLAY_METRICS: DisplayMetrics = DisplayMetrics()

val BASE_CALENDAR: Calendar = Calendar.getInstance().apply {
    set(Calendar.YEAR, 2000)
    set(Calendar.MONTH, Calendar.JANUARY)
    set(Calendar.DAY_OF_MONTH, 1)
    set(Calendar.HOUR, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}
val SIMPLE_DATE_FORMAT = SimpleDateFormat("dd-MMM hh:mm:ss", Locale.US)

const val INPUT_MODE_MANUAL = "Manual"
const val INPUT_MODE_AUTO = "Auto"