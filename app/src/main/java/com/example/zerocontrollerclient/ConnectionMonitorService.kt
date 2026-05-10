package com.example.zerocontrollerclient

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import java.net.DatagramPacket
import java.net.InetAddress
import java.util.Timer
import java.util.TimerTask

class ConnectionMonitorService : Service() {
    private val timer = Timer()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        timer.schedule(object : TimerTask() {
            override fun run() {
                try {
                    val udpSocket = SharedObject.getSocket()
                    if (udpSocket != null && SharedObject.serverIp.isNotEmpty()) {
                        val serverAddress = InetAddress.getByName(SharedObject.serverIp)
                        val buffer = byteArrayOf(0x04, SharedObject.clientSlot) // Packet Type: Heartbeat, Client Slot
                        val packet = DatagramPacket(buffer, buffer.size, serverAddress, SharedObject.serverPort)
                        udpSocket.send(packet)
                    } else {
                        cancelAndDisconnect()
                    }
                } catch (e: Exception) {
                    cancelAndDisconnect()
                }
            }
        }, 0, 500L)
    }
    
    private fun cancelAndDisconnect() {
        SharedObject.setSocket(null)
        val currentActivity = (application as MyApp).currentActivity
        currentActivity?.runOnUiThread {
            if (currentActivity is MainActivity)
                currentActivity.textView.text = ""
            Toast.makeText(currentActivity, "Controller Disconnected", Toast.LENGTH_SHORT).show()
        }
        timer.cancel()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}