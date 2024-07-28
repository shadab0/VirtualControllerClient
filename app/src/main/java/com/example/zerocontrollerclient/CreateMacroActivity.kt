package com.example.zerocontrollerclient

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.DragEvent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Collections

class CreateMacroActivity : AppCompatActivity() {

    private var macro_name = ""
    private var recorded_macro = ""
    private val buttonList = ArrayList<List<Any>>()
    private val macroByteArrayStream = ByteArrayOutputStream()

    @SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_macro_activity)
        macro_name = intent.getStringExtra("macro").toString()
        recorded_macro = intent.getStringExtra("recorded_macro").toString()

        val joystickl = findViewById<JoystickView>(R.id.key_joystick_l)
        val joystickr = findViewById<JoystickView>(R.id.key_joystick_r)
        joystickr.left = false
        val macro_list = findViewById<RecyclerView>(R.id.macro_list)
        val adapter = MacroListAdapter(this, buttonList)

        findViewById<Button>(R.id.record).setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            val inflater = LayoutInflater.from(this)
            val dialogView = inflater.inflate(R.layout.profile_dialog, null)
            dialogBuilder.setView(dialogView)

            val dialog = dialogBuilder.create()
            dialog.show()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val profile = dialog.findViewById<GridLayout>(R.id.profiles)
            dialog.findViewById<Button>(R.id.add_profile).visibility = View.GONE
            val prefsDir = File(applicationContext.dataDir, "shared_prefs")
            val profileFiles = prefsDir.listFiles()
            val profileNames = profileFiles?.map { file -> file.nameWithoutExtension } ?: emptyList()
            for (name in profileNames) {
                if (name != "selected_macros") {
                    val button = MaterialButton(this)
                    button.layoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = GridLayout.LayoutParams.WRAP_CONTENT
                        columnSpec = GridLayout.spec(0, 1, 1f) // Span 1 column with weight
                        setGravity(Gravity.FILL_HORIZONTAL or Gravity.CENTER_VERTICAL)
                    }
                    button.text = name
                    button.isAllCaps = false
                    button.setOnClickListener {
                        dialog.dismiss()
                        startActivity(
                            Intent(this, ControllerPlayActivity::class.java).putExtra("profile", name).putExtra("isMacro", true)
                                .putExtra("macro", macro_name)
                        )
                    }
                    profile.addView(button)
                }
            }
        }

        findViewById<ImageButton>(R.id.key_a).setOnLongClickListener { v ->
            val data = ClipData.newPlainText("item", "A Up|50ms|A Down")
            v.startDragAndDrop(data, View.DragShadowBuilder(v), v, 0)
            true
        }
        findViewById<ImageButton>(R.id.key_b).setOnLongClickListener { v ->
            val data = ClipData.newPlainText("item", "B Up|50ms|B Down")
            v.startDragAndDrop(data, View.DragShadowBuilder(v), v, 0)
            true
        }
        findViewById<ImageButton>(R.id.key_x).setOnLongClickListener { v ->
            val data = ClipData.newPlainText("item", "X Up|50ms|X Down")
            v.startDragAndDrop(data, View.DragShadowBuilder(v), v, 0)
            true
        }
        findViewById<ImageButton>(R.id.key_y).setOnLongClickListener { v ->
            val data = ClipData.newPlainText("item", "Y Up|50ms|Y Down")
            v.startDragAndDrop(data, View.DragShadowBuilder(v), v, 0)
            true
        }
        findViewById<ImageButton>(R.id.key_lb).setOnLongClickListener { v ->
            val data = ClipData.newPlainText("item", "LB Up|50ms|LB Down")
            v.startDragAndDrop(data, View.DragShadowBuilder(v), v, 0)
            true
        }
        findViewById<ImageButton>(R.id.key_lt).setOnLongClickListener { v ->
            val data = ClipData.newPlainText("item", "LT Up|50ms|LT Down")
            v.startDragAndDrop(data, View.DragShadowBuilder(v), v, 0)
            true
        }
        findViewById<ImageButton>(R.id.key_rb).setOnLongClickListener { v ->
            val data = ClipData.newPlainText("item", "RB Up|50ms|RB Down")
            v.startDragAndDrop(data, View.DragShadowBuilder(v), v, 0)
            true
        }
        findViewById<ImageButton>(R.id.key_rt).setOnLongClickListener { v ->
            val data = ClipData.newPlainText("item", "RT Up|50ms|RT Down")
            v.startDragAndDrop(data, View.DragShadowBuilder(v), v, 0)
            true
        }
        findViewById<ImageButton>(R.id.key_start).setOnLongClickListener { v ->
            val data = ClipData.newPlainText("item", "Start Up|50ms|Start Down")
            v.startDragAndDrop(data, View.DragShadowBuilder(v), v, 0)
            true
        }
        findViewById<ImageButton>(R.id.key_select).setOnLongClickListener { v ->
            val data = ClipData.newPlainText("item", "Select Up|50ms|Select Down")
            v.startDragAndDrop(data, View.DragShadowBuilder(v), v, 0)
            true
        }
        findViewById<ImageButton>(R.id.key_lcenter).setOnLongClickListener { v ->
            val data = ClipData.newPlainText("item", "Left Joystick Button Up|50ms|Left Joystick Button Down")
            v.startDragAndDrop(data, View.DragShadowBuilder(v), v, 0)
            true
        }
        findViewById<ImageButton>(R.id.key_rcenter).setOnLongClickListener { v ->
            val data = ClipData.newPlainText("item", "Right Joystick Button Up|50ms|Right Joystick Button Down")
            v.startDragAndDrop(data, View.DragShadowBuilder(v), v, 0)
            true
        }
        joystickl.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
