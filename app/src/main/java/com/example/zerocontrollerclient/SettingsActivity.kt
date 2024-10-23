package com.example.zerocontrollerclient

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView

class SettingsActivity : AppCompatActivity() {
    private lateinit var sharedPrefs: SharedPreferences

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        sharedPrefs = getSharedPreferences("selected_macros", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        val vibrateBtn = findViewById<SwitchMaterial>(R.id.vibrate)
        vibrateBtn.isChecked = sharedPrefs.getBoolean("vibrate", true)
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager: VibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibrateBtn.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("vibrate", isChecked)
            if (isChecked) {
                if (vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= 26)
                        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 0, 0, 22), -1))
                    else
                        vibrator.vibrate(22)
                }
            }
            editor.apply()
        }

        val aimTouchBtn = findViewById<SwitchMaterial>(R.id.aim_touch)
        aimTouchBtn.isChecked = sharedPrefs.getBoolean("aim_touch", false)
        aimTouchBtn.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("aim_touch", isChecked)
            editor.apply()
        }

        val sensitivity = findViewById<Slider>(R.id.sensitivity)
        val sensitivityValue = findViewById<MaterialTextView>(R.id.sensitivity_value)
        sensitivity.value = sharedPrefs.getFloat("sensitivity", 100f)
        sensitivityValue.text = sensitivity.value.toInt().toString()
        sensitivity.addOnChangeListener { _, value, _ ->
            editor.putFloat("sensitivity", value)
            sensitivityValue.text = value.toInt().toString()
            editor.apply()
        }

        val deadzone = findViewById<Slider>(R.id.deadzone)
        val deadzoneValue = findViewById<MaterialTextView>(R.id.deadzone_value)
        deadzone.value = sharedPrefs.getFloat("deadzone", 5f)
        deadzoneValue.text = deadzone.value.toInt().toString() + "%"
        deadzone.addOnChangeListener { _, value, _ ->
            editor.putFloat("deadzone", value)
            deadzoneValue.text = value.toInt().toString() + "%"
            editor.apply()
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
}