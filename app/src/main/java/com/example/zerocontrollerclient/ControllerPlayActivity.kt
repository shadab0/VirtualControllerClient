package com.example.zerocontrollerclient

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("DEPRECATION")
class ControllerPlayActivity : AppCompatActivity(), View.OnTouchListener, SensorEventListener {

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

    private var macroCount = 0
    private var threadExited = true
    private val outputStream = SharedObject.getOutputStream()
    private val coroutinePoolSize = 1 // Number of worker coroutines
    private val dataChannel = Channel<ByteArray>(Channel.UNLIMITED)

    private val handler = Handler()
    private val isTouching = mutableMapOf<Int, Boolean>()

    private var sensorManager: SensorManager? = null
    private var gyroscopeSensor: Sensor? = null
    private var isGyroEnabled = false
    private var gyroSensitivity = 10f
    private val gyroRunning = AtomicBoolean(false)
    private var gyroThread: Thread? = null
    @Volatile private var targetGyroX = 0f
    @Volatile private var targetGyroY = 0f
    @Volatile private var lastGyroMoveTime = 0L
    private val GYRO_STOP_TIMEOUT_MS = 20L

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

        onLoadLayout(loadButtonList(this))
        record_layout.bringToFront()
    }

    override fun onResume() {
        super.onResume()
        val sharedPrefs = getSharedPreferences("selected_macros", Context.MODE_PRIVATE)
        isGyroEnabled = sharedPrefs.getBoolean("gyroscope", false)
        val baseSens = sharedPrefs.getFloat("gyro_sensitivity", 50f).coerceIn(1f, 100f)
        gyroSensitivity = baseSens * 100f

        if (isGyroEnabled) {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            gyroscopeSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            sensorManager?.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME)
            startGyroLoop()
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
        stopGyroLoop()
    }

    private fun startGyroLoop() {
        if (gyroRunning.get()) return
        gyroRunning.set(true)
        gyroThread = Thread {
            val intervalMs = 4L
            var lastSentX = 0f
            var lastSentY = 0f
            while (gyroRunning.get()) {
                val loopStart = System.currentTimeMillis()

                val mt = lastGyroMoveTime
                if (mt > 0L && (System.currentTimeMillis() - mt) > GYRO_STOP_TIMEOUT_MS) {
                    targetGyroX = 0f
                    targetGyroY = 0f
                }

                val tx = targetGyroX
                val ty = targetGyroY

                if (tx != 0f || ty != 0f) {
                    if (tx.toInt() != lastSentX.toInt() || ty.toInt() != lastSentY.toInt()) {
                        lastSentX = tx
                        lastSentY = ty
                        val byteArray = Gamepad(Rx = tx.toInt().toShort(), Ry = ty.toInt().toShort(), isPressed = 0x01, isJoystick = 0x02)
                        sendData(byteArray)
                    }
                } else if (lastSentX != 0f || lastSentY != 0f) {
                    lastSentX = 0f
                    lastSentY = 0f
                    val byteArray = Gamepad(Rx = 0, Ry = 0, isPressed = 0x01, isJoystick = 0x02)
                    sendData(byteArray)
                }

                val elapsedMs = System.currentTimeMillis() - loopStart
                val sleepMs = intervalMs - elapsedMs
                if (sleepMs > 0) Thread.sleep(sleepMs)
            }
        }.also {
            it.name = "GyroLoop"
            it.isDaemon = true
            it.start()
        }
    }

    private fun stopGyroLoop() {
        gyroRunning.set(false)
        gyroThread?.interrupt()
        gyroThread = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE && isGyroEnabled) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Landscape orientation: X is Yaw (Horizontal), Y is Pitch (Vertical)
            // Y is inverted based on user preference
            val rawX = -x * gyroSensitivity
            val rawY = -y * gyroSensitivity

            val MAX_JOYSTICK = 32767f
            val MIN_JOY_OUTPUT = 8000f

            var joyX = rawX.coerceIn(-MAX_JOYSTICK, MAX_JOYSTICK)
            var joyY = rawY.coerceIn(-MAX_JOYSTICK, MAX_JOYSTICK)

            // Linearly map the remaining input to the game's hardware stick travel bounds
            val GYRO_JITTER = 100f
            val magnitude = kotlin.math.sqrt(joyX * joyX + joyY * joyY)
            
            if (magnitude > GYRO_JITTER) {
                val mag = magnitude.coerceAtMost(MAX_JOYSTICK)
                val t = (mag - GYRO_JITTER) / (MAX_JOYSTICK - GYRO_JITTER)
                val outputMag = MIN_JOY_OUTPUT + t * (MAX_JOYSTICK - MIN_JOY_OUTPUT)
                val scale = outputMag / magnitude
                joyX *= scale
                joyY *= scale
            } else {
                joyX = 0f
                joyY = 0f
            }

            targetGyroX = joyX
            targetGyroY = joyY
            lastGyroMoveTime = System.currentTimeMillis()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

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
                    //setupButton(button)
                }
            }
        }
    }

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

    private fun Gamepad(wbutton: Int = 0, LT: UByte = 0u, RT: UByte = 0u, Lx: Short = 0, Ly: Short = 0, Rx: Short = 0, Ry: Short = 0, isPressed: Byte = 0, isDpad: Byte = 0, isJoystick: Byte = 0, macro: Byte = 0, keyboard: Byte = 0): ByteArray {
        val data = ByteBuffer.allocate(17).order(ByteOrder.LITTLE_ENDIAN).apply {
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
            put(keyboard)
        }.array()
        return data
    }

    @SuppressLint("ClickableViewAccessibility", "NewApi")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
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