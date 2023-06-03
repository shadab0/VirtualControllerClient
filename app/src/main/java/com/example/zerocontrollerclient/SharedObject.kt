package com.example.zerocontrollerclient

import java.io.OutputStream

class SharedObject {
        companion object {
            private var outputStream: OutputStream? = null
            fun getOutputStream(): OutputStream? {
                return outputStream
            }

            fun setOutputStream(stream: OutputStream){
                outputStream = stream
            }
        }
}