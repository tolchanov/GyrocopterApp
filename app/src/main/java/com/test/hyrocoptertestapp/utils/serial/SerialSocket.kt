package com.test.hyrocoptertestapp.utils.serial

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDeviceConnection
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.util.SerialInputOutputManager
import com.test.hyrocoptertestapp.utils.INTENT_ACTION_DISCONNECT
import java.io.IOException
import java.util.concurrent.Executors

class SerialSocket : SerialInputOutputManager.Listener{
    companion object{
        const val WRITE_WAIT_MILIS = 2000
    }

    private val disconnectBroadcastReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            listener?.onSerialIoError(IOException("Background disconnect"))
            disconnect()
        }
    }

    private var context: Context? = null
    private var listener: SerialListener? = null
    private var connection: UsbDeviceConnection? = null
    private var serialPort: UsbSerialPort? = null
    private var ioManager: SerialInputOutputManager? = null

    fun connect(context: Context, listener: SerialListener, connection: UsbDeviceConnection, serialPort: UsbSerialPort, baudRate: Int){
        if(this.serialPort != null){
            throw IOException("Already connected!")
        }
        this.context = context
        this.listener = listener
        this.connection = connection
        this.serialPort = serialPort
        context.registerReceiver(disconnectBroadcastReceiver, IntentFilter(INTENT_ACTION_DISCONNECT))
        serialPort.open(connection)
        serialPort.setParameters(baudRate, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        serialPort.dtr = true
        serialPort.rts = true
        ioManager = SerialInputOutputManager(serialPort, this)
        Executors.newSingleThreadExecutor().submit(ioManager)
        this.listener?.onSerialConnect()
    }


    fun disconnect(){
        listener = null
        ioManager?.also {
            it.listener = null
            it.stop()
        }
        ioManager = null
        serialPort?.also {
            try {
                it.dtr = false
                it.rts = false
                it.close()
            } catch (e: Exception){}
        }
        serialPort = null
        connection?.also { it.close() }
        connection = null
        try{
            context?.unregisterReceiver(disconnectBroadcastReceiver)
        }catch (e: Exception){}
    }

    fun write(data: ByteArray){
        if(serialPort == null) throw IOException("Not connected!")
        serialPort?.write(data, WRITE_WAIT_MILIS)
    }

    override fun onRunError(e: Exception?) {
        listener?.onSerialIoError(e)
    }

    override fun onNewData(data: ByteArray?) {
        listener?.onSerialRead(data)
    }
}