package com.example.zerocontrollerclient

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.net.SocketException
import java.nio.ByteBuffer
import java.nio.ByteOrder


@Suppress("DEPRECATION")
class ControllerPlayActivity : AppCompatActivity(), View.OnTouchListener {

    private lateinit var playActivityMainContent: RelativeLayout
    private lateinit var vibrator: Vibrator
    private var vibrate = true
    private var macro_data = StringBuilder()

    private var profile = ""
    private var macro_name = ""
    private var isMacro = false
    private var macroClicked = false
    private var isRecording = false
    private var lastClickTime = 0L
//    val values1 = arrayOf(0, 120, 180, 240, 360, 480)
//    var currentIndex = 0

    private var macroCount = 0
    private var threadExited = true
    private val outputStream = SharedObject.getOutputStream()
    private val coroutinePoolSize = 1 // Number of worker coroutines
    private val dataChannel = Channel<ByteArray>(Channel.UNLIMITED)

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controller_play)
        profile = intent.getStringExtra("profile").toString()
        macro_name = intent.getStringExtra("macro").toString()
        isMacro = intent.getBooleanExtra("isMacro", false)
        val sharedPrefs = getSharedPreferences("selected_macros", Context.MODE_PRIVATE)
        playActivityMainContent = findViewById(R.id.playActivityMainContent)
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager: VibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibrate = sharedPrefs.getBoolean("vibrate", true)
        initCoroutines()

        val record_layout = findViewById<LinearLayout>(R.id.record_layout)
        if (isMacro)
            record_layout.visibility = View.VISIBLE

        findViewById<ImageButton>(R.id.start_record).setOnClickListener {
            isRecording = true
            macro_data.clear()
            Toast.makeText(this, "Recording...", Toast.LENGTH_SHORT).show()
            lastClickTime = System.currentTimeMillis()
        }

        findViewById<ImageButton>(R.id.stop_record).setOnClickListener {
            var L = macro_data.indexOf(",L ") < macro_data.indexOf(",R ")
            var index = 0
            while (true) {
                val indexToReplace = if (L) {
                    macro_data.indexOf(",L ", index)
                } else {
                    macro_data.indexOf(",R ", index)
                }

                if (indexToReplace != -1) {
                    macro_data.replace(indexToReplace, indexToReplace + 2, if (L) "|L" else "|R")
                    L = !L
                    index = indexToReplace + 3
                } else {
                    break
                }
            }
            val intent = Intent(this, CreateMacroActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("macro", macro_name)
            intent.putExtra("recorded_macro", macro_data.toString().replace(",|", "|"))
            startActivity(intent)
            finish()
        }

        val live = findViewById<ImageButton>(R.id.live)
        live.setOnClickListener {
            isMacro = !isMacro
            if (!isMacro)
                live.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
            else
                live.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)
        }

        val rec = findViewById<CardView>(R.id.record_buttons)
        val customDrawable = ContextCompat.getDrawable(this, R.drawable.rounded_background)?.mutate()
        customDrawable?.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        rec.background = customDrawable
        val expand = findViewById<ImageButton>(R.id.expand)
        expand.setOnClickListener {
            if (rec.visibility == View.GONE) {
                rec.visibility = View.VISIBLE
                expand.setImageResource(R.drawable.up)
            } else {
                rec.visibility = View.GONE
                expand.setImageResource(R.drawable.down)
            }
        }


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


//        Thread {
//            while (true) {
//                try {
//                    val aVal = socketMessage.take()
//                    if (aVal != null) {
//                        //Log.e("Abutton", aVal)
//                        outputStream?.write(aVal)?.also { outputStream.flush() }
//                    }
////                    outputStream?.write(socketAnalogueR.toByteArray())?.also { outputStream.flush() }
//                    Thread.sleep(1)
//                    //  socket.close()
//                } catch (e: SocketException) {
//                    Log.e("Error", "Error: ${e.message}")
//                    runOnUiThread {
//                        Toast.makeText(this@ControllerPlayActivity, "Connection Lost. Please Reconnect Controller", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        }.start()

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


        onLoadLayout(loadButtonList(this))
        record_layout.bringToFront()

