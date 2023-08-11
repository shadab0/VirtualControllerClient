package com.example.zerocontrollerclient

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.marginRight
import com.google.android.material.button.MaterialButton
import java.io.File

class CreateMacroActivity : AppCompatActivity() {

    private lateinit var macro_list: GridLayout
    private var macro_name = ""

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_macro_activity)
        macro_name = intent.getStringExtra(Intent.EXTRA_TEXT).toString()

        findViewById<JoystickView>(R.id.key_joystick_r).left=false
        macro_list = findViewById(R.id.macro_list)

        findViewById<ImageButton>(R.id.key_a).setOnClickListener {
            addItem(R.mipmap.key_a,"A Down")
            addItem(R.mipmap.delay,"25ms",true)
            addItem(R.mipmap.key_a,"A Up")
        }
        findViewById<ImageButton>(R.id.key_b).setOnClickListener {
            addItem(R.mipmap.key_b,"B Down")
            addItem(R.mipmap.delay,"25ms",true)
            addItem(R.mipmap.key_b,"B Up")
        }
        findViewById<ImageButton>(R.id.key_x).setOnClickListener {
            addItem(R.mipmap.key_x,"X Down")
            addItem(R.mipmap.delay,"25ms",true)
            addItem(R.mipmap.key_x,"X Up")
        }
        findViewById<ImageButton>(R.id.key_y).setOnClickListener {
            addItem(R.mipmap.key_y,"Y Down")
            addItem(R.mipmap.delay,"25ms",true)
            addItem(R.mipmap.key_y,"Y Up")
        }
        findViewById<ImageButton>(R.id.key_lb).setOnClickListener {
            addItem(R.mipmap.key_lb,"LB Down")
            addItem(R.mipmap.delay,"25ms",true)
            addItem(R.mipmap.key_lb,"LB Up")
        }
        findViewById<ImageButton>(R.id.key_lt).setOnClickListener {
            addItem(R.mipmap.key_lt,"LT Down")
            addItem(R.mipmap.delay,"25ms",true)
            addItem(R.mipmap.key_lt,"LT Up")
        }
        findViewById<ImageButton>(R.id.key_rb).setOnClickListener {
            addItem(R.mipmap.key_rb,"RB Down")
            addItem(R.mipmap.delay,"25ms",true)
            addItem(R.mipmap.key_rb,"RB Up")
        }
        findViewById<ImageButton>(R.id.key_rt).setOnClickListener {
            addItem(R.mipmap.key_rt,"RT Down")
            addItem(R.mipmap.delay,"25ms",true)
            addItem(R.mipmap.key_rt,"RT Up")
        }
        findViewById<ImageButton>(R.id.key_start).setOnClickListener {
            addItem(R.mipmap.key_start,"Start Down")
            addItem(R.mipmap.delay,"25ms",true)
            addItem(R.mipmap.key_start,"Start Up")
        }
        findViewById<ImageButton>(R.id.key_select).setOnClickListener {
            addItem(R.mipmap.key_select,"Select Down")
            addItem(R.mipmap.delay,"25ms",true)
            addItem(R.mipmap.key_select,"Select Up")
        }
        findViewById<ImageButton>(R.id.key_lcenter).setOnClickListener {
            addItem(R.mipmap.key_lcenter,"Left Joystick Button Down")
            addItem(R.mipmap.delay,"25ms",true)
            addItem(R.mipmap.key_lcenter,"Left Joystick Button Up")
        }
        findViewById<ImageButton>(R.id.key_rcenter).setOnClickListener {
            addItem(R.mipmap.key_rcenter,"Right Joystick Button Down")
            addItem(R.mipmap.delay,"25ms",true)
            addItem(R.mipmap.key_rcenter,"Right Joystick Button Up")
        }
        findViewById<JoystickView>(R.id.key_joystick_l).setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                    val joysticklInput = (v as JoystickView).joystickInput.toString().trim()
                    addItem(R.mipmap.key_joystick_l, "Left Joystick Up",false,joysticklInput)
                }
            }
            v.onTouchEvent(event)
            true
        }
        findViewById<JoystickView>(R.id.key_joystick_r).setOnTouchListener { v, event ->
            v.onTouchEvent(event)
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_POINTER_DOWN -> {
//                    addItem(R.mipmap.key_joystick_r, "Right Joystick Down")
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                    addItem(R.mipmap.key_joystick_r, "Right Joystick Up")
                }
            }
            true
        }
        findViewById<ImageButton>(R.id.key_delay).setOnClickListener {
            addItem(R.mipmap.delay,"25ms",true)
        }

        findViewById<Button>(R.id.clear).setOnClickListener {
            macro_list.removeAllViews()
        }

        findViewById<Button>(R.id.save).setOnClickListener {
            var string = ""
            for (i in 0 until macro_list.rowCount)
                string += (macro_list.getChildAt(i * macro_list.columnCount + 1)as TextView).text.toString() + "|"
            val file = File(File(applicationContext.dataDir, "macros"), "$macro_name.txt")
            file.writeText(string)
        }

        onLoad()

    }

    private fun onLoad()
    {
        val string = File(File(applicationContext.dataDir, "macros"), "$macro_name.txt").readText()
        for (item in string.split("|")) {
            if (item=="")
                break
            when (item){
                "A Down","A Up" -> {
                    addItem(R.mipmap.key_a,item)
                }
                "B Down","B Up" -> {
                    addItem(R.mipmap.key_b,item)
                }
                "X Down","X Up" -> {
                    addItem(R.mipmap.key_x,item)
                }
                "Y Down","Y Up" -> {
                    addItem(R.mipmap.key_y,item)
                }
                "LB Down","LB Up" -> {
                    addItem(R.mipmap.key_lb,item)
                }
                "LT Down","LT Up" -> {
                    addItem(R.mipmap.key_lt,item)
                }
                "RB Down","RB Up" -> {
                    addItem(R.mipmap.key_rb,item)
                }
                "RT Down","RT Up" -> {
                    addItem(R.mipmap.key_rt,item)
                }
                "Start Down","Start Up" -> {
                    addItem(R.mipmap.key_start,item)
                }
                "Select Down","Select Up" -> {
                    addItem(R.mipmap.key_select,item)
                }
                "Left Joystick Button Down","Left Joystick Button Up" -> {
                    addItem(R.mipmap.key_lcenter,item)
                }
                "Right Joystick Button Down","Right Joystick Button Up" -> {
                    addItem(R.mipmap.key_rcenter,item)
                }
                "Left Joystick" -> {
                    addItem(R.mipmap.key_joystick_l,item)
                }
                "Right Joystick" -> {
                    addItem(R.mipmap.key_joystick_r,item)
                }
                else -> {
                    addItem(R.mipmap.delay,item,true)
                }

            }
        }
    }

    private fun addItem(id: Int, text: String, isdelay: Boolean=false,data: String=""){
        val img = ImageView(this)
        img.layoutParams = GridLayout.LayoutParams().apply {
            width = 100
            height = 100
            leftMargin=30
            rightMargin=50
            columnSpec = GridLayout.spec(0)
            setGravity(Gravity.CENTER)
        }
        img.setImageResource(id)
        img.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        macro_list.addView(img)

        val textview = TextView(this)
        textview.layoutParams = GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(1, 1, 1f) // Span 1 column with weight
            setGravity(Gravity.FILL_HORIZONTAL or Gravity.CENTER_VERTICAL)
        }
        textview.text=text
        textview.textSize=20f
        textview.tag=data
        textview.setTextColor(Color.WHITE)
        if(isdelay) {
            textview.setOnClickListener {
                val dialogBuilder = AlertDialog.Builder(this)
                val editText = EditText(this)
                editText.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
                editText.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
                dialogBuilder.setView(editText)

                dialogBuilder.setPositiveButton("OK") { _, _ ->
                    val sec = editText.text.toString().trim()
                    textview.text = "${sec}ms"
                }

                dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog1 = dialogBuilder.create()
                dialog1.show()
                dialog1.window?.setBackgroundDrawableResource(com.google.android.material.R.color.material_dynamic_neutral10)
            }
        }
        macro_list.addView(textview)

        val del = ImageButton(this)
        del.layoutParams = GridLayout.LayoutParams().apply {
            width = GridLayout.LayoutParams.WRAP_CONTENT
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(2) // Occupies the second column
            setGravity(Gravity.CENTER)
        }
        del.setImageResource(R.mipmap.del)
        del.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        del.background = null
        del.setOnClickListener { view ->
            val parent = view.parent as GridLayout
            val row = parent.indexOfChild(view) - 2
            parent.removeViewAt(row)
            parent.removeViewAt(row)
            parent.removeViewAt(row)
        }
        macro_list.addView(del)
        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        scrollView.post {
            scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

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