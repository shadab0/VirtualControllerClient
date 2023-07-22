package com.example.zerocontrollerclient

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.DragEvent
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.drawerlayout.widget.DrawerLayout
import kotlin.math.max
import kotlin.math.min

@Suppress("PrivatePropertyName")
class SetLayoutActivity : AppCompatActivity(), View.OnTouchListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawer: RelativeLayout
    private lateinit var drawerScrollview: ScrollView
    private lateinit var staticContainer: RelativeLayout
    private val position = IntArray(2)

    private var iconList: ArrayList<ImageButton> = ArrayList()
    private lateinit var key_a: ImageButton
    private lateinit var key_b: ImageButton
    private lateinit var key_x: ImageButton
    private lateinit var key_y: ImageButton
    private lateinit var key_lb: ImageButton
    private lateinit var key_lt: ImageButton
    private lateinit var key_rb: ImageButton
    private lateinit var key_rt: ImageButton
    private lateinit var key_start: ImageButton
    private lateinit var key_select: ImageButton
    private lateinit var key_lcenter: ImageButton
    private lateinit var key_rcenter: ImageButton
    private lateinit var key_m1: ImageButton
    private lateinit var key_m2: ImageButton
    private lateinit var key_m3: ImageButton
    private lateinit var key_m4: ImageButton
    private lateinit var key_joystick_l: ImageButton
    private lateinit var key_joystick_r: ImageButton
    private lateinit var key_dpad: ImageButton
    private lateinit var key_action_button: ImageButton
    private lateinit var key_circular_button: ImageButton
    private lateinit var mainContent: RelativeLayout
    private lateinit var removebtn: ImageButton
    private lateinit var savebtn: ImageButton
    private lateinit var lastClickedButton: ImageButton

    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scaleFactor = 1.0f
    private var profile = ""

    private var buttonList: MutableList<Pair<Pair<Pair<Int,Int>,Pair<Int,Int>>,Pair<Pair<Float,Float>,Pair<Int,Int>>>> = mutableListOf()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.set_layout_activity)

        profile = intent.getStringExtra(Intent.EXTRA_TEXT).toString()
        Toast.makeText(this,"Loaded $profile",Toast.LENGTH_SHORT).show()

        drawerLayout = findViewById(R.id.drawer_layout)
        drawer = findViewById(R.id.drawer)
        drawerScrollview = findViewById(R.id.drawer_scrollview)
        mainContent = findViewById(R.id.MainContent)
        removebtn = findViewById(R.id.removeButton)
        savebtn = findViewById(R.id.saveButton)
        staticContainer = findViewById(R.id.staticButtonContainer)

        drawerLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Remove the listener to avoid multiple calls
                drawerLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                staticContainer.getLocationInWindow(position)
                val h = drawerScrollview.height
                val width: Int
                val height: Int
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val displayContext = createDisplayContext(DisplayManagerCompat.getInstance(this@SetLayoutActivity).getDisplay(Display.DEFAULT_DISPLAY)!!)
                    width = displayContext.resources.displayMetrics.widthPixels
                    height = displayContext.resources.displayMetrics.heightPixels
                } else {
                    val displayMetrics = DisplayMetrics()
                    @Suppress("DEPRECATION")
                    windowManager.defaultDisplay.getMetrics(displayMetrics)
                    width = displayMetrics.widthPixels
                    height = displayMetrics.heightPixels
                }
                val layoutParams = drawerLayout.layoutParams
                layoutParams.width = (width * 1.5).toInt()
                layoutParams.height = (height * 1.5).toInt()
                drawerLayout.layoutParams = layoutParams
                drawerLayout.requestLayout()
                val layoutParams1 = drawerScrollview.layoutParams as DrawerLayout.LayoutParams
                layoutParams1.height = h
                val layoutParams2 = staticContainer.layoutParams as RelativeLayout.LayoutParams
                layoutParams2.removeRule(RelativeLayout.ALIGN_PARENT_END)
                layoutParams2.leftMargin = position[0]
                staticContainer.layoutParams = layoutParams2
                staticContainer.requestLayout()
            }
        })

      //  removebtn.visibility = View.INVISIBLE

        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) {}
        })

        key_a = findViewById(R.id.key_a)
        key_a.tag = R.mipmap.key_a
        key_b = findViewById(R.id.key_b)
        key_b.tag = R.mipmap.key_b
        key_x = findViewById(R.id.key_x)
        key_x.tag = R.mipmap.key_x
        key_y = findViewById(R.id.key_y)
        key_y.tag = R.mipmap.key_y
        key_lb = findViewById(R.id.key_lb)
        key_lb.tag = R.mipmap.key_lb
        key_lt = findViewById(R.id.key_lt)
        key_lt.tag = R.mipmap.key_lt
        key_rb = findViewById(R.id.key_rb)
        key_rb.tag = R.mipmap.key_rb
        key_rt = findViewById(R.id.key_rt)
        key_rt.tag = R.mipmap.key_rt
        key_start = findViewById(R.id.key_start)
        key_start.tag = R.mipmap.key_start
        key_select = findViewById(R.id.key_select)
        key_select.tag = R.mipmap.key_select
        key_lcenter = findViewById(R.id.key_lcenter)
        key_lcenter.tag = R.mipmap.key_lcenter
        key_rcenter = findViewById(R.id.key_rcenter)
        key_rcenter.tag = R.mipmap.key_rcenter
        key_m1 = findViewById(R.id.key_m1)
        key_m1.tag = R.mipmap.key_m1
        key_m2 = findViewById(R.id.key_m2)
        key_m2.tag = R.mipmap.key_m2
        key_m3 = findViewById(R.id.key_m3)
        key_m3.tag = R.mipmap.key_m3
        key_m4 = findViewById(R.id.key_m4)
        key_m4.tag = R.mipmap.key_m4
        key_joystick_l = findViewById(R.id.key_joystick_l)
        key_joystick_l.tag = R.mipmap.joystick_l_img
        key_joystick_r = findViewById(R.id.key_joystick_r)
        key_joystick_r.tag = R.mipmap.joystick_r_img
        key_dpad = findViewById(R.id.key_dpad)
        key_dpad.tag = R.mipmap.key_dpad
        key_action_button =findViewById(R.id.key_action_button)
        key_action_button.tag = R.mipmap.key_action_button
        key_circular_button =findViewById(R.id.key_circular_button)
        key_circular_button.tag = R.mipmap.key_circular_button

        iconList.add(key_a)
        iconList.add(key_b)
        iconList.add(key_x)
        iconList.add(key_y)
        iconList.add(key_lb)
        iconList.add(key_lt)
        iconList.add(key_rb)
        iconList.add(key_rt)
        iconList.add(key_start)
        iconList.add(key_select)
        iconList.add(key_lcenter)
        iconList.add(key_rcenter)
        iconList.add(key_m1)
        iconList.add(key_m2)
        iconList.add(key_m3)
        iconList.add(key_m4)
        iconList.add(key_joystick_l)
        iconList.add(key_joystick_r)
        iconList.add(key_dpad)
        iconList.add(key_action_button)
        iconList.add(key_circular_button)


        mainContent.setOnTouchListener(this)
        // add a long click listener to the buttons
        key_a.setOnLongClickListener { dragButton(key_a) }
        key_b.setOnLongClickListener { dragButton(key_b) }
        key_x.setOnLongClickListener { dragButton(key_x) }
        key_y.setOnLongClickListener { dragButton(key_y) }
        key_lb.setOnLongClickListener { dragButton(key_lb) }
        key_lt.setOnLongClickListener { dragButton(key_lt) }
        key_rb.setOnLongClickListener { dragButton(key_rb) }
        key_rt.setOnLongClickListener { dragButton(key_rt) }
        key_start.setOnLongClickListener { dragButton(key_start) }
        key_select.setOnLongClickListener { dragButton(key_select) }
        key_lcenter.setOnLongClickListener { dragButton(key_lcenter) }
        key_rcenter.setOnLongClickListener { dragButton(key_rcenter) }
        key_m1.setOnLongClickListener { dragButton(key_m1) }
        key_m2.setOnLongClickListener { dragButton(key_m2) }
        key_m3.setOnLongClickListener { dragButton(key_m3) }
        key_m4.setOnLongClickListener { dragButton(key_m4) }
        key_joystick_l.setOnLongClickListener { dragButton(key_joystick_l) }
        key_joystick_r.setOnLongClickListener { dragButton(key_joystick_r) }
        key_dpad.setOnLongClickListener { dragButton(key_dpad) }
        key_action_button.setOnLongClickListener { dragButton(key_action_button) }
        key_circular_button.setOnLongClickListener { dragButton(key_circular_button) }

        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                detector.let {
                    if (::lastClickedButton.isInitialized && !iconList.contains(lastClickedButton)) {
                        scaleFactor *= it.scaleFactor
                        scaleFactor = max(0.5f, min(scaleFactor, 3f)) // Restrict scale factor between 1 and 5
                        lastClickedButton.scaleX = scaleFactor
                        lastClickedButton.scaleY = scaleFactor
                    }
                }
                return true
            }
        })

       /* button1.setOnTouchListener { v, event ->
            val scaleGestureDetector = ScaleGestureDetector(v.context, object : ScaleGestureDetector.OnScaleGestureListener {
                var scaleFactor = 1.0f
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    scaleFactor *= detector.scaleFactor
                    lastClickedButton.scaleX = scaleFactor
                    lastClickedButton.scaleY = scaleFactor
                    return true
                }
                override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                    // Return true to enable scaling
                    return true
                }
                override fun onScaleEnd(detector: ScaleGestureDetector) {
                    // Do something when scaling ends, if needed
                }
            })

            // Pass the touch event and the View object to the ScaleGestureDetector
            scaleGestureDetector.onTouchEvent(event)
            true
        }*/
   /*     staticContainer.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_ENTERED -> {
                    swipeStaticButtons(event.x, event.y)
                    true
                }

                DragEvent.ACTION_DRAG_LOCATION -> {
                    swipeStaticButtons(event.x, event.y)
                    true
                }

                DragEvent.ACTION_DROP -> {
                    swipeStaticButtons(event.x, event.y)
                    true
                }

                else -> {
                    // Ignore other events
                    true
                }
            }

        }*/

        mainContent.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_ENTERED -> {
                    swapStaticButtons(v)
                   /* if (isButtonOverXIcon(event.x, event.y)) {
                        removebtn.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                    }*/
                    true
                }
                DragEvent.ACTION_DRAG_LOCATION -> {
                    swapStaticButtons(v)
                    /*if (isButtonOverXIcon(event.x, event.y)) {
                        removebtn.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                    } else {
                        removebtn.clearColorFilter()
                    }*/
                    true
                }
                DragEvent.ACTION_DROP -> {
                    val button = event.localState as ImageButton
                    val parent = button.parent as ViewGroup

                    val index=iconList.indexOf(button)
                    if (index != -1) {
                        iconList.removeAt(index)
                        if (iconList.size != index) {
                            val layoutParams1 =
                                iconList[index].layoutParams as RelativeLayout.LayoutParams
                            if (index != 0) {
                                layoutParams1.addRule(RelativeLayout.BELOW, iconList[index - 1].id)
                            } else {
                                layoutParams1.removeRule(RelativeLayout.BELOW)
                            }
                            iconList[index].layoutParams = layoutParams1
                        }
                    }
                    parent.removeView(button)


                    val layoutParams = button.layoutParams as RelativeLayout.LayoutParams
                    layoutParams.leftMargin = event.x.toInt() - button.width / 2
                    layoutParams.topMargin = event.y.toInt() - button.height / 2
                    layoutParams.removeRule(RelativeLayout.BELOW)
                    layoutParams.removeRule(RelativeLayout.CENTER_HORIZONTAL)
                    button.layoutParams = layoutParams
                    mainContent.addView(button)
                    button.setOnClickListener { addButtonFilter(button) }
                    // buttonList.add(Pair(buttonId, Pair(event.x, event.y)))

                    swapStaticButtons(v)



                 //   removebtn.visibility = View.INVISIBLE
                  //  removebtn.clearColorFilter()

                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                  //  removebtn.visibility = View.INVISIBLE
                  //  removebtn.clearColorFilter()
                    true
                }
                else -> {
                    // Ignore other events
                    true
                }
            }
        }

        loadButtonList(this)
        onLoadLayout()

        savebtn.setOnClickListener { onSaveLayout() }

        removebtn.setOnClickListener{ onRemoveButton() }

    }

    private fun swapStaticButtons(v: View){
        if (areViewsOverlapping(v)) {
            Log.d("ontop","ontop")
            //Toast.makeText(this, "ontop", Toast.LENGTH_SHORT).show()
            val staticLayout = staticContainer.layoutParams as RelativeLayout.LayoutParams
           /* val staticParamsRemove = removebtn.layoutParams as RelativeLayout.LayoutParams
            val staticParamsSave = savebtn.layoutParams as RelativeLayout.LayoutParams

            if(x > (resources.displayMetrics.widthPixels / 2)) {
            //    Toast.makeText(this, "ontop", Toast.LENGTH_SHORT).show()
                staticParamsRemove.removeRule(RelativeLayout.ALIGN_PARENT_END)
                staticParamsRemove.addRule(RelativeLayout.ALIGN_PARENT_START)
                staticParamsSave.removeRule(RelativeLayout.ALIGN_PARENT_END)
                staticParamsSave.addRule(RelativeLayout.ALIGN_PARENT_START)
            }
            else {
            //    Toast.makeText(this, "ontop", Toast.LENGTH_SHORT).show()
                staticParamsRemove.removeRule(RelativeLayout.ALIGN_PARENT_START)
                staticParamsRemove.addRule(RelativeLayout.ALIGN_PARENT_END)
                staticParamsSave.removeRule(RelativeLayout.ALIGN_PARENT_START)
                staticParamsSave.addRule(RelativeLayout.ALIGN_PARENT_END)
            }



            removebtn.layoutParams = staticParamsRemove
            savebtn.layoutParams = staticParamsSave*/


            if(v.x > (resources.displayMetrics.widthPixels / 2)) {
                //    Toast.makeText(this, "ontop", Toast.LENGTH_SHORT).show()
                staticLayout.leftMargin = 0
                staticLayout.addRule(RelativeLayout.ALIGN_PARENT_START)
            }
            else {
                //    Toast.makeText(this, "ontop", Toast.LENGTH_SHORT).show()
                staticLayout.removeRule(RelativeLayout.ALIGN_PARENT_START)
                staticLayout.leftMargin = position[0]
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onRemoveButton(): Boolean {
        try {
            if(lastClickedButton.drawable?.colorFilter?.equals(PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)) == true) {

                val layoutParams = RelativeLayout.LayoutParams(
                    lastClickedButton.width,
                    lastClickedButton.height
                )
                if (drawer.childCount > 0) {
                    layoutParams.addRule(
                        RelativeLayout.BELOW,
                        drawer.getChildAt(drawer.childCount - 1).id
                    )
                }
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
                lastClickedButton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                lastClickedButton.scaleX = 1.0f
                lastClickedButton.scaleY = 1.0f
                lastClickedButton.isClickable = false
                lastClickedButton.isFocusable = false
                lastClickedButton.layoutParams = layoutParams
                mainContent.removeView(lastClickedButton)
                drawer.addView(lastClickedButton)
                lastClickedButton.setOnClickListener(null)
                iconList.add(lastClickedButton)
            }
        }catch (_: Exception){}
        return true
    }

    private fun addButtonFilter(button: ImageButton) {
        lastClickedButton = button
        removeButtonFilter()
        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
    }

    private fun removeButtonFilter() {
     //   Toast.makeText(this, "remove", Toast.LENGTH_SHORT).show()
        for (i in 0 until mainContent.childCount) {
            val child = mainContent.getChildAt(i)
            if (child is ImageButton) {
                child.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            }
        }
    }

    private fun onSaveLayout(): Boolean {
        buttonList.clear()
        for (i in 0 until mainContent.childCount) {
            val child = mainContent.getChildAt(i)
            if (child is ImageButton) {
                val buttonId = child.id
                val buttonSrc = child.tag as? Int ?: 0
                val buttonWidth = child.width
                val buttonHeight = child.height
                val buttonScaleX = child.scaleX
                val buttonScaleY = child.scaleY
                val buttonX = child.x.toInt()
                val buttonY = child.y.toInt()
                buttonList.add(Pair(Pair(Pair(buttonId,buttonSrc),Pair(buttonWidth,buttonHeight)),Pair(Pair(buttonScaleX,buttonScaleY),Pair(buttonX,buttonY))))
            }
        }
        saveButtonList(this)
        return true
    }

    private fun saveButtonList(context: Context) {
        val prefs = context.getSharedPreferences(profile, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
        editor.putInt("button_count", buttonList.size)
        for (i in 0 until buttonList.size) {
            editor.putInt("button_${i}_id", buttonList[i].first.first.first)
            editor.putInt("button_${i}_src", buttonList[i].first.first.second)
            editor.putInt("button_${i}_width", buttonList[i].first.second.first)
            editor.putInt("button_${i}_height", buttonList[i].first.second.second)
            editor.putFloat("button_${i}_scaleX", buttonList[i].second.first.first)
            editor.putFloat("button_${i}_scaleY", buttonList[i].second.first.second)
            editor.putInt("button_${i}_x", buttonList[i].second.second.first)
            editor.putInt("button_${i}_y", buttonList[i].second.second.second)
        }
        editor.apply()
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun onLoadLayout(): Boolean {
        for (i in mainContent.childCount - 1 downTo 0) {
            val childView = mainContent.getChildAt(i)
            if (childView is ImageButton) {
                mainContent.removeView(childView)
            }
        }

        for (buttonInfo in buttonList) {
            val button = ImageButton(this)
            button.id = buttonInfo.first.first.first
            val index = iconList.indexOf(findViewById(button.id))
            iconList.removeAt(index)
            if (iconList.size != index) {
                val layoutParams = iconList[index].layoutParams as RelativeLayout.LayoutParams
                if (index != 0) {
                    layoutParams.addRule(RelativeLayout.BELOW, iconList[index - 1].id)
                } else {
                    layoutParams.removeRule(RelativeLayout.BELOW)
                }
                iconList[index].layoutParams = layoutParams
            }
            drawer.removeView(findViewById(button.id))
            button.setImageResource(buttonInfo.first.first.second)
            button.tag=buttonInfo.first.first.second
            button.scaleX = buttonInfo.second.first.first
            button.scaleY = buttonInfo.second.first.second
            button.background = null
            button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            val layoutParams = RelativeLayout.LayoutParams(buttonInfo.first.second.first,buttonInfo.first.second.second)
            layoutParams.leftMargin=buttonInfo.second.second.first
            layoutParams.topMargin=buttonInfo.second.second.second
            button.layoutParams = layoutParams
            mainContent.addView(button)
            button.setOnLongClickListener { dragButton(button) }
            button.setOnClickListener { addButtonFilter(button) }
        }
        return true
    }

    private fun loadButtonList(context: Context) {
        val prefs = context.getSharedPreferences(profile, Context.MODE_PRIVATE)
        val buttonCount = prefs.getInt("button_count", 0)
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
    }

    // function to enable dragging of a button
    private fun dragButton(button: ImageButton): Boolean {
        addButtonFilter(button)
        val item = ClipData.Item(button.tag as? String)
        val dragData = ClipData(
            button.tag as? CharSequence,
            arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
            item
        )

        val shadowBuilder = View.DragShadowBuilder(button)

        button.startDragAndDrop(
            dragData, shadowBuilder, button, 0
        )
        //removebtn.visibility = View.VISIBLE
        return true
    }

    private fun areViewsOverlapping(x: Float, y: Float, view1: View): Boolean  {

        var location = IntArray(2)
        view1.getLocationOnScreen(location)
        val rect1 = Rect(location[0],location[1],location[0]+view1.width,location[1]+view1.height)
//        Log.d("rect1","rect1=$rect1")
        staticContainer.getLocationOnScreen(location)
        val rect2 = Rect(location[0],location[1],location[0]+staticContainer.width,location[1]+staticContainer.height)
//        Log.d("rect2","rect2=$rect2")
        return rect1.intersect(rect2)

//        val location = IntArray(2)
//        staticContainer.getLocationOnScreen(location)
//       // Toast.makeText(this, "ontop"+location, Toast.LENGTH_SHORT).show()
//        val xLeft = location[0]
//        val xRight = xLeft + staticContainer.width
//        val xTop = location[1]
//        val xBottom = xTop + staticContainer.height
//
//        val touchX = x.toInt()
//        val touchY = y.toInt()
//
//        return touchX in (xLeft + 1) until xRight && touchY > xTop && touchY < xBottom
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    /*private class MyOnScaleGestureListener() : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        private var scaleFactor = 1.0f

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            // Calculate the new scale factor based on the current scale factor and the detector's scale factor
            scaleFactor *= detector?.scaleFactor ?: 1.0f

            // Set the new scale factor on the button's layout params
            val layoutParams = SetLayoutActivity().lastClickedButton.layoutParams as RelativeLayout.LayoutParams
            layoutParams.width = (SetLayoutActivity().lastClickedButton.width * scaleFactor).toInt()
            layoutParams.height = (SetLayoutActivity().lastClickedButton.height * scaleFactor).toInt()
            SetLayoutActivity().lastClickedButton.layoutParams = layoutParams

            // Return true to indicate that the event was handled
            return true
        }
    }*/

}