//        findViewById<Button>(R.id.button).setOnClickListener {
//            if (currentIndex>5)
//            {
//                Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//            currentIndex++
//            if(currentIndex<values1.size) {
//                val layoutParams1 = RelativeLayout.LayoutParams(values1[currentIndex],values1[currentIndex])
//                joystickl.layoutParams = layoutParams1
//
//            }
//            saveimage(joystickl,"joystick_l")
//            saveimage(joystickr,"joystick_r")
//        }

    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private fun onLoadLayout(buttonList: MutableList<Pair<Pair<Pair<Int, Int>, Pair<Int, Int>>, Pair<Pair<Float, Float>, Pair<Int, Int>>>>) {
        if (this.getSharedPreferences("selected_macros", Context.MODE_PRIVATE).getBoolean("aim_touch", false)) {
            val aimTouchView = AimTouchView(this, null)
            val layoutParams = RelativeLayout.LayoutParams(resources.displayMetrics.widthPixels / 2, resources.displayMetrics.heightPixels)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
            aimTouchView.layoutParams = layoutParams
            playActivityMainContent.addView(aimTouchView)
            aimTouchView.setAimTouchListener(object : AimTouchView.AimTouchListener {
                override fun onAimTouchMoveMacro(aimTouchX: Float, aimTouchY: Float) {
                    if (isRecording)
                        macro_data.append("${delayTime()},R $aimTouchX $aimTouchY,")
                    if (!isMacro) {
                        val byteArray = Gamepad(Rx = aimTouchX.toInt().toShort(), Ry = aimTouchY.toInt()
                            .toShort(), isPressed = 0x01, isJoystick = 0x02)
                        sendData(byteArray)
                    }
                }
            })
        }
        for (buttonInfo in buttonList) {
            when (buttonInfo.first.first.first) {
                R.id.key_joystick_l -> {
                    val joystickView = JoystickView(this, null)
                    val layoutParams = RelativeLayout.LayoutParams(buttonInfo.first.second.first, buttonInfo.first.second.second)
                    layoutParams.leftMargin = buttonInfo.second.second.first
                    layoutParams.topMargin = buttonInfo.second.second.second
                    joystickView.layoutParams = layoutParams
                    joystickView.id = R.id.key_joystick_l
                    joystickView.scaleX = buttonInfo.second.first.first
                    joystickView.scaleY = buttonInfo.second.first.second
                    joystickView.left = true
                    playActivityMainContent.addView(joystickView)
                    joystickView.setJoystickListener(object : JoystickView.JoystickListener {
                        override fun onJoystickDownMacro(joystickX: Int, joystickY: Int) {
                            if (isRecording)
                                macro_data.append("${delayTime()}|L $joystickX $joystickY,")
                            if (!isMacro) {
                                val byteArray = Gamepad(Lx = joystickX.toShort(), Ly = joystickY.toShort(), isPressed = 0x01, isJoystick = 0x01)
                                sendData(byteArray)
                            }
                        }

                        override fun onJoystickMoveMacro(joystickX: Int, joystickY: Int) {
                            if (isRecording)
                                macro_data.append("${delayTime()},L $joystickX $joystickY,")
                            if (!isMacro) {
                                val byteArray = Gamepad(Lx = joystickX.toShort(), Ly = joystickY.toShort(), isPressed = 0x01, isJoystick = 0x01)
                                sendData(byteArray)
                            }
                        }

                        override fun onJoystickUpMacro() {
                            if (isRecording)
                                macro_data.append("${delayTime()}|L0 0|")
                            if (!isMacro) {
                                val byteArray = Gamepad(isPressed = 0x01, isJoystick = 0x01)
                                sendData(byteArray)
                            }
                            vibrate()
                        }
                    })
                }

                R.id.key_joystick_r -> {
                    val joystickView = JoystickView(this, null)
                    val layoutParams = RelativeLayout.LayoutParams(buttonInfo.first.second.first, buttonInfo.first.second.second)
                    layoutParams.leftMargin = buttonInfo.second.second.first
                    layoutParams.topMargin = buttonInfo.second.second.second
                    joystickView.layoutParams = layoutParams
                    joystickView.id = R.id.key_joystick_r
                    joystickView.scaleX = buttonInfo.second.first.first
                    joystickView.scaleY = buttonInfo.second.first.second
                    joystickView.left = false
                    playActivityMainContent.addView(joystickView)
                    joystickView.setJoystickListener(object : JoystickView.JoystickListener {
                        override fun onJoystickDownMacro(joystickX: Int, joystickY: Int) {
                            if (isRecording)
                                macro_data.append("${delayTime()}|R $joystickX $joystickY,")
                            if (!isMacro) {
                                val byteArray = Gamepad(Rx = joystickX.toShort(), Ry = joystickY.toShort(), isPressed = 0x01, isJoystick = 0x02)
                                sendData(byteArray)
                            }
                        }

                        override fun onJoystickMoveMacro(joystickX: Int, joystickY: Int) {
                            if (isRecording)
                                macro_data.append("${delayTime()},R $joystickX $joystickY,")
                            if (!isMacro) {
                                val byteArray = Gamepad(Rx = joystickX.toShort(), Ry = joystickY.toShort(), isPressed = 0x01, isJoystick = 0x02)
                                sendData(byteArray)
                            }
                        }

                        override fun onJoystickUpMacro() {
                            if (isRecording)
                                macro_data.append("${delayTime()}|R0 0|")
                            if (!isMacro) {
                                val byteArray = Gamepad(isPressed = 0x01, isJoystick = 0x02)
                                sendData(byteArray)
                            }
                            vibrate()
                        }
                    })
                }

                R.id.key_dpad -> {
                    val dpadView = DpadView(this, null)
                    val layoutParams = RelativeLayout.LayoutParams(buttonInfo.first.second.first, buttonInfo.first.second.second)
                    layoutParams.leftMargin = buttonInfo.second.second.first
                    layoutParams.topMargin = buttonInfo.second.second.second
                    dpadView.layoutParams = layoutParams
                    dpadView.scaleX = buttonInfo.second.first.first
                    dpadView.scaleY = buttonInfo.second.first.second
                    playActivityMainContent.addView(dpadView)
                    dpadView.setDpadListener(object : DpadView.DpadListener {
                        override fun onDpadMacro(buttonId: String, wbutton: Int, isPressed: Byte) {
                            if (isRecording)
                                macro_data.append("|${delayTime()}|$buttonId|")
                            if (!isMacro){
                                var byteArray = Gamepad(wbutton = 0x0000, isPressed = 0x00, isDpad = 0x01)
                                sendData(byteArray)
                                byteArray = Gamepad(wbutton = wbutton, isPressed = isPressed, isDpad = 0x01)
                                sendData(byteArray)
                            }
                            if (buttonId.contains("Down"))
                                vibrate()
                        }
                    })
                }

                R.id.key_action_button -> {
                    val actionButtonView = ActionButtonView(this, null)
                    val layoutParams = RelativeLayout.LayoutParams(buttonInfo.first.second.first, buttonInfo.first.second.second)
                    layoutParams.leftMargin = buttonInfo.second.second.first
                    layoutParams.topMargin = buttonInfo.second.second.second
                    actionButtonView.layoutParams = layoutParams
                    actionButtonView.scaleX = buttonInfo.second.first.first
                    actionButtonView.scaleY = buttonInfo.second.first.second
                    playActivityMainContent.addView(actionButtonView)
                    actionButtonView.setActionButtonListener(object : ActionButtonView.ActionButtonListener {
                        override fun onActionMacro(buttonId: String, wbutton: Int, isPressed: Byte) {
                            if (isRecording)
                                macro_data.append("|${delayTime()}|$buttonId|")
                            if (!isMacro) {
                                val byteArray = Gamepad(wbutton = wbutton, isPressed = isPressed)
                                sendData(byteArray)
                            }
                            if (buttonId.contains("Down"))
                                vibrate()
                        }
                    })
                }

                R.id.key_circular_button -> {
                    val circularActionButtonView = CircularActionButtonView(this, null)
                    val layoutParams = RelativeLayout.LayoutParams(buttonInfo.first.second.first, buttonInfo.first.second.second)
                    layoutParams.leftMargin = buttonInfo.second.second.first
                    layoutParams.topMargin = buttonInfo.second.second.second
                    circularActionButtonView.layoutParams = layoutParams
                    circularActionButtonView.scaleX = buttonInfo.second.first.first
                    circularActionButtonView.scaleY = buttonInfo.second.first.second
                    playActivityMainContent.addView(circularActionButtonView)
                    circularActionButtonView.setCircularButtonListener(object : CircularActionButtonView.CircularButtonListener {
                        override fun onCircularMacro(buttonId: String, wbutton: Int, isPressed: Byte) {
                            if (isRecording)
                                macro_data.append("|${delayTime()}|$buttonId|")
                            if (!isMacro) {
                                val byteArray = Gamepad(wbutton = wbutton, isPressed = isPressed)
                                sendData(byteArray)
                            }
                            if (buttonId.contains("Down"))
                                vibrate()
                        }
                    })
                }

                else -> {
                    if (isMacro && buttonInfo.first.first.first in setOf(R.id.key_m1, R.id.key_m2, R.id.key_m3, R.id.key_m4))
                        continue
                    val button = ImageButton(this)
                    button.id = buttonInfo.first.first.first
                    button.setImageResource(buttonInfo.first.first.second)
                    button.scaleX = buttonInfo.second.first.first
                    button.scaleY = buttonInfo.second.first.second
                    button.background = null
                    button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                    val layoutParams = RelativeLayout.LayoutParams(buttonInfo.first.second.first, buttonInfo.first.second.second)
                    layoutParams.leftMargin = buttonInfo.second.second.first
                    layoutParams.topMargin = buttonInfo.second.second.second
                    button.layoutParams = layoutParams
                    playActivityMainContent.addView(button)
                    button.setOnTouchListener(this)
                }
            }
        }
    }

