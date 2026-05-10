package com.example.zerocontrollerclient

import java.net.DatagramSocket

class SharedObject {
        companion object {
            private var udpSocket: DatagramSocket? = null
            var serverIp: String = ""
            var serverPort: Int = 0
            var clientSlot: Byte = 0

            fun getSocket(): DatagramSocket? {
                return udpSocket
            }
            fun setSocket(newSocket: DatagramSocket?) {
                udpSocket = newSocket
            }
        }
}