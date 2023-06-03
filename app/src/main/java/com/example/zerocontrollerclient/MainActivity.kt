package com.example.zerocontrollerclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import java.io.OutputStream
import java.net.Socket

class MainActivity : AppCompatActivity() {
    private lateinit var outputStream: OutputStream

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.start).setOnClickListener{
            startActivity(Intent(this, SetLayoutActivity::class.java))
        }

        findViewById<Button>(R.id.play).setOnClickListener{
            startActivity(Intent(this, ControllerPlayActivity::class.java))
        }

        findViewById<Button>(R.id.connectBtn).setOnClickListener{
           val thread = Thread {
                try {
                    val serverIp = "192.168.0.101"
                    val serverPort = 8888
                    val socket = Socket(serverIp, serverPort)
                    outputStream = socket.getOutputStream()
                } catch (e: Exception) {
                    Log.e("SocketClient", "Error: ${e.message}")
                }
            }
            thread.start()
            thread.join();

            SharedObject.setOutputStream(outputStream)
            startActivity(Intent(this, ControllerPlayActivity::class.java))
        }



    }
}