package com.example.zerocontrollerclient

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream
import java.net.BindException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {
    private lateinit var outputStream: OutputStream
    private var discoverySocket: DatagramSocket? = null
    internal lateinit var textView: TextView
    private lateinit var sharedPrefs: SharedPreferences

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val macroDir = File(applicationContext.dataDir, "macros")
        if (!macroDir.exists())
            macroDir.mkdirs()

        textView = findViewById(R.id.textView)
        sharedPrefs = getSharedPreferences("selected_macros", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        findViewById<ImageButton>(R.id.settings).setOnClickListener{
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.set_layout).setOnClickListener{
            val dialogBuilder = AlertDialog.Builder(this)
            val inflater = LayoutInflater.from(this)
            val dialogView = inflater.inflate(R.layout.profile_dialog, null)
            dialogBuilder.setView(dialogView)

            val dialog = dialogBuilder.create()
            dialog.show()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val profile = dialog.findViewById<GridLayout>(R.id.profiles)
            val profileMsg = dialog.findViewById<RelativeLayout>(R.id.profile_msg)
            val profileContainer = dialog.findViewById<ScrollView>(R.id.profile_container)
            val prefsDir = File(applicationContext.dataDir, "shared_prefs")
            val profileFiles = prefsDir.listFiles()
            val profileNames = profileFiles?.map { file -> file.nameWithoutExtension } ?: emptyList()
            for (name in profileNames) {
                if (name!="selected_macros")
                    addButton(name,profile,dialog,profileMsg,profileContainer,false)
            }
            if (profile.childCount == 0) {
                profileMsg.visibility = View.VISIBLE
                profileContainer.visibility = View.GONE
            }
            dialog.findViewById<Button>(R.id.add_profile).setOnClickListener{
                dialogBuilder.setTitle("Enter Name:")
                val editText = EditText(this)
                val parentLayout = LinearLayout(this)
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(50,0,50,0)
                editText.layoutParams = layoutParams
                editText.setSingleLine()
                editText.imeOptions=EditorInfo.IME_FLAG_NO_EXTRACT_UI
                parentLayout.addView(editText)
                dialogBuilder.setView(parentLayout)

                dialogBuilder.setPositiveButton("OK") { _, _ ->
                    val text = editText.text.toString().trim()
                    if (text.isEmpty()) {
                        Toast.makeText(this,"Profile name cannot be empty",Toast.LENGTH_SHORT).show()
                    }else if (text.length > 30) {
                        Toast.makeText(this,"Profile name cannot exceed 30 characters",Toast.LENGTH_SHORT).show()
                    }else if (Regex("[^a-zA-Z0-9._-]").containsMatchIn(text)) {
                        Toast.makeText(this,"Profile name contains invalid characters", Toast.LENGTH_SHORT).show()
                    }else if ((prefsDir.listFiles()?.map { file -> file.nameWithoutExtension } ?: emptyList()).contains(text)) {
                        Toast.makeText(this,"Profile name already exists", Toast.LENGTH_SHORT).show()
                    }else {
                        profileMsg.visibility = View.GONE
                        profileContainer.visibility = View.VISIBLE
                        addButton(text,profile,dialog,profileMsg,profileContainer,false)
                    }
                }

                dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog1 = dialogBuilder.create()
                editText.requestFocus()
                dialog1.setOnDismissListener {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0)
                }
                dialog1.show()
                dialog1.window?.apply {
                    clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                    clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                    setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)}
                dialog1.window?.setBackgroundDrawableResource(R.drawable.rounded_background)
            }
        }

        findViewById<Button>(R.id.play).setOnClickListener{
            val dialogBuilder = AlertDialog.Builder(this)
            val inflater = LayoutInflater.from(this)
            val dialogView = inflater.inflate(R.layout.profile_dialog, null)
            dialogBuilder.setView(dialogView)

            val dialog = dialogBuilder.create()
            dialog.show()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val profile = dialog.findViewById<GridLayout>(R.id.profiles)
            val profileMsg = dialog.findViewById<RelativeLayout>(R.id.profile_msg)
            val profileContainer = dialog.findViewById<ScrollView>(R.id.profile_container)
            dialog.findViewById<Button>(R.id.add_profile).visibility = View.INVISIBLE
            val prefsDir = File(applicationContext.dataDir, "shared_prefs")
            val profileFiles = prefsDir.listFiles()
            val profileNames = profileFiles?.map { file -> file.nameWithoutExtension } ?: emptyList()
            for (name in profileNames) {
                if (name!="selected_macros")
                    addButton(name,profile,dialog,profileMsg,profileContainer,false)
            }
            if (profile.childCount == 0) {
                profileMsg.visibility = View.VISIBLE
                profileContainer.visibility = View.GONE
            }
        }

        findViewById<Button>(R.id.create_macro).setOnClickListener{
            val dialogBuilder = AlertDialog.Builder(this)
            val inflater = LayoutInflater.from(this)
            val dialogView = inflater.inflate(R.layout.macro_dialog, null)
            dialogBuilder.setView(dialogView)

            val dialog = dialogBuilder.create()
            dialog.show()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val macro = dialog.findViewById<GridLayout>(R.id.macros)
            val macroMsg = dialog.findViewById<RelativeLayout>(R.id.macro_msg)
            val macroContainer = dialog.findViewById<ScrollView>(R.id.macro_container)
            dialog.findViewById<LinearLayout>(R.id.add_macro).visibility = View.VISIBLE
            dialog.findViewById<LinearLayout>(R.id.show_macro).visibility = View.GONE
            val prefsDir = File(applicationContext.dataDir, "macros")
            val macroNames = prefsDir.listFiles()
                ?.filter { it.isFile && it.extension == "txt" }
                ?.map { it.nameWithoutExtension } ?: emptyList()
            for (name in macroNames)
                addButton(name,macro,dialog,macroMsg,macroContainer,true)
            if (macro.childCount == 0) {
                macroMsg.visibility = View.VISIBLE
                macroContainer.visibility = View.GONE
            }
            dialog.findViewById<Button>(R.id.new_macro).setOnClickListener {
                dialogBuilder.setTitle("Enter Name:")
                val editText = EditText(this)
                val parentLayout = LinearLayout(this)
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(50,0,50,0)
                editText.layoutParams = layoutParams
                editText.setSingleLine()
                editText.imeOptions=EditorInfo.IME_FLAG_NO_EXTRACT_UI
                parentLayout.addView(editText)
                dialogBuilder.setView(parentLayout)

                dialogBuilder.setPositiveButton("OK") { _, _ ->
                    val text = editText.text.toString().trim()
                    if (text.isEmpty()) {
                        Toast.makeText(this,"Macro name cannot be empty",Toast.LENGTH_SHORT).show()
                    }else if (text.length > 30) {
                        Toast.makeText(this,"Macro name cannot exceed 30 characters",Toast.LENGTH_SHORT).show()
                    }else if (Regex("[^a-zA-Z0-9._-]").containsMatchIn(text)) {
                        Toast.makeText(this,"Macro name contains invalid characters", Toast.LENGTH_SHORT).show()
                    }else if ((prefsDir.listFiles()?.map { file -> file.nameWithoutExtension } ?: emptyList()).contains(text)) {
                        Toast.makeText(this,"Macro name already exists", Toast.LENGTH_SHORT).show()
                    }else {
                        macroMsg.visibility = View.GONE
                        macroContainer.visibility = View.VISIBLE
                        addButton(text,macro,dialog,macroMsg,macroContainer,true)
                    }
                }

                dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog1 = dialogBuilder.create()
                editText.requestFocus()
                dialog1.setOnDismissListener {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0)
                }
                dialog1.show()
                dialog1.window?.apply {
                    clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                    clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                    setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)}
                dialog1.window?.setBackgroundDrawableResource(R.drawable.rounded_background)
            }
        }

        findViewById<Button>(R.id.set_macro).setOnClickListener{
            val dialogBuilder = AlertDialog.Builder(this)
            val inflater = LayoutInflater.from(this)
            val dialogView = inflater.inflate(R.layout.macro_dialog, null)
            dialogBuilder.setView(dialogView)

            val dialog = dialogBuilder.create()
            dialog.show()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.findViewById<LinearLayout>(R.id.add_macro).visibility = View.GONE
            dialog.findViewById<LinearLayout>(R.id.show_macro).visibility = View.VISIBLE
            val prefsDir = File(applicationContext.dataDir, "macros")
            val macroNames = (listOf("None") + (prefsDir.listFiles()?.filter { it.isFile && it.extension == "txt" }
                ?.map { it.nameWithoutExtension } ?: emptyList()))
            for (i in 1..4) {
                val macroValue = sharedPrefs.getString("M$i", "None")
                if (!macroNames.contains(macroValue))
                    editor.putString("M$i", "None")
            }
            editor.apply()
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, macroNames)
            val macroM1 = dialog.findViewById<AutoCompleteTextView>(R.id.macro_m1)
            macroM1.setText(sharedPrefs.getString("M1", "None"))
            macroM1.setAdapter(adapter)
            val macroM2 = dialog.findViewById<AutoCompleteTextView>(R.id.macro_m2)
            macroM2.setText(sharedPrefs.getString("M2", "None"))
            macroM2.setAdapter(adapter)
            val macroM3 = dialog.findViewById<AutoCompleteTextView>(R.id.macro_m3)
            macroM3.setText(sharedPrefs.getString("M3", "None"))
            macroM3.setAdapter(adapter)
            val macroM4 = dialog.findViewById<AutoCompleteTextView>(R.id.macro_m4)
            macroM4.setText(sharedPrefs.getString("M4", "None"))
            macroM4.setAdapter(adapter)
            dialog.findViewById<Button>(R.id.save_macro).setOnClickListener {
                editor.putString("M1", macroM1.text.toString())
                editor.putString("M2", macroM2.text.toString())
                editor.putString("M3", macroM3.text.toString())
                editor.putString("M4", macroM4.text.toString())
                editor.apply()
                dialog.dismiss()
                Toast.makeText(this, "Please reconnect the controller to use the updated macros", Toast.LENGTH_SHORT).show()
            }
        }

        val connect = findViewById<Button>(R.id.connectBtn)
        connect.setOnClickListener{
            connect.isEnabled = false
            val dialogBuilder = AlertDialog.Builder(this)
            val inflater = LayoutInflater.from(this)
            val dialogView = inflater.inflate(R.layout.connection_dialog, null)
            dialogBuilder.setView(dialogView)
            val dialog = dialogBuilder.create()
            dialog.setOnDismissListener {
                discoverySocket?.close()
                connect.isEnabled = true
            }
            dialog.show()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val socket = SharedObject.getSocket()
            val connectionMsg = dialog.findViewById<RelativeLayout>(R.id.connection_msg)
            val showConnection = dialog.findViewById<RelativeLayout>(R.id.show_connection)
            val addIpBtn = dialog.findViewById<Button>(R.id.add_ip)
            val disconnectBtn = dialog.findViewById<Button>(R.id.disconnect)
            if (socket == null) {
                discoverSocket(connect, dialog)
                connectionMsg.visibility = View.GONE
                disconnectBtn.visibility = View.GONE
                addIpBtn.visibility = View.VISIBLE
                showConnection.visibility = View.VISIBLE
            } else {
                connectionMsg.visibility = View.VISIBLE
                disconnectBtn.visibility = View.VISIBLE
                addIpBtn.visibility = View.GONE
                showConnection.visibility = View.GONE
            }
            dialog.findViewById<Button>(R.id.scan).setOnClickListener {
                discoverSocket(connect, dialog)
            }

            disconnectBtn.setOnClickListener {
                socket!!.close()
                discoverSocket(connect, dialog)
                connectionMsg.visibility = View.GONE
                disconnectBtn.visibility = View.GONE
                addIpBtn.visibility = View.VISIBLE
                showConnection.visibility = View.VISIBLE
            }

            addIpBtn.setOnClickListener{
                discoverySocket?.close()
                dialogBuilder.setTitle("Enter Details:")
                val editText = EditText(this)
                val parentLayout = LinearLayout(this)
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(50,0,50,0)
                editText.layoutParams = layoutParams
                editText.setSingleLine()
                editText.imeOptions=EditorInfo.IME_FLAG_NO_EXTRACT_UI
                editText.hint="IP:PORT - (192.168.1.10:12400)"
                parentLayout.addView(editText)
                dialogBuilder.setView(parentLayout)

                dialogBuilder.setPositiveButton("OK") { _, _ ->
                    val ip = editText.text.toString().trim().split(":")
                    val ipPattern = Pattern.compile("^((25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)$")
                    if (ip.size == 2 && ip[1].toIntOrNull() != null && ipPattern.matcher(ip[0]).matches() && ip[1].toInt() in 1..65535)
                        connectSocket(ip[0], ip[1].toInt(), dialog)
                    else
                        Toast.makeText(this, "Enter valid IP and Port", Toast.LENGTH_SHORT).show()
                }

                dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog1 = dialogBuilder.create()
                editText.requestFocus()
                dialog1.setOnDismissListener {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0)
                }
                dialog1.show()
                dialog1.window?.apply {
                    clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                    clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                    setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)}
                dialog1.window?.setBackgroundDrawableResource(R.drawable.rounded_background)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun discoverSocket(connect: Button, dialog: AlertDialog) {
        val handler = Handler(Looper.getMainLooper())
        val scan = dialog.findViewById<Button>(R.id.scan)
        scan.visibility = View.GONE
        val loading = dialog.findViewById<ImageView>(R.id.loading)
        loading.setImageResource(R.mipmap.key_x)
        loading.visibility = View.VISIBLE
        val parent = dialog.findViewById<RelativeLayout>(R.id.show_connection)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                discoverySocket = DatagramSocket(40404)
                val buffer = ByteArray(1024)
                val discoveryPacket = DatagramPacket(buffer, buffer.size)
                handler.postDelayed({
                    discoverySocket!!.close()
                }, 5000)
                discoverySocket!!.receive(discoveryPacket)
                val serverInfo = String(buffer, 0, discoveryPacket.length)
                val serverInfoParts = serverInfo.split("|")
                if (serverInfoParts.size == 3 && serverInfoParts[0] == "ServerInfo") {
                    val serverIp = serverInfoParts[1]
                    val serverPort = serverInfoParts[2].toInt()
                    withContext(Dispatchers.Main) {
                        delay(1000)
                        loading.visibility = View.GONE
                        val button = MaterialButton(this@MainActivity)
                        val layoutParams = RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT
                        )
                        button.layoutParams = layoutParams
                        button.text = "$serverIp:$serverPort"
                        parent.addView(button)
                        button.setOnClickListener {
                            connectSocket(serverIp, serverPort, dialog)
                        }
                        connect.isEnabled = true
                    }
                    handler.removeCallbacksAndMessages(null)
                    discoverySocket!!.close()
                }
            } catch (e: SocketException) {
                withContext(Dispatchers.Main) {
                    if (e is BindException)
                        Toast.makeText(this@MainActivity, "Already in use!", Toast.LENGTH_SHORT).show()
                    connect.isEnabled = true
                    handler.removeCallbacksAndMessages(null)
                    if (dialog.isShowing) {
                        loading.setImageResource(R.mipmap.key_a)
                        scan.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun connectSocket(serverIp: String, serverPort: Int, dialog: AlertDialog) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val socket = Socket(serverIp, serverPort)
                outputStream = socket.getOutputStream()
                SharedObject.setSocketAndOutputStream(socket, outputStream)
//                val m1 = File(applicationContext.dataDir, "macros/${sharedPrefs.getString("M1", "None")}.bin")
//                    .takeIf { it.exists() }?.readBytes()
//                val m2 = File(applicationContext.dataDir, "macros/${sharedPrefs.getString("M2", "None")}.bin")
//                    .takeIf { it.exists() }?.readBytes()
//                val m3 = File(applicationContext.dataDir, "macros/${sharedPrefs.getString("M3", "None")}.bin")
//                    .takeIf { it.exists() }?.readBytes()
//                val m4 = File(applicationContext.dataDir, "macros/${sharedPrefs.getString("M4", "None")}.bin")
//                    .takeIf { it.exists() }?.readBytes()
//                val size = ByteBuffer.allocate(17).order(ByteOrder.LITTLE_ENDIAN).putInt(m1?.size ?: 1).putInt(m2?.size ?: 1).putInt(m3?.size ?: 1).putInt(m4?.size ?: 1)
//                    .array()
//                outputStream.write(size).also { outputStream.flush() }
//                for (m in arrayOf(m1, m2, m3, m4)) {
//                    if (m != null)
//                        outputStream.write(m).also { outputStream.flush() }
//                    else
//                        outputStream.write(0x00).also { outputStream.flush() }
//                }
                withContext(Dispatchers.Main) {
                    startService(Intent(this@MainActivity, ConnectionMonitorService::class.java))
                    dialog.dismiss()
                    textView.text = "Connected : $serverIp"
                    Toast.makeText(this@MainActivity, "Controller Connected Successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("SocketClient", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Failed to connect: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun addButton(text: String, gridLayout: GridLayout, dialog: AlertDialog, msg: RelativeLayout, container: ScrollView, isMacro: Boolean) {
        val button = MaterialButton(this)
        button.layoutParams = GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(0, 1, 1f) // Span 1 column with weight
            setGravity(Gravity.FILL_HORIZONTAL or Gravity.CENTER_VERTICAL)
        }
        button.text = text
        button.isAllCaps = false
        if (isMacro)
            File(applicationContext.dataDir, "macros/$text.txt").createNewFile()
        else
            applicationContext.getSharedPreferences(text, Context.MODE_PRIVATE).edit().apply()
        button.setOnClickListener {
            dialog.dismiss()
            if (isMacro){
                startActivity(Intent(this, CreateMacroActivity::class.java).putExtra("macro", text))
            } else {
                if (dialog.findViewById<Button>(R.id.add_profile).visibility != View.INVISIBLE) {
                    startActivity(Intent(this, SetLayoutActivity::class.java).putExtra("profile", text))
                } else {
                    startActivity(Intent(this, ControllerPlayActivity::class.java).putExtra("profile", text))
                }
            }
        }
        gridLayout.addView(button)
        if (isMacro || dialog.findViewById<Button>(R.id.add_profile).visibility != View.INVISIBLE) {
            val del = ImageButton(this)
            del.layoutParams = GridLayout.LayoutParams().apply {
                width = GridLayout.LayoutParams.WRAP_CONTENT
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(1) // Occupies the second column
                setGravity(Gravity.CENTER)
            }
            del.setImageResource(R.mipmap.del)
            del.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            del.background = null
            del.setOnClickListener { view ->
                val parent = view.parent as GridLayout
                val row = parent.indexOfChild(view) - 1
                parent.removeViewAt(row)
                parent.removeViewAt(row)
                val fileName = if (isMacro) "macros/$text.txt" else "shared_prefs/$text.xml"
                val prefsFile = File(this.applicationContext.dataDir, fileName)
                prefsFile.delete()
                Toast.makeText(this, "Deleted $text", Toast.LENGTH_SHORT).show()
                if (gridLayout.childCount == 0) {
                    msg.visibility = View.VISIBLE
                    container.visibility = View.GONE
                }
            }
            gridLayout.addView(del)
        }
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

    override fun onResume() {
        super.onResume()
        val socket = SharedObject.getSocket()
        textView.text = socket?.run {"Connected : ${(remoteSocketAddress as? InetSocketAddress)?.address?.hostAddress ?: ""}"} ?: ""
    }

}