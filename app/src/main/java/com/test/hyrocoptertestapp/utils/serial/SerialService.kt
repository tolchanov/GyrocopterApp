package com.test.hyrocoptertestapp.utils.serial

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import com.test.hyrocoptertestapp.R
import com.test.hyrocoptertestapp.utils.INTENT_ACTION_DISCONNECT
import com.test.hyrocoptertestapp.utils.INTENT_CLASS_MAIN_ACTIVITY
import com.test.hyrocoptertestapp.utils.NOTIFICATION_CHANNEL
import com.test.hyrocoptertestapp.utils.NOTIFY_MANAGER_START_FOREGROUND_SERVICE
import java.util.*

/**
 * Currently unused
 * For keep alive usb connection
 */

class SerialService : Service(), SerialListener{

    inner class SerialBinder : Binder() {
        fun getService() = this@SerialService
    }

    private enum class QueueType {CONNECT, CONNECT_ERROR, READ, IO_ERROR}

    private data class QueueItem(
            val type: QueueType,
            val data: ByteArray?,
            val e: Exception?
    )

    private val mainLooper: Handler = Handler(Looper.getMainLooper())
    private val binder: IBinder = SerialBinder()
    private val queue1 : Queue<QueueItem> = LinkedList()
    private val queue2: Queue<QueueItem> = LinkedList()

    private var listener: SerialListener? = null
    private var connected: Boolean = false
    private var notificationMsg: String? = null


    override fun onDestroy() {
        //cancelNotification()
        disconnect()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? { return binder }

    fun connect(listener: SerialListener, notificationMsg: String){
        this.listener = listener
        connected = true
        this.notificationMsg = notificationMsg
    }

    fun disconnect(){
        listener = null
        connected = false
        notificationMsg = null
    }

    fun attach(listener: SerialListener){
        if(Looper.getMainLooper().thread != Thread.currentThread()){
            throw IllegalArgumentException("Not in main thread!")
        }
        cancelNotification()
        if(connected){
            synchronized(this){
                this.listener = listener
            }
        }
        queue1.forEach {item ->
            when(item.type){
                QueueType.CONNECT -> listener.onSerialConnect()
                QueueType.CONNECT_ERROR -> listener.onSerialConnectError(item.e)
                QueueType.READ -> listener.onSerialRead(item.data)
                QueueType.IO_ERROR -> listener.onSerialIoError(item.e)
            }
        }
        queue2.forEach {item ->
            when(item.type){
                QueueType.CONNECT -> listener.onSerialConnect()
                QueueType.CONNECT_ERROR -> listener.onSerialConnectError(item.e)
                QueueType.READ -> listener.onSerialRead(item.data)
                QueueType.IO_ERROR -> listener.onSerialIoError(item.e)
            }
        }
        queue1.clear()
        queue2.clear()
    }

    fun detach(){
        if(connected){
            createNotification()
        }
        listener = null
    }

    fun createNotification(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val nc = NotificationChannel(NOTIFICATION_CHANNEL, "Background service", NotificationManager.IMPORTANCE_LOW)
            nc.setShowBadge(false)
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(nc)
        }
        val disconnectIntent = Intent().setAction(INTENT_ACTION_DISCONNECT)
        val restartIntent = Intent()
                .setClassName(this, INTENT_CLASS_MAIN_ACTIVITY)
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
        val disconnectPendingIntent = PendingIntent.getBroadcast(this, 1, disconnectIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val restartPendingIntent = PendingIntent.getActivity(this, 1, restartIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(resources.getColor(R.color.colorPrimary))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(notificationMsg)
                .setContentIntent(restartPendingIntent)
                .setOngoing(true)
                .addAction(NotificationCompat.Action(R.drawable.ic_clear_white_24dp, "Disconnect", disconnectPendingIntent))

        val notification = builder.build()
        startForeground(NOTIFY_MANAGER_START_FOREGROUND_SERVICE, notification)
    }

    fun cancelNotification() { stopForeground(true)}

    override fun onSerialConnect() {
        if(connected){
            synchronized(this){
                if(listener!=null){
                    mainLooper.post {
                        if(listener!=null){
                            listener?.onSerialConnect()
                        } else {
                            queue1.add(QueueItem(QueueType.CONNECT, null, null))
                        }
                    }
                } else {
                    queue2.add(QueueItem(QueueType.CONNECT, null, null))
                }
            }
        }
    }

    override fun onSerialConnectError(e: Exception?) {
        if (connected) {
            synchronized(this) {
                if (listener != null) {
                    mainLooper.post {
                        if (listener != null) {
                            listener?.onSerialConnectError(e)
                        } else {
                            queue1.add(QueueItem(QueueType.CONNECT_ERROR, null, e))
                            cancelNotification()
                            disconnect()
                        }
                    }
                } else {
                    queue2.add(QueueItem(QueueType.CONNECT_ERROR, null, e))
                    cancelNotification()
                    disconnect()
                }
            }
        }
    }

    override fun onSerialRead(data: ByteArray?) {
        if (connected) {
            synchronized(this) {
                if (listener != null) {
                    mainLooper.post {
                        if (listener != null) {
                            listener?.onSerialRead(data)
                        } else {
                            queue1.add(QueueItem(QueueType.READ, data, null))
                        }
                    }
                } else {
                    queue2.add(QueueItem(QueueType.READ, data, null))
                }
            }
        }
    }

    override fun onSerialIoError(e: Exception?) {
        if (connected) {
            synchronized(this) {
                if (listener != null) {
                    mainLooper.post {
                        if (listener != null) {
                            listener?.onSerialIoError(e)
                        } else {
                            queue1.add(QueueItem(QueueType.IO_ERROR, null, e))
                            cancelNotification()
                            disconnect()
                        }
                    }
                } else {
                    queue2.add(QueueItem(QueueType.IO_ERROR, null, e))
                    cancelNotification()
                    disconnect()
                }
            }
        }
    }
}