//    private fun saveimage(view: View, name: String)
//    {
//        view.isDrawingCacheEnabled = true
//        view.buildDrawingCache()
//        view.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
//        val bitmap = Bitmap.createBitmap(view.drawingCache)
//        view.isDrawingCacheEnabled = false
//        val imagesDir = File(this.getExternalFilesDir(null), "images")
//        imagesDir.mkdirs()
//        val imageFile = File(imagesDir, "${name}_${values1[currentIndex-1]}.png")
//        try {
//            val outputStream: OutputStream = FileOutputStream(imageFile)
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//            outputStream.flush()
//            outputStream.close()
//            Log.d("img", "Saved ${name}_${values1[currentIndex-1]}.png")
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

    private fun loadButtonList(context: Context): MutableList<Pair<Pair<Pair<Int, Int>, Pair<Int, Int>>, Pair<Pair<Float, Float>, Pair<Int, Int>>>> {
        val prefs = context.getSharedPreferences(profile, Context.MODE_PRIVATE)
        val buttonCount = prefs.getInt("button_count", 0)
        val buttonList: MutableList<Pair<Pair<Pair<Int, Int>, Pair<Int, Int>>, Pair<Pair<Float, Float>, Pair<Int, Int>>>> = mutableListOf()
        for (i in 0 until buttonCount) {
            val buttonId = prefs.getInt("button_${i}_id", 0)
            val buttonSrc = prefs.getInt("button_${i}_src", 0)
            val buttonWidth = prefs.getInt("button_${i}_width", 0)
            val buttonHeight = prefs.getInt("button_${i}_height", 0)
            val buttonScaleX = prefs.getFloat("button_${i}_scaleX", 0f)
            val buttonScaleY = prefs.getFloat("button_${i}_scaleY", 0f)
            val buttonX = prefs.getInt("button_${i}_x", 0)
            val buttonY = prefs.getInt("button_${i}_y", 0)
            buttonList.add(
                Pair(
                    Pair(Pair(buttonId, buttonSrc), Pair(buttonWidth, buttonHeight)),
                    Pair(Pair(buttonScaleX, buttonScaleY), Pair(buttonX, buttonY))
                )
            )
        }
        return buttonList
    }

    private fun vibrate() {
        if (vibrator.hasVibrator() && vibrate) {
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 0, 0, 22), -1))
            } else {
                vibrator.vibrate(22)
            }
        }
    }

    private fun delayTime(): String {
        val currentTime = System.currentTimeMillis()
        val timeDelay = currentTime - lastClickTime
        lastClickTime = currentTime
        return "${timeDelay}ms"
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun initCoroutines() {
        repeat(coroutinePoolSize) {
            GlobalScope.launch(Dispatchers.IO) {
                for (data in dataChannel) {
                    sendDataToSocket(data)
                }
            }
        }
    }

    private fun sendData(data: ByteArray) {
        if (macroClicked) {
            macroClicked = false
            for (buttonId in setOf(R.id.key_m1, R.id.key_m2, R.id.key_m3, R.id.key_m4))
                findViewById<ImageButton>(buttonId)?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        }
        dataChannel.trySend(data).isSuccess
    }

    private fun sendDataToSocket(data: ByteArray) {
        try {
            outputStream?.write(data)?.also { outputStream.flush() }
        } catch (e: SocketException) {
            Log.e("Error", "Error: ${e.message}")
            runOnUiThread {
                Toast.makeText(this@ControllerPlayActivity, "Connection Lost. Please Reconnect Controller", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun macroCancelHandler() {
        if (macroCount == 1 && threadExited) {
            GlobalScope.launch(Dispatchers.IO) {
                threadExited = false
                val inputStream = SharedObject.getSocket()?.getInputStream()
                while (true) {
                    try {
                        val macroStatus = inputStream?.read()
                        if (macroStatus == 1) {
                            threadExited = true
                            macroClicked = false
                            for (buttonId in setOf(R.id.key_m1, R.id.key_m2, R.id.key_m3, R.id.key_m4))
                                findViewById<ImageButton>(buttonId)?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        }
                        macroCount--
                        if (macroCount == 0)
                            break
                    } catch (_: Exception) {}
                }
            }
        }
    }

    private fun Gamepad(wbutton: Int = 0, LT: UByte = 0u, RT: UByte = 0u, Lx: Short = 0, Ly: Short = 0, Rx: Short = 0, Ry: Short = 0, isPressed: Byte = 0, isDpad: Byte = 0, isJoystick: Byte = 0, macro: Byte = 0): ByteArray {
        val data = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN).apply {
            put(byteArrayOf((wbutton and 0xFF).toByte(), ((wbutton shr 8) and 0xFF).toByte()))
            put(LT.toByte())
            put(RT.toByte())
            putShort(Lx)
            putShort(Ly)
            putShort(Rx)
            putShort(Ry)
            put(isPressed)
            put(isDpad)
            put(isJoystick)
            put(macro)
        }.array()
        return data
    }

    @SuppressLint("ClickableViewAccessibility", "NewApi")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
//        val i = event.actionIndex
//        val actionId = event.getPointerId(i)
//        val touchX = event.getX(i)
//        val touchY = event.getY(i)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                when (v.id) {
                    R.id.key_a -> {
                        val button = findViewById<ImageButton>(R.id.key_a)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|A Down|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x1000, isPressed = 0x01)
                            sendData(byteArray)
                        }
                        vibrate()
                    }

                    R.id.key_b -> {
                        val button = findViewById<ImageButton>(R.id.key_b)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|B Down|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x2000, isPressed = 0x01)
                            sendData(byteArray)
                        }
                        vibrate()
                    }

                    R.id.key_x -> {
                        val button = findViewById<ImageButton>(R.id.key_x)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|X Down|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x4000, isPressed = 0x01)
                            sendData(byteArray)
                        }
                        vibrate()
                    }

                    R.id.key_y -> {
                        val button = findViewById<ImageButton>(R.id.key_y)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|Y Down|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 32768, isPressed = 0x01)
                            sendData(byteArray)
                        }
                        vibrate()
                    }

                    R.id.key_lb -> {
                        val button = findViewById<ImageButton>(R.id.key_lb)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|LB Down|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0100, isPressed = 0x01)
                            sendData(byteArray)
                        }
                        vibrate()
                    }

                    R.id.key_lt -> {
                        val button = findViewById<ImageButton>(R.id.key_lt)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|LT Down|")
                        if (!isMacro) {
                            val byteArray = Gamepad(LT = 255u, isPressed = 0x01, isDpad = 0x02)
                            sendData(byteArray)
                        }
                        vibrate()
                    }

                    R.id.key_rb -> {
                        val button = findViewById<ImageButton>(R.id.key_rb)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|RB Down|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0200, isPressed = 0x01)
                            sendData(byteArray)
                        }
                        vibrate()
                    }

                    R.id.key_rt -> {
                        val button = findViewById<ImageButton>(R.id.key_rt)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|RT Down|")
                        if (!isMacro) {
                            val byteArray = Gamepad(RT = 255u, isPressed = 0x01, isDpad = 0x03)
                            sendData(byteArray)
                        }
                        vibrate()
                    }

                    R.id.key_start -> {
                        val button = findViewById<ImageButton>(R.id.key_start)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|Start Down|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0010, isPressed = 0x01)
                            sendData(byteArray)
                        }
                        vibrate()
                    }

                    R.id.key_select -> {
                        val button = findViewById<ImageButton>(R.id.key_select)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|Select Down|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0020, isPressed = 0x01)
                            sendData(byteArray)
                        }
                        vibrate()
                    }

                    R.id.key_lcenter -> {
                        val button = findViewById<ImageButton>(R.id.key_lcenter)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|Left Joystick Button Down|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0080, isPressed = 0x01)
                            sendData(byteArray)
                        }
                        vibrate()
                    }

                    R.id.key_rcenter -> {
                        val button = findViewById<ImageButton>(R.id.key_rcenter)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|Right Joystick Button Down|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0040, isPressed = 0x01)
                            sendData(byteArray)
                        }
                        vibrate()
                    }

                    R.id.key_m1 -> {
                        val byteArray = Gamepad(macro = 0x01)
                        sendData(byteArray)
                        val button = findViewById<ImageButton>(R.id.key_m1)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        macroClicked = true
                        macroCount++
                        vibrate()
                        macroCancelHandler()
                    }

                    R.id.key_m2 -> {
                        val byteArray = Gamepad(macro = 0x02)
                        sendData(byteArray)
                        val button = findViewById<ImageButton>(R.id.key_m2)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        macroClicked = true
                        macroCount++
                        vibrate()
                        macroCancelHandler()
                    }

                    R.id.key_m3 -> {
                        val byteArray = Gamepad(macro = 0x03)
                        sendData(byteArray)
                        val button = findViewById<ImageButton>(R.id.key_m3)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        macroClicked = true
                        macroCount++
                        vibrate()
                        macroCancelHandler()
                    }

                    R.id.key_m4 -> {
                        val byteArray = Gamepad(macro = 0x04)
                        sendData(byteArray)
                        val button = findViewById<ImageButton>(R.id.key_m4)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        macroClicked = true
                        macroCount++
                        vibrate()
                        macroCancelHandler()
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                when (v.id) {
                    R.id.key_a -> {
                        val button = findViewById<ImageButton>(R.id.key_a)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|A Up|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x1000, isPressed = 0x00)
                            sendData(byteArray)
                        }
                    }

                    R.id.key_b -> {
                        val button = findViewById<ImageButton>(R.id.key_b)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|B Up|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x2000, isPressed = 0x00)
                            sendData(byteArray)
                        }
                    }

                    R.id.key_x -> {
                        val button = findViewById<ImageButton>(R.id.key_x)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|X Up|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x4000, isPressed = 0x00)
                            sendData(byteArray)
                        }
                    }

                    R.id.key_y -> {
                        val button = findViewById<ImageButton>(R.id.key_y)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|Y Up|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 32768, isPressed = 0x00)
                            sendData(byteArray)
                        }
                    }

                    R.id.key_lb -> {
                        val button = findViewById<ImageButton>(R.id.key_lb)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|LB Up|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0100, isPressed = 0x00)
                            sendData(byteArray)
                        }
                    }

                    R.id.key_lt -> {
                        val button = findViewById<ImageButton>(R.id.key_lt)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|LT Up|")
                        if (!isMacro) {
                            val byteArray = Gamepad(LT = 0u, isPressed = 0x01, isDpad = 0x02)
                            sendData(byteArray)
                        }
                    }

                    R.id.key_rb -> {
                        val button = findViewById<ImageButton>(R.id.key_rb)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|RB Up|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0200, isPressed = 0x00)
                            sendData(byteArray)
                        }
                    }

                    R.id.key_rt -> {
                        val button = findViewById<ImageButton>(R.id.key_rt)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|RT Up|")
                        if (!isMacro) {
                            val byteArray = Gamepad(RT = 0u, isPressed = 0x01, isDpad = 0x03)
                            sendData(byteArray)
                        }
                    }

                    R.id.key_start -> {
                        val button = findViewById<ImageButton>(R.id.key_start)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|Start Up|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0010, isPressed = 0x00)
                            sendData(byteArray)
                        }
                    }

                    R.id.key_select -> {
                        val button = findViewById<ImageButton>(R.id.key_select)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|Select Up|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0020, isPressed = 0x00)
                            sendData(byteArray)
                        }
                    }

                    R.id.key_lcenter -> {
                        val button = findViewById<ImageButton>(R.id.key_lcenter)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|Left Joystick Button Up|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0080, isPressed = 0x00)
                            sendData(byteArray)
                        }
                    }

                    R.id.key_rcenter -> {
                        val button = findViewById<ImageButton>(R.id.key_rcenter)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|Right Joystick Button Up|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0040, isPressed = 0x00)
                            sendData(byteArray)
                        }
                    }
                }
            }
        }
        return true
    }

    //Deprecated above API 30 so checking API.
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            // Hide the status bar and navigation bar
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.apply {
                    hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
                window.setDecorFitsSystemWindows(false)
                window.statusBarColor = Color.TRANSPARENT
                window.navigationBarColor = Color.BLACK
                window.insetsController?.hide(WindowInsets.Type.statusBars())
            } else {
                @Suppress("DEPRECATION") window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
            }
        }
    }

}