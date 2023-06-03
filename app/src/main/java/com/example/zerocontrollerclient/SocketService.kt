package com.example.zerocontrollerclient

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.io.OutputStream
import java.net.Socket

class SocketService : Service() {

    private lateinit var socket: Socket
    private lateinit var outputStream: OutputStream

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Connect to the socket
        Thread {
            try {
                socket = Socket("192.168.0.101", 8888)
                outputStream = socket.getOutputStream()
            } catch (e: Exception) {
                Log.e("SocketService", "Error connecting to socket: ${e.message}")
            }
        }.start()

        return START_STICKY
    }

    fun sendMessage(message: String) {
        // Send a message to the socket
        Thread {
            try {
                outputStream.write(message.toByteArray())
                outputStream.flush()
            } catch (e: Exception) {
                Log.e("SocketService", "Error sending message: ${e.message}")
            }
        }.start()
    }

    override fun onDestroy() {
        // Close the socket connection when the service is destroyed
        try {
            socket.close()
        } catch (e: Exception) {
            Log.e("SocketService", "Error closing socket: ${e.message}")
        }
    }
}
