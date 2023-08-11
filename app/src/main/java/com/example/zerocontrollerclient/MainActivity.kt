package com.example.zerocontrollerclient

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.io.File
import java.io.OutputStream
import java.net.Socket

class MainActivity : AppCompatActivity() {
    private lateinit var outputStream: OutputStream

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val macroDir = File(applicationContext.dataDir, "macros")
        if (!macroDir.exists())
            macroDir.mkdirs()

        findViewById<Button>(R.id.start).setOnClickListener{
            val dialogBuilder = AlertDialog.Builder(this)
            val inflater = LayoutInflater.from(this)
            val dialogView = inflater.inflate(R.layout.profile_dialog, null)
            dialogBuilder.setView(dialogView)

            val dialog = dialogBuilder.create()
            dialog.show()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val profile = dialog.findViewById<GridLayout>(R.id.profiles)

            val prefsDir = File(applicationContext.dataDir, "shared_prefs")
            val profileFiles = prefsDir.listFiles()
            val profileNames = profileFiles?.map { file -> file.nameWithoutExtension } ?: emptyList()
            for (name in profileNames) {
                if (name!="selected_macros")
                    addButton(name,profile,dialog,false)
            }
            dialog.findViewById<Button>(R.id.add_profile).setOnClickListener{
                val editText = EditText(this)
                editText.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                editText.imeOptions= EditorInfo.IME_FLAG_NO_EXTRACT_UI
                dialogBuilder.setView(editText)

                dialogBuilder.setPositiveButton("OK") { _, _ ->
                    val text = editText.text.toString().trim()
                    if (text.isEmpty()) {
                        Toast.makeText(this,"Profile name cannot be empty",Toast.LENGTH_SHORT).show()
                    }else if (text.length > 30) {
                        Toast.makeText(this,"Profile name cannot exceed 30 characters",Toast.LENGTH_SHORT).show()
                    }else if (Regex("[^a-zA-Z0-9._-]").containsMatchIn(text)) {
                        Toast.makeText(this,"Profile name contains invalid characters", Toast.LENGTH_SHORT).show()
                    }else if (profileNames.contains(text)) {
                        Toast.makeText(this,"Profile name already exists", Toast.LENGTH_SHORT).show()
                    }else {
                        addButton(text,profile,dialog,false)
                    }
                }

                dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog1 = dialogBuilder.create()
                dialog1.show()
                dialog1.window?.setBackgroundDrawableResource(com.google.android.material.R.color.material_dynamic_neutral10)
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
            dialog.findViewById<Button>(R.id.add_profile).visibility = View.GONE
            val prefsDir = File(applicationContext.dataDir, "shared_prefs")
            val profileFiles = prefsDir.listFiles()
            val profileNames = profileFiles?.map { file -> file.nameWithoutExtension } ?: emptyList()
            for (name in profileNames) {
                if (name!="selected_macros")
                    addButton(name,profile,dialog,false)
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
            dialog.findViewById<LinearLayout>(R.id.add_macro).visibility = View.VISIBLE
            dialog.findViewById<LinearLayout>(R.id.show_macro).visibility = View.GONE
            val prefsDir = File(applicationContext.dataDir, "macros")
            val macroFiles = prefsDir.listFiles()
            val macroNames = macroFiles?.map { file -> file.nameWithoutExtension } ?: emptyList()
            for (name in macroNames)
            {
                addButton(name,macro,dialog,true)
            }
            dialog.findViewById<Button>(R.id.new_macro).setOnClickListener {
                val editText = EditText(this)
                editText.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                editText.imeOptions=EditorInfo.IME_FLAG_NO_EXTRACT_UI
                dialogBuilder.setView(editText)

                dialogBuilder.setPositiveButton("OK") { _, _ ->
                    val text = editText.text.toString().trim()
                    if (text.isEmpty()) {
                        Toast.makeText(this,"Macro name cannot be empty",Toast.LENGTH_SHORT).show()
                    }else if (text.length > 30) {
                        Toast.makeText(this,"Macro name cannot exceed 30 characters",Toast.LENGTH_SHORT).show()
                    }else if (Regex("[^a-zA-Z0-9._-]").containsMatchIn(text)) {
                        Toast.makeText(this,"Macro name contains invalid characters", Toast.LENGTH_SHORT).show()
                    }else if (macroNames.contains(text)) {
                        Toast.makeText(this,"Macro name already exists", Toast.LENGTH_SHORT).show()
                    }else {
                        addButton(text,macro,dialog,true)
                    }
                }

                dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog1 = dialogBuilder.create()
                dialog1.show()
                dialog1.window?.setBackgroundDrawableResource(com.google.android.material.R.color.material_dynamic_neutral10)
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
            val macroFiles = prefsDir.listFiles()
            val macroNames = (listOf("None") + (macroFiles?.map { file -> file.nameWithoutExtension } ?: emptyList()))
            val sharedPrefs = getSharedPreferences("selected_macros", Context.MODE_PRIVATE)
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
                val editor = sharedPrefs.edit()
                editor.putString("M1", macroM1.text.toString())
                editor.putString("M2", macroM2.text.toString())
                editor.putString("M3", macroM3.text.toString())
                editor.putString("M4", macroM4.text.toString())
                editor.apply()
                dialog.dismiss()
            }
        }

        findViewById<Button>(R.id.connectBtn).setOnClickListener{
           val thread = Thread {
                try {
                    val serverIp = "192.168.0.101"
                    val serverPort = 8888
                    val socket = Socket(serverIp, serverPort)
                    outputStream = socket.getOutputStream()
                } catch (e: Exception) {
                    Log.e("SocketClient", "Error: ${e.message}")
                }
            }
            thread.start()
            thread.join()

            SharedObject.setOutputStream(outputStream)
            startActivity(Intent(this, ControllerPlayActivity::class.java))
        }

    }

    private fun addButton(text: String, profile: GridLayout, dialog: AlertDialog, isMacro: Boolean) {
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
                startActivity(Intent(this, CreateMacroActivity::class.java).putExtra(Intent.EXTRA_TEXT, text))
            } else {
                if (dialog.findViewById<Button>(R.id.add_profile).visibility != View.GONE) {
                    startActivity(Intent(this, SetLayoutActivity::class.java).putExtra(Intent.EXTRA_TEXT, text))
                } else {
                    startActivity(Intent(this, ControllerPlayActivity::class.java).putExtra(Intent.EXTRA_TEXT, text))
                }
            }
        }
        profile.addView(button)
        if (isMacro || dialog.findViewById<Button>(R.id.add_profile).visibility != View.GONE) {
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
            }
            profile.addView(del)
        }
    }
}