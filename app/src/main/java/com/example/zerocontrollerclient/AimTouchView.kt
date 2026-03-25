package com.example.zerocontrollerclient

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View

class AimTouchView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    interface AimTouchListener {
        fun onAimTouchMoveMacro(aimTouchX: Float, aimTouchY: Float)
    }

    private var aimTouchListener: AimTouchListener? = null

    fun setAimTouchListener(listener: AimTouchListener) {
        aimTouchListener = listener
    }

    private val MAX_JOYSTICK = 32767f
    private val SMOOTHING = 0.35f
    private var sensitivity = 1800f

    private var targetJoyX = 0f
    private var targetJoyY = 0f
    private var currentJoyX = 0f
    private var currentJoyY = 0f

    private var isActive = false
    private var lastX = 0f
    private var lastY = 0f

    private val stopHandler = Handler(Looper.getMainLooper())
    private val stopRunnable = Runnable {
        targetJoyX = 0f
        targetJoyY = 0f
    }

    private var isLooping = false
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isLooping) return

            currentJoyX += (targetJoyX - currentJoyX) * SMOOTHING
            currentJoyY += (targetJoyY - currentJoyY) * SMOOTHING

            aimTouchListener?.onAimTouchMoveMacro(currentJoyX, currentJoyY)

            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    init {
        sensitivity = context.getSharedPreferences("selected_macros", Context.MODE_PRIVATE).getFloat("sensitivity", 1800f)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isLooping = true
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isLooping = false
        stopHandler.removeCallbacksAndMessages(null)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        sensitivity = context.getSharedPreferences("selected_macros", Context.MODE_PRIVATE).getFloat("sensitivity", 1800f)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                isActive = true
                lastX = event.x
                lastY = event.y
                stopHandler.removeCallbacks(stopRunnable)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isActive) return true

                val deltaX = event.x - lastX
                val deltaY = event.y - lastY

                lastX = event.x
                lastY = event.y

                val rawX = deltaX * sensitivity
                val rawY = deltaY * sensitivity

                targetJoyX = rawX.coerceIn(-MAX_JOYSTICK, MAX_JOYSTICK)
                targetJoyY = rawY.coerceIn(-MAX_JOYSTICK, MAX_JOYSTICK)

                // Snap to 0 if movement stops for 40ms
                stopHandler.removeCallbacks(stopRunnable)
                stopHandler.postDelayed(stopRunnable, 40)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                isActive = false
                stopHandler.removeCallbacks(stopRunnable)
                targetJoyX = 0f
                targetJoyY = 0f
            }
        }
        return true
    }
}