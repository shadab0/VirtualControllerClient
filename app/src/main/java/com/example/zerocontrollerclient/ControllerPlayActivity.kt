package com.example.zerocontrollerclient

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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

    private val handler = Handler()
    private val isTouching = mutableMapOf<Int, Boolean>()

    private var touchJoyX = 0f
    private var touchJoyY = 0f
    private var physJoyRx = 0
    private var physJoyRy = 0
    private var gyroJoyX = 0f
    private var gyroJoyY = 0f

    private lateinit var sensorManager: SensorManager
    private var gyroSensor: Sensor? = null
    private var isGyroEnabled = false
    private var gyroSensitivity = 1800f
    private var gyroAntiDeadzone = 7500f
    private var aimTouchViewRef: AimTouchView? = null

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
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        setupDrawerSettings()

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

    override fun onResume() {
        super.onResume()
        gyroSensor?.let {
            sensorManager.registerListener(gyroListener, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(gyroListener)
    }

    private val gyroListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (!isGyroEnabled) {
                gyroJoyX = 0f
                gyroJoyY = 0f
                return
            }

            // Corrected standard mapping for Landscape Gyro
            val yaw = -(event.values[0] + event.values[2])
            val pitch = -event.values[1]

            val rawGyroX = yaw * gyroSensitivity
            val rawGyroY = pitch * gyroSensitivity

            val magnitude = kotlin.math.sqrt((rawGyroX * rawGyroX + rawGyroY * rawGyroY).toDouble()).toFloat()

            // Must be high enough to prevent natural hand noise from triggering the 7500 anti-deadzone
            // which causes massive jitter and drowns out the Touchpad
            val noiseDeadzone = 100f
            
            if (magnitude > noiseDeadzone) {
                val normalizedX = rawGyroX / magnitude
                val normalizedY = rawGyroY / magnitude

                val remainingRange = 32767f - gyroAntiDeadzone
                val scaledMagnitude = gyroAntiDeadzone + (magnitude - noiseDeadzone) * (remainingRange / 32767f)

                // Instant 1:1 mapping with square clamping for accurate diagonal speed
                gyroJoyX = (normalizedX * scaledMagnitude).coerceIn(-32767f, 32767f)
                gyroJoyY = (normalizedY * scaledMagnitude).coerceIn(-32767f, 32767f)
            } else {
                gyroJoyX = 0f
                gyroJoyY = 0f
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private fun setupDrawerSettings() {
        val sharedPrefs = getSharedPreferences("selected_macros", Context.MODE_PRIVATE)
        isGyroEnabled = sharedPrefs.getBoolean("gyro_enabled", false)
        gyroSensitivity = sharedPrefs.getFloat("gyro_sens", 1800f)
        gyroAntiDeadzone = sharedPrefs.getFloat("gyro_ad", 7500f)

        val switchGyro = findViewById<Switch>(R.id.switch_gyro)
        val seekbarGyroSens = findViewById<SeekBar>(R.id.seekbar_gyro_sens)
        val seekbarGyroAd = findViewById<SeekBar>(R.id.seekbar_gyro_ad)
        val seekbarTouchSens = findViewById<SeekBar>(R.id.seekbar_touch_sens)
        val seekbarTouchAd = findViewById<SeekBar>(R.id.seekbar_touch_ad)

        switchGyro.isChecked = isGyroEnabled
        seekbarGyroSens.progress = gyroSensitivity.toInt()
        seekbarGyroAd.progress = gyroAntiDeadzone.toInt()
        seekbarTouchSens.progress = sharedPrefs.getFloat("sensitivity", 1800f).toInt()
        seekbarTouchAd.progress = sharedPrefs.getFloat("anti_deadzone", 7500f).toInt()

        switchGyro.setOnCheckedChangeListener { _, isChecked ->
            isGyroEnabled = isChecked
            sharedPrefs.edit().putBoolean("gyro_enabled", isChecked).apply()
        }

        val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                val editor = sharedPrefs.edit()
                when (seekBar?.id) {
                    R.id.seekbar_gyro_sens -> {
                        gyroSensitivity = progress.toFloat()
                        editor.putFloat("gyro_sens", gyroSensitivity)
                    }
                    R.id.seekbar_gyro_ad -> {
                        gyroAntiDeadzone = progress.toFloat()
                        editor.putFloat("gyro_ad", gyroAntiDeadzone)
                    }
                    R.id.seekbar_touch_sens -> {
                        editor.putFloat("sensitivity", progress.toFloat())
                        aimTouchViewRef?.reloadSettings()
                    }
                    R.id.seekbar_touch_ad -> {
                        editor.putFloat("anti_deadzone", progress.toFloat())
                        aimTouchViewRef?.reloadSettings()
                    }
                }
                editor.apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

        seekbarGyroSens.setOnSeekBarChangeListener(seekBarListener)
        seekbarGyroAd.setOnSeekBarChangeListener(seekBarListener)
        seekbarTouchSens.setOnSeekBarChangeListener(seekBarListener)
        seekbarTouchAd.setOnSeekBarChangeListener(seekBarListener)
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private fun onLoadLayout(buttonList: MutableList<Pair<Pair<Pair<Int, Int>, Pair<Int, Int>>, Pair<Pair<Float, Float>, Pair<Int, Int>>>>) {
        if (this.getSharedPreferences("selected_macros", Context.MODE_PRIVATE).getBoolean("aim_touch", false)) {
            val aimTouchView = AimTouchView(this, null)
            aimTouchViewRef = aimTouchView
            val layoutParams = RelativeLayout.LayoutParams(resources.displayMetrics.widthPixels / 2, resources.displayMetrics.heightPixels)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
            aimTouchView.layoutParams = layoutParams
            playActivityMainContent.addView(aimTouchView)
            aimTouchView.setAimTouchListener(object : AimTouchView.AimTouchListener {
                override fun onAimTouchMoveMacro(aimTouchX: Float, aimTouchY: Float) {
                    if (isRecording)
                        macro_data.append("${delayTime()},R $aimTouchX $aimTouchY,")
                    if (!isMacro) {
                        touchJoyX = aimTouchX
                        touchJoyY = aimTouchY
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
                                controllerState.Lx = joystickX.toShort()
                                controllerState.Ly = joystickY.toShort()
                            }
                        }

                        override fun onJoystickMoveMacro(joystickX: Int, joystickY: Int) {
                            if (isRecording)
                                macro_data.append("${delayTime()},L $joystickX $joystickY,")
                            if (!isMacro) {
                                controllerState.Lx = joystickX.toShort()
                                controllerState.Ly = joystickY.toShort()
                            }
                        }

                        override fun onJoystickUpMacro() {
                            if (isRecording)
                                macro_data.append("${delayTime()}|L0 0|")
                            if (!isMacro) {
                                controllerState.Lx = 0.toShort()
                                controllerState.Ly = 0.toShort()
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
                                physJoyRx = joystickX
                                physJoyRy = joystickY
                            }
                        }

                        override fun onJoystickMoveMacro(joystickX: Int, joystickY: Int) {
                            if (isRecording)
                                macro_data.append("${delayTime()},R $joystickX $joystickY,")
                            if (!isMacro) {
                                physJoyRx = joystickX
                                physJoyRy = joystickY
                            }
                        }

                        override fun onJoystickUpMacro() {
                            if (isRecording)
                                macro_data.append("${delayTime()}|R0 0|")
                            if (!isMacro) {
                                physJoyRx = 0
                                physJoyRy = 0
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
                    //setupButton(button)
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

    data class ControllerState(
        var wbutton: Int = 0,
        var LT: UByte = 0u,
        var RT: UByte = 0u,
        var Lx: Short = 0,
        var Ly: Short = 0,
        var Rx: Short = 0,
        var Ry: Short = 0,
        var isPressed: Byte = 0,
        var isDpad: Byte = 0,
        var isJoystick: Byte = 0,
        var isMacro: Byte = 0
    )

    private val controllerState = ControllerState()
    private var sequenceNumber: UInt = 0u
    private var isLoopRunning = true

    private fun initCoroutines() {
        val udpSocket = SharedObject.getSocket()
        if (udpSocket == null || SharedObject.serverIp.isEmpty()) return
        
        val serverAddress = java.net.InetAddress.getByName(SharedObject.serverIp)
        val serverPort = SharedObject.serverPort
        val clientSlot = SharedObject.clientSlot
        
        val buffer = ByteArray(22)
        val packet = java.net.DatagramPacket(buffer, buffer.size, serverAddress, serverPort)
        buffer[0] = 0x01.toByte() // Packet Type: Data
        buffer[1] = clientSlot    // Client Slot
        
        val thread = Thread {
            while (isLoopRunning) {
                try {
                    sequenceNumber++
                    val seq = sequenceNumber.toInt()
                    buffer[2] = (seq and 0xFF).toByte()
                    buffer[3] = ((seq shr 8) and 0xFF).toByte()
                    buffer[4] = ((seq shr 16) and 0xFF).toByte()
                    buffer[5] = ((seq shr 24) and 0xFF).toByte()
                    
                    val wb = controllerState.wbutton
                    buffer[6] = (wb and 0xFF).toByte()
                    buffer[7] = ((wb shr 8) and 0xFF).toByte()
                    
                    buffer[8] = controllerState.LT.toByte()
                    buffer[9] = controllerState.RT.toByte()
                    
                    val lx = controllerState.Lx.toInt()
                    buffer[10] = (lx and 0xFF).toByte()
                    buffer[11] = ((lx shr 8) and 0xFF).toByte()
                    
                    val ly = controllerState.Ly.toInt()
                    buffer[12] = (ly and 0xFF).toByte()
                    buffer[13] = ((ly shr 8) and 0xFF).toByte()
                    
                    var totalRx = physJoyRx + touchJoyX + gyroJoyX
                    var totalRy = physJoyRy + touchJoyY + gyroJoyY
                    
                    val rx = totalRx.toInt().coerceIn(-32767, 32767)
                    controllerState.Rx = rx.toShort()
                    buffer[14] = (rx and 0xFF).toByte()
                    buffer[15] = ((rx shr 8) and 0xFF).toByte()
                    
                    val ry = totalRy.toInt().coerceIn(-32767, 32767)
                    controllerState.Ry = ry.toShort()
                    buffer[16] = (ry and 0xFF).toByte()
                    buffer[17] = ((ry shr 8) and 0xFF).toByte()
                    
                    buffer[18] = controllerState.isPressed
                    buffer[19] = controllerState.isDpad
                    buffer[20] = controllerState.isJoystick
                    buffer[21] = controllerState.isMacro
                    
                    udpSocket.send(packet)
                } catch (e: Exception) {
                    Log.e("UDP_LOOP", "Error: ${e.message}")
                }
                try {
                    Thread.sleep(4) // 250Hz = 4ms
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
        thread.priority = Thread.MAX_PRIORITY
        thread.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        isLoopRunning = false
        Thread {
            try {
                val udpSocket = SharedObject.getSocket()
                if (udpSocket != null && SharedObject.serverIp.isNotEmpty()) {
                    val serverAddress = java.net.InetAddress.getByName(SharedObject.serverIp)
                    val buffer = byteArrayOf(0x02) // Packet Type: Disconnect
                    val packet = java.net.DatagramPacket(buffer, buffer.size, serverAddress, SharedObject.serverPort)
                    udpSocket.send(packet)
                }
            } catch (e: Exception) {}
        }.start()
    }

    private fun sendData(data: ByteArray) {
        if (macroClicked) {
            macroClicked = false
            for (buttonId in setOf(R.id.key_m1, R.id.key_m2, R.id.key_m3, R.id.key_m4))
                findViewById<ImageButton>(buttonId)?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun macroCancelHandler() {
    }

    private fun Gamepad(wbutton: Int = 0, LT: UByte = 0u, RT: UByte = 0u, Lx: Short = 0, Ly: Short = 0, Rx: Short = 0, Ry: Short = 0, isPressed: Byte = 0, isDpad: Byte = 0, isJoystick: Byte = 0, macro: Byte = 0, keyboard: Byte = 0): ByteArray {
        if (isDpad.toInt() == 1) {
            if (isPressed.toInt() == 0 && wbutton == 0) {
                controllerState.wbutton = controllerState.wbutton and 0xFFF0
            } else {
                controllerState.wbutton = controllerState.wbutton and 0xFFF0
                controllerState.wbutton = controllerState.wbutton or wbutton
            }
        } else if (wbutton != 0) {
            if (isPressed.toInt() == 1) {
                controllerState.wbutton = controllerState.wbutton or wbutton
            } else {
                controllerState.wbutton = controllerState.wbutton and wbutton.inv()
            }
        }
        
        if (isDpad.toInt() == 2) controllerState.LT = LT
        if (isDpad.toInt() == 3) controllerState.RT = RT
        
        if (isJoystick.toInt() == 1) {
            controllerState.Lx = Lx
            controllerState.Ly = Ly
        } else if (isJoystick.toInt() == 2) {
            controllerState.Rx = Rx
            controllerState.Ry = Ry
        }
        
        return ByteArray(0)
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

                    R.id.v_key_a -> {
                        isTouching[v.id] = true
                        val button = findViewById<ImageButton>(R.id.v_key_a)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|A Down|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0041, isPressed = 0x01, keyboard = 0x01)
                            sendData(byteArray)
                        }
                        v.postDelayed({
                            startLoop(v.id, 0x0041)
                        }, 500)
                        vibrate()

                    }

                    R.id.v_key_b -> {
                        isTouching[v.id] = true
                        val button = findViewById<ImageButton>(R.id.v_key_b)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|A Down|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0042, isPressed = 0x01, keyboard = 0x01)
                            sendData(byteArray)
                        }
                        v.postDelayed({
                            startLoop(v.id, 0x0042)
                        }, 500)
                        vibrate()

                    }
                    R.id.v_key_c -> {
                        isTouching[v.id] = true
                        val button = findViewById<ImageButton>(R.id.v_key_c)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|A Down|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0043, isPressed = 0x01, keyboard = 0x01)
                            sendData(byteArray)
                        }
                        v.postDelayed({
                            startLoop(v.id, 0x0043)
                        }, 500)
                        vibrate()

                    }
                    R.id.v_key_d -> {
                        isTouching[v.id] = true
                        val button = findViewById<ImageButton>(R.id.v_key_d)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|A Down|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0044, isPressed = 0x01, keyboard = 0x01)
                            sendData(byteArray)
                        }
                        v.postDelayed({
                            startLoop(v.id, 0x0044)
                        }, 500)
                        vibrate()

                    }
                    R.id.v_key_e -> {
                        isTouching[v.id] = true
                        val button = findViewById<ImageButton>(R.id.v_key_e)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|A Down|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0045, isPressed = 0x01, keyboard = 0x01)
                            sendData(byteArray)
                        }
                        v.postDelayed({
                            startLoop(v.id, 0x0045)
                        }, 500)
                        vibrate()

                    }
                    R.id.v_key_f -> {
                        isTouching[v.id] = true
                        val button = findViewById<ImageButton>(R.id.v_key_f)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|A Down|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0046, isPressed = 0x01, keyboard = 0x01)
                            sendData(byteArray)
                        }
                        v.postDelayed({
                            startLoop(v.id, 0x0046)
                        }, 500)
                        vibrate()

                    }
                    R.id.v_key_g -> {
                        isTouching[v.id] = true
                        val button = findViewById<ImageButton>(R.id.v_key_g)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|A Down|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0047, isPressed = 0x01, keyboard = 0x01)
                            sendData(byteArray)
                        }
                        v.postDelayed({
                            startLoop(v.id, 0x0047)
                        }, 500)
                        vibrate()

                    }
                    R.id.v_key_h -> {
                        isTouching[v.id] = true
                        val button = findViewById<ImageButton>(R.id.v_key_h)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|A Down|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0048, isPressed = 0x01, keyboard = 0x01)
                            sendData(byteArray)
                        }
                        v.postDelayed({
                            startLoop(v.id, 0x0048)
                        }, 500)
                        vibrate()

                    }

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
                    R.id.v_key_a -> {
                        isTouching[v.id] = false
                        v.removeCallbacks(null);
                        val button = findViewById<ImageButton>(R.id.v_key_a)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|A Up|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0041, isPressed = 0x00, keyboard = 0x01)
                            sendData(byteArray)
                        }
                        Log.i("up", "a")
                    }
                    R.id.v_key_b -> {
                        isTouching[v.id] = false
                        v.removeCallbacks(null);
                        val button = findViewById<ImageButton>(R.id.v_key_b)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|A Up|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0042, isPressed = 0x00, keyboard = 0x01)
                            sendData(byteArray)
                        }
                        Log.i("up", "a")
                    }
                    R.id.v_key_c -> {
                        isTouching[v.id] = false
                        v.removeCallbacks(null);
                        val button = findViewById<ImageButton>(R.id.v_key_c)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|A Up|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0043, isPressed = 0x00, keyboard = 0x01)
                            sendData(byteArray)
                        }
                        Log.i("up", "a")
                    }
                    R.id.v_key_d -> {
                        isTouching[v.id] = false
                        v.removeCallbacks(null);
                        val button = findViewById<ImageButton>(R.id.v_key_d)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|A Up|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0044, isPressed = 0x00, keyboard = 0x01)
                            sendData(byteArray)
                        }
                        Log.i("up", "a")
                    }
                    R.id.v_key_e -> {
                        isTouching[v.id] = false
                        v.removeCallbacks(null);
                        val button = findViewById<ImageButton>(R.id.v_key_e)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|A Up|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0045, isPressed = 0x00, keyboard = 0x01)
                            sendData(byteArray)
                        }
                        Log.i("up", "a")
                    }
                    R.id.v_key_f -> {
                        isTouching[v.id] = false
                        v.removeCallbacks(null);
                        val button = findViewById<ImageButton>(R.id.v_key_f)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|A Up|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0046, isPressed = 0x00, keyboard = 0x01)
                            sendData(byteArray)
                        }
                        Log.i("up", "a")
                    }
                    R.id.v_key_g -> {
                        isTouching[v.id] = false
                        v.removeCallbacks(null);
                        val button = findViewById<ImageButton>(R.id.v_key_g)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|A Up|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0047, isPressed = 0x00, keyboard = 0x01)
                            sendData(byteArray)
                        }
                        Log.i("up", "a")
                    }
                    R.id.v_key_h -> {
                        isTouching[v.id] = false
                        v.removeCallbacks(null);
                        val button = findViewById<ImageButton>(R.id.v_key_h)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        if (isRecording)
                            macro_data.append("|${delayTime()}|A Up|")
                        if (!isMacro) {
                            val byteArray = Gamepad(wbutton = 0x0048, isPressed = 0x00, keyboard = 0x01)
                            sendData(byteArray)
                        }
                        Log.i("up", "a")
                    }
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

//    private fun setupButton(button: ImageButton) {
//        button.setOnLongClickListener {
//            val id = button.id
//            isTouching[id] = true
//            startLoop(id)
//            Log.i("long", "paa");
//            true // Return true to indicate the event was handled
//        }
//    }


    private fun startLoop(buttonId: Int, keycode: Int) {
        handler.post(object : Runnable {
            override fun run() {
                if (isTouching[buttonId] == true) {
                    val byteArray = Gamepad(wbutton = keycode, isPressed = 0x01, keyboard = 0x01)
                    sendData(byteArray)
                    handler.postDelayed(this, 50)
                }
                else {
                    return;
                }
            }
        })
    }
}