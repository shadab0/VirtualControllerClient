package com.example.zerocontrollerclient

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.ConcurrentLinkedQueue


@Suppress("DEPRECATION")
class ControllerPlayActivity : AppCompatActivity(), View.OnTouchListener {

    private lateinit var playActivityMainContent: RelativeLayout

    private val socketMessage = ConcurrentLinkedQueue<String>()
    private var socketAnalogueR = ""
    private var socketAnalogueL = ""

    private var profile = ""
//    val values1 = arrayOf(0, 120, 180, 240, 360, 480)
//    var currentIndex = 0


    private lateinit var handler: Handler

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controller_play)
        profile = intent.getStringExtra(Intent.EXTRA_TEXT).toString()
        Toast.makeText(this,"Loaded $profile", Toast.LENGTH_SHORT).show()
        playActivityMainContent = findViewById(R.id.playActivityMainContent)


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


        onLoadLayout(loadButtonList(this))

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
    private fun onLoadLayout(buttonList: MutableList<Pair<Pair<Pair<Int,Int>,Pair<Int,Int>>,Pair<Pair<Float,Float>,Pair<Int,Int>>>>){
        for (buttonInfo in buttonList) {
            when (buttonInfo.first.first.first) {
                R.id.key_joystick_l -> {
                    val joystickView = JoystickView(this,null)
                    val layoutParams = RelativeLayout.LayoutParams(buttonInfo.first.second.first,buttonInfo.first.second.second)
                    layoutParams.leftMargin = buttonInfo.second.second.first
                    layoutParams.topMargin = buttonInfo.second.second.second
                    joystickView.layoutParams = layoutParams
                    joystickView.scaleX = buttonInfo.second.first.first
                    joystickView.scaleY = buttonInfo.second.first.second
                    joystickView.left = true
                    playActivityMainContent.addView(joystickView)
                }
                R.id.key_joystick_r -> {
                    val joystickView = JoystickView(this,null)
                    val layoutParams = RelativeLayout.LayoutParams(buttonInfo.first.second.first,buttonInfo.first.second.second)
                    layoutParams.leftMargin = buttonInfo.second.second.first
                    layoutParams.topMargin = buttonInfo.second.second.second
                    joystickView.layoutParams = layoutParams
                    joystickView.scaleX = buttonInfo.second.first.first
                    joystickView.scaleY = buttonInfo.second.first.second
                    joystickView.left = false
                    playActivityMainContent.addView(joystickView)
                }
                R.id.key_dpad -> {
                    val dpadView = DpadView(this,null)
                    val layoutParams = RelativeLayout.LayoutParams(buttonInfo.first.second.first,buttonInfo.first.second.second)
                    layoutParams.leftMargin = buttonInfo.second.second.first
                    layoutParams.topMargin = buttonInfo.second.second.second
                    dpadView.layoutParams = layoutParams
                    dpadView.scaleX = buttonInfo.second.first.first
                    dpadView.scaleY = buttonInfo.second.first.second
                    playActivityMainContent.addView(dpadView)
                }
                R.id.key_action_button -> {
                    val actionButtonView = ActionButtonView(this,null)
                    val layoutParams = RelativeLayout.LayoutParams(buttonInfo.first.second.first,buttonInfo.first.second.second)
                    layoutParams.leftMargin = buttonInfo.second.second.first
                    layoutParams.topMargin = buttonInfo.second.second.second
                    actionButtonView.layoutParams = layoutParams
                    actionButtonView.scaleX = buttonInfo.second.first.first
                    actionButtonView.scaleY = buttonInfo.second.first.second
                    playActivityMainContent.addView(actionButtonView)
                }
                R.id.key_circular_button -> {
                    val circularActionButtonView = CircularActionButtonView(this,null)
                    val layoutParams = RelativeLayout.LayoutParams(buttonInfo.first.second.first,buttonInfo.first.second.second)
                    layoutParams.leftMargin = buttonInfo.second.second.first
                    layoutParams.topMargin = buttonInfo.second.second.second
                    circularActionButtonView.layoutParams = layoutParams
                    circularActionButtonView.scaleX = buttonInfo.second.first.first
                    circularActionButtonView.scaleY = buttonInfo.second.first.second
                    playActivityMainContent.addView(circularActionButtonView)
                }
                else -> {
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

    private fun loadButtonList(context: Context): MutableList<Pair<Pair<Pair<Int,Int>,Pair<Int,Int>>,Pair<Pair<Float,Float>,Pair<Int,Int>>>> {
        val prefs = context.getSharedPreferences(profile, Context.MODE_PRIVATE)
        val buttonCount = prefs.getInt("button_count", 0)
        val buttonList: MutableList<Pair<Pair<Pair<Int,Int>,Pair<Int,Int>>,Pair<Pair<Float,Float>,Pair<Int,Int>>>> = mutableListOf()
        for (i in 0 until buttonCount) {
            val buttonId = prefs.getInt("button_${i}_id", 0)
            val buttonSrc = prefs.getInt("button_${i}_src", 0)
            val buttonWidth = prefs.getInt("button_${i}_width", 0)
            val buttonHeight = prefs.getInt("button_${i}_height", 0)
            val buttonScaleX = prefs.getFloat("button_${i}_scaleX", 0f)
            val buttonScaleY = prefs.getFloat("button_${i}_scaleY", 0f)
            val buttonX = prefs.getInt("button_${i}_x", 0)
            val buttonY = prefs.getInt("button_${i}_y", 0)
            buttonList.add(Pair(Pair(Pair(buttonId,buttonSrc),Pair(buttonWidth,buttonHeight)),Pair(Pair(buttonScaleX,buttonScaleY),Pair(buttonX,buttonY))))
        }
        return buttonList
    }


    @SuppressLint("ClickableViewAccessibility", "NewApi")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
//        val i = event.actionIndex
//        val actionId = event.getPointerId(i)
//        val touchX = event.getX(i)
//        val touchY = event.getY(i)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,MotionEvent.ACTION_POINTER_DOWN -> {
                when (v.id) {
                    R.id.key_a -> {
                        val button = findViewById<ImageButton>(R.id.key_a)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        socketMessage.add("1,")
                        Log.d("Play", "A Down")
                    }
                    R.id.key_b -> {
                        val button = findViewById<ImageButton>(R.id.key_b)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        socketMessage.add("2,")
                        Log.d("Play", "B Down")
                    }
                    R.id.key_x -> {
                        val button = findViewById<ImageButton>(R.id.key_x)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        socketMessage.add("1,")
                        Log.d("Play", "X Down")
                    }
                    R.id.key_y -> {
                        val button = findViewById<ImageButton>(R.id.key_y)
                        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                        socketMessage.add("2,")
                        Log.d("Play", "Y Down")
                    }
                }
            }
            MotionEvent.ACTION_UP,MotionEvent.ACTION_POINTER_UP -> {
                when (v.id) {
                    R.id.key_a -> {
                        val button = findViewById<ImageButton>(R.id.key_a)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        socketMessage.add("-1,")
                        Log.d("Play", "A Up")
                    }
                    R.id.key_b -> {
                        val button = findViewById<ImageButton>(R.id.key_b)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        socketMessage.add("-2,")
                        Log.d("Play", "B Up")
                    }
                    R.id.key_x -> {
                        val button = findViewById<ImageButton>(R.id.key_x)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        socketMessage.add("-1,")
                        Log.d("Play", "X Up")
                    }
                    R.id.key_y -> {
                        val button = findViewById<ImageButton>(R.id.key_y)
                        button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        socketMessage.add("-2,")
                        Log.d("Play", "Y Up")
                    }
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                when (v.id) {
                    R.id.key_a -> {
                        val button = findViewById<ImageButton>(R.id.key_a)
                        button.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN)
                        socketMessage.add("-1,")
                        Log.d("Play", "A Cancel Up")
                    }
                    R.id.key_b -> {
                        val button = findViewById<ImageButton>(R.id.key_b)
                        button.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN)
                        socketMessage.add("-2,")
                        Log.d("Play", "B Cancel Up")
                    }
                    R.id.key_x -> {
                        val button = findViewById<ImageButton>(R.id.key_x)
                        button.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN)
                        socketMessage.add("-1,")
                        Log.d("Play", "X Cancel Up")
                    }
                    R.id.key_y -> {
                        val button = findViewById<ImageButton>(R.id.key_y)
                        button.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN)
                        socketMessage.add("-2,")
                        Log.d("Play", "Y Cancel Up")
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
                @Suppress("DEPRECATION")
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
            }
        }
    }
}