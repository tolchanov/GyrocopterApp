package com.test.hyrocoptertestapp.model

import android.hardware.usb.UsbDevice
import com.hoho.android.usbserial.driver.UsbSerialDriver

data class DeviceListItem(
        val device: UsbDevice,
        val port: Int,
        val driver: UsbSerialDriver?
)