//                    val joysticklInput = (v as JoystickView).joystickInput.toString().trim()
//                    addItem(R.mipmap.key_joystick_l, "Left Joystick Up",joysticklInput,false)
                }
            }
            v.onTouchEvent(event)
//            val joysticklInput = (v as JoystickView).joystickInput
//            if (joysticklInput.isNotEmpty()) {
//                when (event.actionMasked) {
//                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
//                        macro_data.append("${delayTime()}|L $joysticklInput,")
//                        v.joystickInput.clear()
//                    }
//                    MotionEvent.ACTION_MOVE -> {
//                        macro_data.append("${delayTime()},L $joysticklInput,")
//                        v.joystickInput.clear()
//                    }
//                }
//            }
//            if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_POINTER_UP) {
//                macro_data.append("${delayTime()}|L 0 0|")
//            }
            true
        }
        joystickr.setOnTouchListener { v, event ->
            v.onTouchEvent(event)
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_POINTER_DOWN -> {
//                    addItem(R.mipmap.key_joystick_r, "Right Joystick Down")
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
//                    addItem(R.mipmap.key_joystick_r, "Right Joystick Up")
                }
            }
            true
        }
        findViewById<ImageButton>(R.id.key_delay).setOnLongClickListener { v ->
            val data = ClipData.newPlainText("item", "50ms")
            v.startDragAndDrop(data, View.DragShadowBuilder(v), v, 0)
            true
        }



        findViewById<Button>(R.id.save).setOnClickListener {
            var string = ""
            macroByteArrayStream.reset()
            for (item in buttonList) {
                string += item[2].toString() + "|"
                macroByteArray(item[2].toString())
            }
            var file = File(File(applicationContext.dataDir, "macros"), "$macro_name.txt")
            file.writeText(string)
            file = File(applicationContext.dataDir, "macros/$macro_name.bin")
            file.writeBytes(macroByteArrayStream.toByteArray())
            Toast.makeText(this, "Please reconnect the controller to use the updated macros", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.clear).setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Clear")
            builder.setMessage("Are you sure you want to clear?")
            builder.setPositiveButton("Yes") { _, _ ->
                buttonList.clear()
                adapter.notifyDataSetChanged()
                var file = File(File(applicationContext.dataDir, "macros"), "$macro_name.txt")
                file.writeText("")
                file = File(applicationContext.dataDir, "macros/$macro_name.bin")
                file.writeBytes(ByteArray(0))
            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_background)
        }
        val string = File(File(applicationContext.dataDir, "macros"), "$macro_name.txt").readText()
        macro_list.layoutManager = LinearLayoutManager(this)

        onLoad(string)
        if (recorded_macro != "null")
            onLoad(recorded_macro)

        macro_list.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                source: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val sourcePosition = source.adapterPosition
                val targetPosition = target.adapterPosition

                Collections.swap(buttonList, sourcePosition, targetPosition)
                adapter.notifyItemMoved(sourcePosition, targetPosition)

                return true

            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                TODO("Not yet implemented")
            }

        })
        macro_list.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val x = event.x
                    val y = event.y
                    val view = macro_list.findChildViewUnder(x, y)
                    var position = view?.let { macro_list.getChildAdapterPosition(it) }
                    val item = event.clipData?.getItemAt(0)?.text?.toString()
                    if (position == null)
                        position = buttonList.size
                    onLoad(item!!, position)
                    adapter.notifyItemRangeInserted(position, item.split("|").size)
                    true
                }
                else -> true
            }
        }
        itemTouchHelper.attachToRecyclerView(macro_list)

    }

    private fun Gamepad(wbutton: Int = 0, LT: UByte = 0u, RT: UByte = 0u, Lx: Short = 0, Ly: Short = 0, Rx: Short = 0, Ry: Short = 0, isPressed: Byte = 0, isDpad: Byte = 0, isJoystick: Byte = 0, delay: Int = 0) {
        val data = ByteBuffer.allocate(19).order(ByteOrder.LITTLE_ENDIAN).apply {
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
            putInt(delay)
        }.array()
        macroByteArrayStream.write(data)
    }

    private fun macroByteArray(item: String) {
        if (item.contains("L ")) {
            val itemList = item.replace("L ", "").split(",")
            for (i in itemList) {
                if (i.contains("ms")) {
                    Gamepad(delay = i.removeSuffix("ms").toInt())
                } else {
                    val joystick = i.split(" ")
                    Gamepad(Lx = joystick[0].toShort(), Ly = joystick[1].toShort(), isPressed = 0x01, isJoystick = 0x01)
                }
            }
        }
        else if (item.contains("R ")) {
            val itemList = item.replace("R ", "").split(",")
            for (i in itemList) {
                if (i.contains("ms")) {
                    Gamepad(delay = i.removeSuffix("ms").toInt())
                } else {
                    val joystick = i.split(" ")
                    Gamepad(Rx = joystick[0].toShort(), Ry = joystick[1].toShort(), isPressed = 0x01, isJoystick = 0x02)
                }
            }
        } else {
            when (item) {
                "A Down", "Bottom Down" -> {
                    Gamepad(wbutton = 0x1000, isPressed = 0x01)
                }

                "A Up", "Bottom Up", "Bottom Cancel" -> {
                    Gamepad(wbutton = 0x1000, isPressed = 0x00)
                }

                "B Down", "Right Down" -> {
                    Gamepad(wbutton = 0x2000, isPressed = 0x01)
                }

                "B Up", "Right Up", "Right Cancel" -> {
                    Gamepad(wbutton = 0x2000, isPressed = 0x00)
                }

                "X Down", "Left Down" -> {
                    Gamepad(wbutton = 0x4000, isPressed = 0x01)
                }

                "X Up", "Left Up", "Left Cancel" -> {
                    Gamepad(wbutton = 0x4000, isPressed = 0x00)
                }

                "Y Down", "Top Down" -> {
                    Gamepad(wbutton = 32768, isPressed = 0x01)
                }

                "Y Up", "Top Up", "Top Cancel" -> {
                    Gamepad(wbutton = 32768, isPressed = 0x00)
                }

                "Dpad Left Down" -> {
                    Gamepad(wbutton = 0x0004, isPressed = 0x01)
                }

                "Dpad Top Down" -> {
                    Gamepad(wbutton = 0x0001, isPressed = 0x01)
                }

                "Dpad Right Down" -> {
                    Gamepad(wbutton = 0x0008, isPressed = 0x01)
                }

                "Dpad Bottom Down" -> {
                    Gamepad(wbutton = 0x0002, isPressed = 0x01)
                }

                "Dpad Left Up", "Dpad Top Up", "Dpad Right Up", "Dpad Bottom Up" -> {
                    Gamepad(wbutton = 0x0000, isPressed = 0x00)
                }

                "LB Down" -> {
                    Gamepad(wbutton = 0x0100, isPressed = 0x01)
                }

                "LB Up" -> {
                    Gamepad(wbutton = 0x0100, isPressed = 0x00)
                }

                "LT Down" -> {
                    Gamepad(LT = 255u, isPressed = 0x01, isDpad = 0x02)
                }

                "LT Up" -> {
                    Gamepad(LT = 0u, isPressed = 0x01, isDpad = 0x02)
                }

                "RB Down" -> {
                    Gamepad(wbutton = 0x0200, isPressed = 0x01)
                }

                "RB Up" -> {
                    Gamepad(wbutton = 0x0200, isPressed = 0x00)
                }

                "RT Down" -> {
                    Gamepad(RT = 255u, isPressed = 0x01, isDpad = 0x03)
                }

                "RT Up" -> {
                    Gamepad(RT = 0u, isPressed = 0x01, isDpad = 0x03)
                }

                "Start Down" -> {
                    Gamepad(wbutton = 0x0010, isPressed = 0x01)
                }

                "Start Up" -> {
                    Gamepad(wbutton = 0x0010, isPressed = 0x00)
                }

                "Select Down" -> {
                    Gamepad(wbutton = 0x0020, isPressed = 0x01)
                }

                "Select Up" -> {
                    Gamepad(wbutton = 0x0020, isPressed = 0x00)
                }

                "Left Joystick Button Down" -> {
                    Gamepad(wbutton = 0x0080, isPressed = 0x01)
                }

                "Left Joystick Button Up" -> {
                    Gamepad(wbutton = 0x0080, isPressed = 0x00)
                }

                "Right Joystick Button Down" -> {
                    Gamepad(wbutton = 0x0040, isPressed = 0x01)
                }

                "Right Joystick Button Up" -> {
                    Gamepad(wbutton = 0x0040, isPressed = 0x00)
                }

                "L0 0" -> {
                    Gamepad(isPressed = 0x01, isJoystick = 0x01)
                }

                "R0 0" -> {
                    Gamepad(isPressed = 0x01, isJoystick = 0x02)
                }

                else -> {
                    Gamepad(delay = item.removeSuffix("ms").toInt())
                }
            }
        }
    }

    private fun onLoad(string: String, pos: Int? = null) {
        var position = pos
        for (item in string.split("|")) {
            if (item == "")
                continue
            if (pos == null)
                position = buttonList.size
            if (item.contains("L ")) {
                buttonList.add(position!!, listOf(R.mipmap.key_joystick_l, "Left Joystick Down", item, false))
                continue
            }
            if (item.contains("R ")) {
                buttonList.add(position!!, listOf(R.mipmap.key_joystick_r, "Right Joystick Down", item, false))
                continue
            }
            if (item.contains("Dpad")) {
                buttonList.add(position!!, listOf(R.mipmap.key_dpad, item, item, false))
                continue
            }
            when (item) {
                "A Down", "A Up" -> {
                    buttonList.add(position!!, listOf(R.mipmap.key_a, item, item, false))
                }

                "B Down", "B Up" -> {
                    buttonList.add(position!!, listOf(R.mipmap.key_b, item, item, false))
                }

                "X Down", "X Up" -> {
                    buttonList.add(position!!, listOf(R.mipmap.key_x, item, item, false))
                }

                "Y Down", "Y Up" -> {
                    buttonList.add(position!!, listOf(R.mipmap.key_y, item, item, false))
                }

                "LB Down", "LB Up" -> {
                    buttonList.add(position!!, listOf(R.mipmap.key_lb, item, item, false))
                }

                "LT Down", "LT Up" -> {
                    buttonList.add(position!!, listOf(R.mipmap.key_lt, item, item, false))
                }

                "RB Down", "RB Up" -> {
                    buttonList.add(position!!, listOf(R.mipmap.key_rb, item, item, false))
                }

                "RT Down", "RT Up" -> {
                    buttonList.add(position!!, listOf(R.mipmap.key_rt, item, item, false))
                }

                "Start Down", "Start Up" -> {
                    buttonList.add(position!!, listOf(R.mipmap.key_start, item, item, false))
                }

                "Select Down", "Select Up" -> {
                    buttonList.add(position!!, listOf(R.mipmap.key_select, item, item, false))
                }

                "Left Joystick Button Down", "Left Joystick Button Up" -> {
                    buttonList.add(position!!, listOf(R.mipmap.key_lcenter, item, item, false))
                }

                "Right Joystick Button Down", "Right Joystick Button Up" -> {
                    buttonList.add(position!!, listOf(R.mipmap.key_rcenter, item, item, false))
                }

                "L0 0" -> {
                    buttonList.add(position!!, listOf(R.mipmap.key_joystick_l, "Left Joystick Up", item, false))
                }

                "R0 0" -> {
                    buttonList.add(position!!, listOf(R.mipmap.key_joystick_r, "Right Joystick Up", item, false))
                }

                "Left Down", "Left Up", "Left Cancel", "Top Down", "Top Up", "Top Cancel", "Right Down", "Right Up", "Right Cancel", "Bottom Down", "Bottom Up", "Bottom Cancel" -> {
                    buttonList.add(position!!, listOf(R.mipmap.key_circular_button, item, item, false))
                }

                else -> {
                    buttonList.add(position!!, listOf(R.mipmap.delay, item, item, true))
                }
            }
        }
    }

