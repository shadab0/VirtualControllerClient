package com.example.zerocontrollerclient

import java.io.OutputStream
import java.net.DatagramSocket
import java.net.Socket

class SharedObject {
        companion object {
            private var outputStream: OutputStream? = null
            private var socket: Socket? = null
            fun getOutputStream(): OutputStream? {
                return outputStream
            }
            fun getSocket(): Socket? {
                return socket
            }
            fun setSocketAndOutputStream(newSocket: Socket?, newOutputStream: OutputStream?) {
                socket = newSocket
                outputStream = newOutputStream
            }
        }
}