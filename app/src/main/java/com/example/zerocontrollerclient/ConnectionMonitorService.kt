package com.example.zerocontrollerclient

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import java.io.OutputStream
import java.net.SocketException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Timer
import java.util.TimerTask

class ConnectionMonitorService : Service() {
    private var outputStream: OutputStream? = null
    private val timer = Timer()
    private val test = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN)
        .putLong(0).putInt(0).putShort(0).put(0).put(0x7f).array()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        outputStream = SharedObject.getOutputStream()
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        timer.schedule(object : TimerTask() {
            override fun run() {
                try {
                    outputStream?.write(test)?.also { outputStream!!.flush() }
                } catch (e: SocketException) {
                    SharedObject.setSocketAndOutputStream(null,null)
                    val currentActivity = (application as MyApp).currentActivity
                    currentActivity?.runOnUiThread {
                        if (currentActivity is MainActivity)
                            currentActivity.textView.text = ""
                        Toast.makeText(currentActivity, "Controller Disconnected", Toast.LENGTH_SHORT).show()
                    }
                    timer.cancel()
                    stopSelf()
                }
            }
        }, 0, 1000L)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}