//    private fun addItem(id: Int, text: String, data: String="", isdelay: Boolean=false){
//        val img = ImageView(this)
//        img.layoutParams = GridLayout.LayoutParams().apply {
//            width = 100
//            height = 100
//            leftMargin=30
//            rightMargin=50
//            columnSpec = GridLayout.spec(0)
//            setGravity(Gravity.CENTER)
//        }
//        img.setImageResource(id)
//        if (id != R.mipmap.key_circular_button) {
//            img.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
//        }
//        macro_list.addView(img)
//
//        val textview = TextView(this)
//        textview.layoutParams = GridLayout.LayoutParams().apply {
//            width = 0
//            height = GridLayout.LayoutParams.WRAP_CONTENT
//            columnSpec = GridLayout.spec(1, 1, 1f) // Span 1 column with weight
//            setGravity(Gravity.FILL_HORIZONTAL or Gravity.CENTER_VERTICAL)
//        }
//        textview.text=text
//        textview.setTextColor(Color.WHITE)
//        textview.textSize=20f
//        textview.tag=Pair(data,null)
//        if (text.contains("Cancel")) {
//            textview.setTextColor(Color.RED)
//            textview.tag=Pair(data.replace("Cancel","Up"),null)
//        }
//        if(isdelay) {
//            textview.setOnClickListener {
//                val dialogBuilder = AlertDialog.Builder(this)
//                val editText = EditText(this)
//                editText.layoutParams = LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//                )
//                editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
//                editText.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
//                dialogBuilder.setView(editText)
//
//                dialogBuilder.setPositiveButton("OK") { _, _ ->
//                    val sec = editText.text.toString().trim()
//                    textview.text = "${sec}ms"
//                }
//
//                dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
//                    dialog.dismiss()
//                }
//
//                val dialog1 = dialogBuilder.create()
//                dialog1.show()
//                dialog1.window?.setBackgroundDrawableResource(com.google.android.material.R.color.material_dynamic_neutral10)
//            }
//        }
//        macro_list.addView(textview)
//
//        val del = ImageButton(this)
//        del.layoutParams = GridLayout.LayoutParams().apply {
//            width = GridLayout.LayoutParams.WRAP_CONTENT
//            height = GridLayout.LayoutParams.WRAP_CONTENT
//            columnSpec = GridLayout.spec(2) // Occupies the second column
//            setGravity(Gravity.CENTER)
//        }
//        del.setImageResource(R.mipmap.del)
//        del.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
//        del.background = null
//        del.setOnClickListener { view ->
//            val parent = view.parent as GridLayout
//            val row = parent.indexOfChild(view) - 2
//            parent.removeViewAt(row)
//            parent.removeViewAt(row)
//            parent.removeViewAt(row)
//        }
//        macro_list.addView(del)
////        val scrollView = findViewById<ScrollView>(R.id.scrollView)
////        scrollView.post {
////            scrollView.fullScroll(View.FOCUS_DOWN)
////        }
//    }

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