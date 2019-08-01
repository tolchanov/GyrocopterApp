package com.test.hyrocoptertestapp.data

import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.test.hyrocoptertestapp.model.DeviceListItem
import com.test.hyrocoptertestapp.utils.CustomProber
import com.test.hyrocoptertestapp.utils.SingleLiveEvent

interface IUsbDataManager{
    val deviceListData: SingleLiveEvent<MutableList<DeviceListItem>>

    fun refreshDeviceList()
//    fun write(data: String)
}

class UsbDataManager(private val usbManager: UsbManager) : IUsbDataManager{
    private val deviceList = mutableListOf<DeviceListItem>()

    override val deviceListData: SingleLiveEvent<MutableList<DeviceListItem>> = SingleLiveEvent()

    override fun refreshDeviceList(){
        val usbDefaultProber = UsbSerialProber.getDefaultProber()
        val usbCustomProber = CustomProber.customProber
        deviceList.clear()
        for(device in usbManager.deviceList.values){
            var driver = usbDefaultProber.probeDevice(device)
            if(driver == null) {
                driver = usbCustomProber.probeDevice(device)
            }
            if(driver != null){
                for(port in 0 until driver.ports.size){
                    deviceList.add(DeviceListItem(device, port, driver))
                }
            } else {
                deviceList.add(DeviceListItem(device, 0, null))
            }
        }
        deviceListData.callWithValue(deviceList)
    }
}