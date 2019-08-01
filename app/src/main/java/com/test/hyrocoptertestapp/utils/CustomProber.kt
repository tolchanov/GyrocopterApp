package com.test.hyrocoptertestapp.utils

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
import com.hoho.android.usbserial.driver.UsbSerialProber

object CustomProber {

    // e.g. Digispark CDC
    val customProber: UsbSerialProber
        get() {
            val customTable = ProbeTable()
            customTable.addProduct(0x16d0, 0x087e, CdcAcmSerialDriver::class.java)
            return UsbSerialProber(customTable)
        }

}
