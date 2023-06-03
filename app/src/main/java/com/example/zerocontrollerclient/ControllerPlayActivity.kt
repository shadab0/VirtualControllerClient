package com.example.zerocontrollerclient

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Debug
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import java.net.Socket
import java.util.LinkedList
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


class ControllerPlayActivity : AppCompatActivity(), View.OnTouchListener {

    private lateinit var joystickBase: ImageView
    private lateinit var joystickStick: ImageView
    private lateinit var rightAnalogue: View
    private lateinit var playActivityMainContent: RelativeLayout

    private val socketMessage = ConcurrentLinkedQueue<String>()
    private var socketAnalogueR = ""
    private var socketAnalogueL = ""

    private var xPosition = 0
    private var yPosition = 0

    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controller_play)

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rightAnalogue = inflater.inflate(R.layout.alanogue, null)
        playActivityMainContent = findViewById(R.id.playActivityMainContent)
        playActivityMainContent.addView(rightAnalogue)

        joystickBase = rightAnalogue.findViewById(R.id.joystick_base)
        joystickStick = rightAnalogue.findViewById(R.id.joystick_stick)
        joystickStick.setOnTouchListener(this)

      /*  handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                val currentXPosition = xPosition
                val currentYPosition = yPosition
                Log.e("Pos", "$xPosition-$yPosition")
               // handler.postDelayed(this, 16) // 60 times per second
                handler.postDelayed(this, 1000)
            }
        }, 16)*/

       /* val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
        val interval: Long = 16
        val myRunnable = Runnable {
            val aVal = aButtonEvents.poll()
            val bVal = bButtonEvents.poll()
            if(aVal != null)
                Log.e("Abutton", aVal)
            if(bVal != null)
                Log.e("Bbutton", bVal)
        }
        val future = executor.scheduleAtFixedRate(myRunnable, 0, interval, TimeUnit.MILLISECONDS)*/

        val outputStream = SharedObject.getOutputStream()


        Thread {
            try {
                while (true) {
                    val aVal = socketMessage.poll()
                    if(aVal != null) {
                        //Log.e("Abutton", aVal)
                        outputStream?.write(aVal.toByteArray())?.also { outputStream.flush() }
                    }
                    outputStream?.write(socketAnalogueR.toByteArray())?.also { outputStream.flush() }
                    Thread.sleep(3)
                }
              //  socket.close()
            } catch (e: Exception) {
                Log.e("SocketClient", "Error: ${e.message}")
            }
        }.start()

    /*    Thread {
            try {
                while (true){
                    outputStream?.write(socketAnalogueR.toByteArray())?.also { outputStream.flush() }
                    Thread.sleep(5)
                }
                //  socket.close()
            } catch (e: Exception) {
                Log.e("SocketClient", "Error: ${e.message}")
            }
        }.start()*/


        val buttonList = loadButtonList(this)

        for (buttonInfo in buttonList){
            if (buttonInfo.second.first.first.contains("singleButton")) {
                val button = ImageButton(this)
                val bytes = Base64.decode(buttonInfo.first.first, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                val drawable = BitmapDrawable(resources, bitmap)
                button.setImageDrawable(drawable)
                button.id = buttonInfo.first.second
                button.tag = buttonInfo.second.first.first
                button.scaleX = buttonInfo.second.first.second.first
                button.scaleY = buttonInfo.second.first.second.second
                button.background = null
                button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                val layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.leftMargin = buttonInfo.second.second.first
                layoutParams.topMargin = buttonInfo.second.second.second
                playActivityMainContent.addView( button, layoutParams)
                button.setOnTouchListener(this)
            }
           /* button.setOnLongClickListener { dragButton(button) }
            button.setOnClickListener { addButtonFilter(button) }*/
        }

    }

    private fun loadButtonList(context: Context): MutableList<Pair<Pair<String, Int>, Pair<Pair<String, Pair<Float, Float>>, Pair<Int, Int>>>> {
        val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val buttonCount = prefs.getInt("button_count", 0)
        val buttonList = mutableListOf<Pair<Pair<String, Int>, Pair<Pair<String, Pair<Float, Float>>, Pair<Int, Int>>>>()
        for (i in 0 until buttonCount) {
            val buttonSrc = prefs.getString("button_${i}_src", null) ?: continue
            val buttonId = prefs.getInt("button_${i}_id", 0)
            val buttonTag = prefs.getString("button_${i}_tag", "#FFFFFF") ?: "#FFFFFF"
            val buttonScaleX = prefs.getFloat("button_${i}_scalex", 0f)
            val buttonScaleY = prefs.getFloat("button_${i}_scaley", 0f)
            val buttonX = prefs.getInt("button_${i}_x", 0)
            val buttonY = prefs.getInt("button_${i}_y", 0)
            buttonList.add(Pair(Pair(buttonSrc, buttonId), Pair(Pair(buttonTag, Pair(buttonScaleX, buttonScaleY)), Pair(buttonX, buttonY ))))
        }
        return buttonList
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        var tag = ""
        if(v.tag != null)
            tag = v.tag as String
        if (v == joystickStick) {
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    val rawX = event.rawX
                    val rawY = event.rawY

                    // Calculate the distance from the center of the joystick base to the touch event point
                    val distance = sqrt(
                        (rawX - joystickBase.x - joystickBase.width / 2).pow(2) + (rawY - joystickBase.y - joystickBase.height / 2).pow(
                            2
                        )
                    )

                    // Clamp the distance to the radius of the joystick base
                    val maxDistance = joystickBase.width / 2.toDouble()
                    var clampedX = rawX
                    var clampedY = rawY
                    if (distance > maxDistance) {
                        val angle = atan2(
                            rawY - joystickBase.y - joystickBase.height / 2,
                            rawX - joystickBase.x - joystickBase.width / 2
                        )
                        clampedX =
                            (joystickBase.x + joystickBase.width / 2 + maxDistance * cos(angle)).toFloat()
                        clampedY =
                            (joystickBase.y + joystickBase.height / 2 + maxDistance * sin(angle)).toFloat()
                    }

                    // Calculate the x and y positions based on the clamped touch event coordinates
                    val scale = min(joystickBase.width, joystickBase.height) / 2.0
                    xPosition =
                        ((clampedX - (joystickBase.x + joystickBase.width / 2)) * 32767 / scale).toInt()
                    yPosition =
                        ((clampedY - (joystickBase.y + joystickBase.height / 2)) * 32767 / scale).toInt()

                    // Clamp x and y positions to range -32767 to 32767
                    xPosition = xPosition.coerceIn(-32767, 32767)
                    yPosition = yPosition.coerceIn(-32767, 32767)

                    socketAnalogueR = "R$xPosition|$yPosition,";
                    // Update joystick stick position
                    joystickStick.x = clampedX - joystickStick.width / 2
                    joystickStick.y = clampedY - joystickStick.height / 2

                    // Use xPosition and yPosition as desired
                }

                MotionEvent.ACTION_UP -> {
                    // Reset x and y positions
                    xPosition = 0
                    yPosition = 0

                    socketAnalogueR = "R$xPosition|$yPosition,";
                    // Reset joystick stick position
                    joystickStick.x =
                        joystickBase.x + joystickBase.width / 2 - joystickStick.width / 2
                    joystickStick.y =
                        joystickBase.y + joystickBase.height / 2 - joystickStick.height / 2
                }
            }
        }
        if (tag.contains("singleButton")) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if(tag.contains("key_a"))
                        socketMessage.add("1,")
                    if(tag.contains("key_b"))
                        socketMessage.add("2,")
                }

                MotionEvent.ACTION_UP -> {
                    if(tag.contains("key_a"))
                        socketMessage.add("-1,")
                    if(tag.contains("key_b"))
                        socketMessage.add("-2,")
                }
                MotionEvent.ACTION_CANCEL -> {
                    if(tag.contains("key_a"))
                        socketMessage.add("-1,")
                    if(tag.contains("key_b"))
                        socketMessage.add("-2,")
                }
            }

        }
        return true
    }
}