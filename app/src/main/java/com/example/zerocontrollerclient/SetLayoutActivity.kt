package com.example.zerocontrollerclient

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.RelativeLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.drawerlayout.widget.DrawerLayout
import java.io.ByteArrayOutputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class SetLayoutActivity : AppCompatActivity(), View.OnTouchListener {

    private val MIN_SWIPE_DISTANCE = 100

    private var xDown = 0f
    private var xUp = 0f
    lateinit var drawerLayout: DrawerLayout
    lateinit var drawer: RelativeLayout
    lateinit var staticContainer: RelativeLayout

    private lateinit var button1: ImageButton
    private lateinit var button2: ImageButton
    private lateinit var mainContent: RelativeLayout
    private lateinit var removebtn: ImageButton
    private lateinit var savebtn: ImageButton
    private lateinit var loadbtn: ImageButton
    private lateinit var lastClickedButton: ImageButton

    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scaleFactor = 1.0f

    private var buttonList: MutableList<Pair<Pair<String,Int>,Pair<Pair<String, Pair<Float, Float>>, Pair<Int, Int>>>> = mutableListOf()


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.set_layout_activity)


        drawerLayout = findViewById(R.id.drawer_layout)
        drawer = findViewById(R.id.drawer)
        mainContent = findViewById(R.id.MainContent)
        removebtn = findViewById(R.id.removeButton)
        savebtn = findViewById(R.id.saveButton)
        loadbtn = findViewById(R.id.loadButton)
        staticContainer = findViewById(R.id.staticButtonContainer)

      //  removebtn.visibility = View.INVISIBLE

        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) {}
        })

        button1 = findViewById(R.id.button1)
        button2 = findViewById(R.id.button2)


        mainContent.setOnTouchListener(this)
        // add a long click listener to the buttons
        button1.setOnLongClickListener { dragButton(button1) }
        button2.setOnLongClickListener { dragButton(button2) }

        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                detector.let {
                    scaleFactor *= it.scaleFactor
                    scaleFactor = max(1f, min(scaleFactor, 5f)) // Restrict scale factor between 1 and 5
                    lastClickedButton.scaleX = scaleFactor
                    lastClickedButton.scaleY = scaleFactor
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
            when (event.action) {
                DragEvent.ACTION_DRAG_ENTERED -> {
                    swipeStaticButtons(event.x, event.y)
                   /* if (isButtonOverXIcon(event.x, event.y)) {
                        removebtn.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                    }*/
                    true
                }
                DragEvent.ACTION_DRAG_LOCATION -> {
                    swipeStaticButtons(event.x, event.y)
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

                    parent.removeView(button)


                    val layoutParams = button.layoutParams as RelativeLayout.LayoutParams
                    layoutParams.leftMargin = event.x.toInt() - button.width / 2
                    layoutParams.topMargin = event.y.toInt() - button.height / 2
                    layoutParams.removeRule(RelativeLayout.BELOW)
                    button.layoutParams = layoutParams
                    mainContent.addView(button)
                    button.setOnClickListener { addButtonFilter(button) }
                    // buttonList.add(Pair(buttonId, Pair(event.x, event.y)))

                    swipeStaticButtons(event.x, event.y)



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

        savebtn.setOnClickListener { onSaveLayout(savebtn) }

        loadbtn.setOnClickListener { onLoadLayout(loadbtn) }

        removebtn.setOnClickListener{ onRemoveButton() }

    }

    private fun swipeStaticButtons(x: Float, y: Float){
        if (isButtonOverXIcon(x, y)) {
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


            if(x > (resources.displayMetrics.widthPixels / 2)) {
                //    Toast.makeText(this, "ontop", Toast.LENGTH_SHORT).show()
                staticLayout.removeRule(RelativeLayout.ALIGN_PARENT_END)
                staticLayout.addRule(RelativeLayout.ALIGN_PARENT_START)
            }
            else {
                //    Toast.makeText(this, "ontop", Toast.LENGTH_SHORT).show()
                staticLayout.removeRule(RelativeLayout.ALIGN_PARENT_START)
                staticLayout.addRule(RelativeLayout.ALIGN_PARENT_END)
            }
        }
    }

    private fun onRemoveButton(): Boolean {
        try {
            if(lastClickedButton.drawable?.colorFilter?.equals(PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)) == true) {
                val layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                if (drawer.childCount > 0) {
                    layoutParams.addRule(
                        RelativeLayout.BELOW,
                        drawer.getChildAt(drawer.childCount - 1).id
                    )
                } else {
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                }
                lastClickedButton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                lastClickedButton.scaleX = 1f
                lastClickedButton.scaleY = 1f
                lastClickedButton.layoutParams = layoutParams
                mainContent.removeView(lastClickedButton)
                drawer.addView(lastClickedButton)
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

    private fun onSaveLayout(button: ImageButton): Boolean {
        buttonList.clear();
        for (i in 0 until mainContent.childCount) {
            val child = mainContent.getChildAt(i)
            if (child is ImageButton) {
                val id = child.id
                val outputStream = ByteArrayOutputStream()
                child.drawable.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                val bytes = outputStream.toByteArray()
                val encodedString = Base64.encodeToString(bytes, Base64.DEFAULT)
                val tag = child.tag ?: ""
                val scaleX = child.scaleX
                val scaleY = child.scaleY
                /*val imageView1 = findViewById<ImageView>(id)
                val drawableId = resources.getIdentifier(
                    imageView1.tag.toString(), "drawable", packageName
                )*/
/*                val layRules = child.layoutParams as RelativeLayout.LayoutParams
                val rules = intArrayOf()
                for (i in 0..20) {
                    val rule = layRules.getRule(i)
                    if (rule != 0) {
                        rules.plus(rule)
                    }
                }*/
                val x = child.x.toInt()
                val y = child.y.toInt()
                val buttonInfo = Pair(Pair(encodedString ,id),Pair(Pair(tag.toString(), Pair(scaleX, scaleY)), Pair(x, y)))
                buttonList.add(buttonInfo)
            }
        }

        saveButtonList(this, buttonList)


        return true
    }

    private fun saveButtonList(context: Context, buttonList: MutableList<Pair<Pair<String, Int>, Pair<Pair<String, Pair<Float, Float>>, Pair<Int, Int>>>>) {
        val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt("button_count", buttonList.size)
        for (i in 0 until buttonList.size) {
            editor.putString("button_${i}_src", buttonList[i].first.first)
            editor.putInt("button_${i}_id", buttonList[i].first.second)
            editor.putString("button_${i}_tag", buttonList[i].second.first.first)
            editor.putFloat("button_${i}_scalex", buttonList[i].second.first.second.first)
            editor.putFloat("button_${i}_scaley", buttonList[i].second.first.second.second)
            editor.putInt("button_${i}_x", buttonList[i].second.second.first)
            editor.putInt("button_${i}_y", buttonList[i].second.second.second)
        }
        editor.apply()
    }


    private fun onLoadLayout(button: ImageButton): Boolean {
        val viewToKeep: View = findViewById(R.id.staticButtonContainer)
        for (i in mainContent.childCount - 1 downTo 0) {
            val childView = mainContent.getChildAt(i)
            if (childView != viewToKeep) {
                mainContent.removeView(childView)
            }
        }

        for (buttonInfo in buttonList) {
            val button = ImageButton(this)
            val bytes = Base64.decode(buttonInfo.first.first, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            val drawable = BitmapDrawable(resources, bitmap)
            button.setImageDrawable(drawable)
            button.id = buttonInfo.first.second
            button.tag = buttonInfo.second.first.first;
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

            mainContent.addView(button, layoutParams)
            button.setOnLongClickListener { dragButton(button) }
            button.setOnClickListener { addButtonFilter(button) }

            /*if (buttonInfo.second.first.isNotEmpty()) {
                for (rule in buttonInfo.second.first) {
                    Log.e("sizeError", "size" + buttonInfo.second.first.size)
                    layoutParams.addRule(rule)
                }
            }*/

           /* if (button.tag == "staticSave") {
                button.setOnClickListener { onSaveLayout(button) }
                savebtn = button
            }
            else if (button.tag == "staticLoad") {
                button.setOnClickListener { onLoadLayout(button) }
                loadbtn = button
            }
            else if (button.tag == "staticRemove")
            {
                button.setOnClickListener{ onRemoveButton() }
                removebtn = button

            }*/
           /* else {
                mainContent.addView(button, layoutParams)
                button.setOnLongClickListener { dragButton(button) }
                button.setOnClickListener { addButtonFilter(button) }
            }

            */
        }
        return true
    }

    // function to enable dragging of a button
    private fun dragButton(button: ImageButton): Boolean {
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> xDown = event.x
            MotionEvent.ACTION_UP -> {
                xUp = event.x
                val deltaX = xUp - xDown
                if (abs(deltaX) > MIN_SWIPE_DISTANCE) {
                    if (deltaX < 0) {
                        // Swipe from right to left, unimplemented yet.
                    } else {
                        // Swipe from left to right
                        drawerLayout.openDrawer(drawer)
                    }
                }
            }
        }
        return true
    }

    private fun isButtonOverXIcon(x: Float, y: Float): Boolean {
        val location = IntArray(2)
        staticContainer.getLocationOnScreen(location)
       // Toast.makeText(this, "ontop"+location, Toast.LENGTH_SHORT).show()
        val xLeft = location[0]
        val xRight = xLeft + staticContainer.width
        val xTop = location[1]
        val xBottom = xTop + staticContainer.height

        val touchX = x.toInt()
        val touchY = y.toInt()

        return touchX in (xLeft + 1) until xRight && touchY > xTop && touchY < xBottom
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

    override fun onTouch(p0: View, p1: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(p1)
        return true;
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