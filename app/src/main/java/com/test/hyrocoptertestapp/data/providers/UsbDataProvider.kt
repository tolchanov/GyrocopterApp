package com.test.hyrocoptertestapp.data.providers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.test.hyrocoptertestapp.model.InputModel
import com.test.hyrocoptertestapp.utils.*
import com.test.hyrocoptertestapp.utils.serial.SerialListener
import com.test.hyrocoptertestapp.utils.serial.SerialSocket
import timber.log.Timber

class UsbDataProvider(private val usbManager: UsbManager, private val context: Context) : InputDataProvider, SerialListener {
    override val inputData: MutableLiveData<InputModel> = MutableLiveData()
    override val preparing: MutableLiveData<ProcessStatus> = MutableLiveData()
    override val playback: MutableLiveData<PlaybackState> = MutableLiveData<PlaybackState>().apply { value = PlaybackState.PAUSED }

    private var socket: SerialSocket? = null
    private var connected = Connected.FALSE

    private var deviceId: Int? = null
    private var portNum: Int? = null
    private var baudRate: Int? = null

    private val dataList = mutableListOf<InputModel>()

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == INTENT_ACTION_GRANT_USB) {
                val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                connect(granted, deviceId, portNum, baudRate)
                context?.unregisterReceiver(this)
            }
        }
    }


    override fun prepare(prepareData: Bundle?) {
        Timber.d("Provider preparing")
        if (prepareData != null) {
            preparing.value = ProcessStatus(Status.LOADING)
            deviceId = prepareData.getInt(BUNDLE_DEVICE_ID)
            portNum = prepareData.getInt(BUNDLE_PORT)
            baudRate = prepareData.getInt(BUNDLE_BAUD_RATE)
            context.registerReceiver(broadcastReceiver, IntentFilter(INTENT_ACTION_GRANT_USB))
            connect(null, deviceId, portNum, baudRate)
            dataList.clear()
            Timber.d("Provider prepared")
        } else {
            throw IllegalArgumentException("prepare data must be not null!")
        }
    }

    override fun stop() {
        disconnect()
    }

    override fun getList(): MutableList<InputModel> {
        return dataList
    }

    override fun play(toNext: Boolean) {

    }

    override fun setTrackedPosition(pos: Int) {
    }

    private fun connect(permissionGranted: Boolean?, deviceId: Int?, portNum: Int?, baudRate: Int?) {
        Timber.d("Connect grant $permissionGranted")
        var device: UsbDevice? = null
        for (v in usbManager.deviceList.values) {
            if (v.deviceId == deviceId) device = v
        }
        if (device == null) {
            preparing.value = ProcessStatus(Status.ERROR, "Device not found!")
            return
        }
        var driver = UsbSerialProber.getDefaultProber().probeDevice(device)
        if (driver == null) {
            driver = CustomProber.customProber.probeDevice(device)
        }
        if (driver == null) {
            preparing.value = ProcessStatus(Status.ERROR, "No driver for device!")
            return
        }
        if (driver.ports.size < portNum ?: 0) {
            preparing.value = ProcessStatus(Status.ERROR, "Not enough ports at device!")
            return
        }
        val serialPort = driver.ports[portNum ?: 0]
        val usbConnection = usbManager.openDevice(driver.device)
        if (usbConnection == null && permissionGranted == null) {
            if (!usbManager.hasPermission(driver.device)) {
                val permissionIntent = PendingIntent.getBroadcast(context, 0, Intent(INTENT_ACTION_GRANT_USB), 0)
                usbManager.requestPermission(driver.device, permissionIntent)
                return
            }
        }
        if (usbConnection == null) {
            if (!usbManager.hasPermission(driver.device)) {
                preparing.value = ProcessStatus(Status.ERROR, "Permission denied!")
            } else {
                preparing.value = ProcessStatus(Status.ERROR, "Open failed!")
            }
            return
        }
        connected = Connected.PENDING
        try {
            socket = SerialSocket()
            if (baudRate != null) {
                socket?.connect(context, this, usbConnection, serialPort, baudRate)
            } else {
                preparing.value = ProcessStatus(Status.ERROR, "Baud rate is null!")
                throw Exception("Baud rate is null!")
            }
        } catch (e: Exception) {
            onSerialConnectError(e)
        }

    }

    private fun disconnect() {
        connected = Connected.FALSE
        socket?.disconnect()
        socket = null
    }


    // serial
    override fun onSerialConnect() {
        Timber.d("Provider connected")
        connected = Connected.TRUE
        preparing.value = ProcessStatus(Status.SUCCESS)
    }

    override fun onSerialConnectError(e: Exception?) {
        Timber.d(e)
        preparing.value = ProcessStatus(Status.ERROR, e?.message)
        disconnect()
    }

    private val stringBuilder: StringBuilder = StringBuilder()
    private val maxConcat = 3
    private var concatCount = 0

    override fun onSerialRead(data: ByteArray?) {
        Timber.d("Provider read: $data")
        data?.also {
            Timber.d(String(it))
            val s = String(it)
            if(concatCount == maxConcat) {
                concatCount = 0
                stringBuilder.clear()
            }
            try{
                val m = s.getInputModel()
                Timber.e("Data normal!")
                stringBuilder.clear()
                concatCount = 0
                acceptData(m)
            } catch (e: Exception){
                try{
                    Timber.e("Adding to sb!")
                    stringBuilder.append(s)
                    concatCount++
                    val m = stringBuilder.toString().getInputModel()
                    Timber.e("SB Data normal!")
                    stringBuilder.clear()
                    concatCount = 0
                    acceptData(m)

                } catch (e2: Exception){
                    Timber.e("SB data corrupted!")
                    if (stringBuilder.contains("Auto") || stringBuilder.contains("Manual")) {
                        Timber.e("SB Containt end. Clearing...")
                        stringBuilder.clear()
                        concatCount = 0
                    }
                }
            }

//            try {
//                val model = s.getInputModel()
//                Handler(Looper.getMainLooper()).post {
//                    dataList.add(model)
//                    inputData.value = model
//                }
//            } catch (e: Exception) {
//                Timber.e("Parsing failed. Skipping...")
//            }
        }
    }

    private fun acceptData(model: InputModel){
        Handler(Looper.getMainLooper()).post {
            dataList.add(model)
            inputData.value = model
        }
    }

    override fun onSerialIoError(e: Exception?) {
        Timber.d(e)
        disconnect()
    }
}