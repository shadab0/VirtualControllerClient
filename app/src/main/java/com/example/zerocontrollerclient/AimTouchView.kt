package com.example.zerocontrollerclient

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class AimTouchView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var aimTouchX: Int = 0
    private var aimTouchY: Int = 0
    private var actionId: Int? = null
    private var previousPoint: PointF? = null
    private var currentPoint: PointF? = null
    private val sensitivity = context.getSharedPreferences("selected_macros", Context.MODE_PRIVATE).getFloat("sensitivity", 100f)
    private val deadzone = (327.67 * context.getSharedPreferences("selected_macros", Context.MODE_PRIVATE).getFloat("deadzone", 5f)).toInt()
//    private var isRunning = false
    private var isMoving = false

    interface AimTouchListener {
        fun onAimTouchMoveMacro(aimTouchX: Int, aimTouchY: Int)
        fun onAimTouchUpMacro()
    }

    private var aimTouchListener: AimTouchListener? = null

    fun setAimTouchListener(listener: AimTouchListener) {
        aimTouchListener = listener
    }

//    private val loggingThread = Thread {
//        while (isRunning) {
//            try {
//                if (isMoving) {
//                    aimTouchListener?.onAimTouchMoveMacro(aimTouchX, aimTouchY)
//                    previousPoint = currentPoint
//                }
//                Thread.sleep(1000 / 24.toLong())
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    override fun onAttachedToWindow() {
//        super.onAttachedToWindow()
//        isRunning = true
//        loggingThread.start()
//    }
//
//    override fun onDetachedFromWindow() {
//        super.onDetachedFromWindow()
//        isRunning = false
//        try {
//            loggingThread.join()
//        } catch (e: InterruptedException) {
//            e.printStackTrace()
//        }
//    }

    fun smoothMovement(currentValue: Int, previousValue: Int, smoothingFactor: Float): Int {
        return ((previousValue * (1 - smoothingFactor)) + (currentValue * smoothingFactor)).toInt()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val actionIndex = event.actionIndex
        val touchX = event.getX(actionIndex)
        val touchY = event.getY(actionIndex)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if(actionId == null || actionId == event.getPointerId(actionIndex)) {
                    previousPoint = PointF(touchX, touchY)
                    actionId = event.getPointerId(actionIndex)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (actionId == event.getPointerId(actionIndex)) {
                    isMoving = true
                    currentPoint = PointF(touchX, touchY)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (actionId == event.getPointerId(actionIndex)) {
                    isMoving = false
                    previousPoint = null
                    currentPoint = null
                    aimTouchX = 0
                    aimTouchY = 0
                    actionId = null
                    aimTouchListener?.onAimTouchUpMacro()
                }
            }
        }
        if (isMoving) {
            aimTouchX = (((currentPoint!!.x - previousPoint!!.x) / width) * 32767 * sensitivity).toInt().coerceIn(-32767, 32767)
            aimTouchY = -(((currentPoint!!.y - previousPoint!!.y) / height) * 32767 * sensitivity).toInt().coerceIn(-32767, 32767)
            val distance = sqrt(aimTouchX.toDouble().pow(2.0) + aimTouchY.toDouble().pow(2.0)).toFloat()
            if (0 < distance && distance < deadzone) {
                val angle = atan2(aimTouchY.toFloat(), aimTouchX.toFloat())
                aimTouchX = (deadzone * cos(angle)).toInt()
                aimTouchY = (deadzone * sin(angle)).toInt()
            }
            aimTouchListener?.onAimTouchMoveMacro(aimTouchX, aimTouchY)
            previousPoint = currentPoint
        }
        return true
    }
}