package com.example.zerocontrollerclient

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Display
import android.view.DragEvent
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import androidx.drawerlayout.widget.DrawerLayout
import kotlin.math.max
import kotlin.math.min


@Suppress("PrivatePropertyName")
class SetLayoutActivity : AppCompatActivity(), OnTouchListener {

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
    private lateinit var v_key_a: ImageButton
    private lateinit var v_key_b: ImageButton
    private lateinit var v_key_c: ImageButton
    private lateinit var v_key_d: ImageButton
    private lateinit var v_key_e: ImageButton
    private lateinit var v_key_f: ImageButton
    private lateinit var v_key_g: ImageButton
    private lateinit var v_key_h: ImageButton


    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scaleFactor = 1.0f
    private var profile = ""
    private var overlappingView = ArrayList<Pair<ImageButton, ImageButton>>()
    private var isModified = false
    private var buttonList: MutableList<Pair<Pair<Pair<Int, Int>, Pair<Int, Int>>, Pair<Pair<Float, Float>, Pair<Int, Int>>>> = mutableListOf()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.set_layout_activity)

        profile = intent.getStringExtra("profile").toString()
        drawerLayout = findViewById(R.id.drawer_layout)
        drawer = findViewById(R.id.drawer)
        drawerScrollview = findViewById(R.id.drawer_scrollview)
        mainContent = findViewById(R.id.MainContent)
        removebtn = findViewById(R.id.removeButton)
        savebtn = findViewById(R.id.saveButton)
        staticContainer = findViewById(R.id.staticButtonContainer)
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(!isModified) {
                    finish()
                }
                val handler = Handler(Looper.getMainLooper())
                AlertDialog.Builder(this@SetLayoutActivity)
                    .setTitle("Save Changes?")
                    .setMessage("Do you want to save your changes before exiting?")
                    .setPositiveButton("Save") { dialog, _ ->
                        dialog.dismiss()
                        onSaveLayout()
                        handler.postDelayed({
                            finish()
                        }, 40)

                    }
                    .setNegativeButton("Discard") { dialog, _ ->
                        dialog.dismiss()
                        handler.postDelayed({
                            finish()
                        }, 40)
                    }
                    .show()
                    .window?.setBackgroundDrawableResource(R.drawable.rounded_background)
            }
        })

        drawerLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Remove the listener to avoid multiple calls
                drawerLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                staticContainer.getLocationInWindow(position)
                val h = drawerScrollview.height
                val width: Int
                val height: Int
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val displayContext = createDisplayContext(
                        DisplayManagerCompat.getInstance(this@SetLayoutActivity).getDisplay(Display.DEFAULT_DISPLAY)!!
                    )
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
        key_joystick_l.tag = R.mipmap.key_joystick_l
        key_joystick_r = findViewById(R.id.key_joystick_r)
        key_joystick_r.tag = R.mipmap.key_joystick_r
        key_dpad = findViewById(R.id.key_dpad)
        key_dpad.tag = R.mipmap.key_dpad
        key_action_button = findViewById(R.id.key_action_button)
        key_action_button.tag = R.mipmap.key_action_button
        key_circular_button = findViewById(R.id.key_circular_button)
        key_circular_button.tag = R.mipmap.key_circular_button
        v_key_a = findViewById(R.id.v_key_a)
        v_key_a.tag = R.mipmap.v_key_a;
        v_key_b = findViewById(R.id.v_key_b)
        v_key_b.tag = R.mipmap.v_key_b;
        v_key_c = findViewById(R.id.v_key_c)
        v_key_c.tag = R.mipmap.v_key_c;
        v_key_d = findViewById(R.id.v_key_d)
        v_key_d.tag = R.mipmap.v_key_d;
        v_key_e = findViewById(R.id.v_key_e)
        v_key_e.tag = R.mipmap.v_key_e;
        v_key_f = findViewById(R.id.v_key_f)
        v_key_f.tag = R.mipmap.v_key_f;
        v_key_g = findViewById(R.id.v_key_g)
        v_key_g.tag = R.mipmap.v_key_g;
        v_key_h = findViewById(R.id.v_key_h)
        v_key_h.tag = R.mipmap.v_key_h;

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
        iconList.add(v_key_a)
        iconList.add(v_key_b)
        iconList.add(v_key_c)
        iconList.add(v_key_d)
        iconList.add(v_key_e)
        iconList.add(v_key_f)
        iconList.add(v_key_g)
        iconList.add(v_key_h)



        mainContent.setOnTouchListener(this)
        // add a long click listener to the buttons
        v_key_a.setOnLongClickListener { view -> dragButton(view, v_key_a) }
        v_key_b.setOnLongClickListener { view -> dragButton(view, v_key_b) }
        v_key_c.setOnLongClickListener { view -> dragButton(view, v_key_c) }
        v_key_d.setOnLongClickListener { view -> dragButton(view, v_key_d) }
        v_key_e.setOnLongClickListener { view -> dragButton(view, v_key_e) }
        v_key_f.setOnLongClickListener { view -> dragButton(view, v_key_f) }
        v_key_g.setOnLongClickListener { view -> dragButton(view, v_key_g) }
        v_key_h.setOnLongClickListener { view -> dragButton(view, v_key_h) }

        key_a.setOnLongClickListener { view -> dragButton(view, key_a) }
        key_b.setOnLongClickListener { view -> dragButton(view, key_b) }
        key_x.setOnLongClickListener { view -> dragButton(view, key_x) }
        key_y.setOnLongClickListener { view -> dragButton(view, key_y) }
        key_lb.setOnLongClickListener { view -> dragButton(view, key_lb) }
        key_lt.setOnLongClickListener { view -> dragButton(view, key_lt) }
        key_rb.setOnLongClickListener { view -> dragButton(view, key_rb) }
        key_rt.setOnLongClickListener { view -> dragButton(view, key_rt) }
        key_start.setOnLongClickListener { view -> dragButton(view, key_start) }
        key_select.setOnLongClickListener { view -> dragButton(view, key_select) }
        key_lcenter.setOnLongClickListener { view -> dragButton(view, key_lcenter) }
        key_rcenter.setOnLongClickListener { view -> dragButton(view, key_rcenter) }
        key_m1.setOnLongClickListener { view -> dragButton(view, key_m1) }
        key_m2.setOnLongClickListener { view -> dragButton(view, key_m2) }
        key_m3.setOnLongClickListener { view -> dragButton(view, key_m3) }
        key_m4.setOnLongClickListener { view -> dragButton(view, key_m4) }
        key_joystick_l.setOnLongClickListener { view -> dragButton(view, key_joystick_l) }
        key_joystick_r.setOnLongClickListener { view -> dragButton(view, key_joystick_r) }
        key_dpad.setOnLongClickListener { view -> dragButton(view, key_dpad) }
        key_action_button.setOnLongClickListener { view -> dragButton(view, key_action_button) }
        key_circular_button.setOnLongClickListener { view -> dragButton(view, key_circular_button) }

        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                detector.let {
                    if (::lastClickedButton.isInitialized && !iconList.contains(lastClickedButton)) {
                        scaleFactor *= it.scaleFactor
                        scaleFactor = max(0.7f, min(scaleFactor, 3.5f))
                        lastClickedButton.scaleX = scaleFactor
                        lastClickedButton.scaleY = scaleFactor
                        swapStaticButtons(lastClickedButton)
                        if (!areViewsOverlapping(lastClickedButton)) {
                            lastClickedButton.background = null
                        }
                        isModified = true
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

        mainContent.setOnDragListener { _, event ->
            val button = event.localState as ImageButton
            when (event.action) {
                DragEvent.ACTION_DRAG_ENTERED -> {
                    (button.parent as ViewGroup).removeView(button)
                    val index = iconList.indexOf(button)
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
                    true
                }
                DragEvent.ACTION_DROP -> {
                    isModified = true
                    val offset = event.clipData?.getItemAt(0)?.text?.toString()?.split(",")?.let { Point(it[0].toInt(), it[1].toInt()) }!!
                    val layoutParams = button.layoutParams as RelativeLayout.LayoutParams
                    layoutParams.leftMargin = event.x.toInt() - offset.x
                    layoutParams.topMargin = event.y.toInt() - offset.y
                    layoutParams.removeRule(RelativeLayout.BELOW)
                    layoutParams.removeRule(RelativeLayout.CENTER_HORIZONTAL)
                    button.layoutParams = layoutParams
                    mainContent.addView(button)
                    button.setOnClickListener { addButtonFilter(button) }
                    swapStaticButtons(button)
                    if (!areViewsOverlapping(button)) {
                        button.background = null
                    }


                    //   removebtn.visibility = View.INVISIBLE
                    //  removebtn.clearColorFilter()

                    true
                }

                else -> true
            }
        }

        loadButtonList(this)
        onLoadLayout()
        staticContainer.bringToFront()

        savebtn.setOnClickListener {
            onSaveLayout()
            isModified = false
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        }

        removebtn.setOnClickListener { onRemoveButton() }

    }

    private fun swapStaticButtons(button: ImageButton) {
        if (checkOverlap(button, staticContainer)) {
            val staticLayout = staticContainer.layoutParams as RelativeLayout.LayoutParams
            staticContainer.bringToFront()
            if (button.marginLeft > (resources.displayMetrics.widthPixels / 2)) {
                //    Toast.makeText(this, "ontop", Toast.LENGTH_SHORT).show()
                staticLayout.leftMargin = 0
                staticLayout.addRule(RelativeLayout.ALIGN_PARENT_START)
            } else {
                //    Toast.makeText(this, "ontop", Toast.LENGTH_SHORT).show()
                staticLayout.removeRule(RelativeLayout.ALIGN_PARENT_START)
                staticLayout.leftMargin = position[0]
            }
            staticContainer.requestLayout()
        }
    }

    private fun areViewsOverlapping(button: ImageButton): Boolean {
        var overlapping = false
        for (i in 0 until mainContent.childCount) {
            val child = mainContent.getChildAt(i)
            if (child is ImageButton && child != button) {
                if (checkOverlap(button, child)) {
                    if (!overlappingView.contains(Pair(button, child)) && !overlappingView.contains(Pair(child, button)))
                        overlappingView.add(Pair(button, child))
                    overlapping = true
                } else {
                    overlappingView.removeAll { pair ->
                        (pair.first == button && pair.second == child) || (pair.first == child && pair.second == button)
                    }
                    child.background = null
                }
            }
        }
        for (viewPair in overlappingView) {
            viewPair.first.setBackgroundColor(Color.parseColor("#40FF0000"))
            viewPair.second.setBackgroundColor(Color.parseColor("#40FF0000"))
        }
        return overlapping
    }

    private fun checkOverlap(view1: View, view2: View): Boolean {
        var viewRect1 = Rect(view1.marginLeft, view1.marginTop, view1.marginLeft + view1.width, view1.marginTop + view1.height)
        val offsetX1 = (view1.width - (view1.width * view1.scaleX).toInt()) / 2
        val offsetY1 = (view1.height - (view1.height * view1.scaleY).toInt()) / 2
        viewRect1 = Rect(viewRect1.left + offsetX1, viewRect1.top + offsetY1, viewRect1.right - offsetX1, viewRect1.bottom - offsetY1)

        var viewRect2 = Rect(view2.marginLeft, view2.marginTop, view2.marginLeft + view2.width, view2.marginTop + view2.height)
        val offsetX2 = (view2.width - (view2.width * view2.scaleX).toInt()) / 2
        val offsetY2 = (view2.height - (view2.height * view2.scaleY).toInt()) / 2
        viewRect2 = Rect(viewRect2.left + offsetX2, viewRect2.top + offsetY2, viewRect2.right - offsetX2, viewRect2.bottom - offsetY2)

        return viewRect1.intersect(viewRect2)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onRemoveButton(): Boolean {
        try {
            if (lastClickedButton.drawable?.colorFilter?.equals(PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)) == true) {

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
                lastClickedButton.background = null
                mainContent.removeView(lastClickedButton)
                drawer.addView(lastClickedButton)
                lastClickedButton.setOnClickListener(null)
                iconList.add(lastClickedButton)
                val uniqueViews = overlappingView.filter { pair ->
                    pair.first == lastClickedButton || pair.second == lastClickedButton
                }.flatMap { listOf(it.first, it.second) }.distinct()

                overlappingView.removeAll { pair ->
                    pair.first == lastClickedButton || pair.second == lastClickedButton
                }
                for (view in uniqueViews) {
                    view.background = null
                }
                for (viewPair in overlappingView) {
                    viewPair.first.setBackgroundColor(Color.parseColor("#40FF0000"))
                    viewPair.second.setBackgroundColor(Color.parseColor("#40FF0000"))
                }
            }
        } catch (_: Exception) {
        }
        return true
    }

    private fun addButtonFilter(button: ImageButton) {
        lastClickedButton = button
        removeButtonFilter()
        button.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
    }

    private fun removeButtonFilter() {
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
                buttonList.add(
                    Pair(
                        Pair(Pair(buttonId, buttonSrc), Pair(buttonWidth, buttonHeight)),
                        Pair(Pair(buttonScaleX, buttonScaleY), Pair(buttonX, buttonY))
                    )
                )
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
        editor.putInt("overlapping_view_size", overlappingView.size)
        overlappingView.forEachIndexed { index, pair ->
            editor.putInt("overlapping_view_$index.first", pair.first.id)
            editor.putInt("overlapping_view_$index.second", pair.second.id)
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
            button.tag = buttonInfo.first.first.second
            button.scaleX = buttonInfo.second.first.first
            button.scaleY = buttonInfo.second.first.second
            button.background = null
            button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            val layoutParams = RelativeLayout.LayoutParams(buttonInfo.first.second.first, buttonInfo.first.second.second)
            layoutParams.leftMargin = buttonInfo.second.second.first
            layoutParams.topMargin = buttonInfo.second.second.second
            button.layoutParams = layoutParams
            mainContent.addView(button)
            button.setOnLongClickListener { view -> dragButton(view, button) }
            button.setOnClickListener { addButtonFilter(button) }
        }
        val prefs = this.getSharedPreferences(profile, Context.MODE_PRIVATE)
        val overlappingViewSize = prefs.getInt("overlapping_view_size", 0)
        for (index in 0 until overlappingViewSize) {
            val first = findViewById<ImageButton>(prefs.getInt("overlapping_view_$index.first", 0))
            first.setBackgroundColor(Color.parseColor("#40FF0000"))
            val second = findViewById<ImageButton>(prefs.getInt("overlapping_view_$index.second", 0))
            second.setBackgroundColor(Color.parseColor("#40FF0000"))
            overlappingView.add(Pair(first, second))
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
            buttonList.add(
                Pair(
                    Pair(Pair(buttonId, buttonSrc), Pair(buttonWidth, buttonHeight)),
                    Pair(Pair(buttonScaleX, buttonScaleY), Pair(buttonX, buttonY))
                )
            )
        }
    }

    // function to enable dragging of a button
    @SuppressLint("ClickableViewAccessibility")
    private fun dragButton(view: View, button: ImageButton): Boolean {
        addButtonFilter(button)
        view.setOnTouchListener { _, event ->
            view.setOnTouchListener(null)
            val touchX = event.x.toInt()
            val touchY = event.y.toInt()
            val offsetX = ((touchX * view.scaleX) - ((view.width * view.scaleX) / 2 - view.width / 2)).toInt()
            val offsetY = ((touchY * view.scaleY) - ((view.height * view.scaleY) / 2 - view.height / 2)).toInt()

            val shadowBuilder = object : View.DragShadowBuilder(view) {
                override fun onProvideShadowMetrics(shadowSize: Point, shadowTouchPoint: Point) {
                    val scaledWidth = (view.width * view.scaleX).toInt()
                    val scaledHeight = (view.height * view.scaleY).toInt()
                    shadowSize.set(scaledWidth, scaledHeight)
                    shadowTouchPoint.set((touchX * view.scaleX).toInt(), (touchY * view.scaleY).toInt())
                }

                override fun onDrawShadow(canvas: Canvas) {
                    canvas.scale(view.scaleX, view.scaleY)
                    view.draw(canvas)
                }
            }
            val data = ClipData.newPlainText("offset", "${offsetX},${offsetY}")
            try {
                button.startDragAndDrop(data, shadowBuilder, button, 0)
            } catch (_: Exception) {
            }

            true
        }
        //removebtn.visibility = View.VISIBLE
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

}