package com.example.zerocontrollerclient

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class AimTouchView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
//    private val CIRCLE_RADIUS = 32767
//    private var prevX = 0f
//    private var prevY = 0f
//    private var prevTime = 0L

    interface AimTouchListener {
        fun onAimTouchMoveMacro(aimTouchX: Float, aimTouchY: Float)
        //fun Print()
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

    // Assuming max radius of circle is 32767
    private val CIRCLE_RADIUS = 32767f
    private val sensitivity = 7f
    private val noMovementThreshold = context.getSharedPreferences("selected_macros", Context.MODE_PRIVATE).getFloat("sensitivity", 10f)
    private val startPercentage = context.getSharedPreferences("selected_macros", Context.MODE_PRIVATE).getFloat("deadzone", 5f)
    private var prevX = 0f
    private var prevY = 0f
    private var prevTime = 0L
    private var currentPointX = 0f
    private var currentPointY = 0f
    private val smoothingFactor = 0.1f
    private var zeroSpeedFrameCount = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val currentTime = System.currentTimeMillis()

        if (event.action == MotionEvent.ACTION_MOVE) {
            val dx = x - prevX
            val dy = y - prevY
            val timeElapsed = currentTime - prevTime
            val speed = calculateSpeed(dx, dy, timeElapsed)

            // Normalize the speed and amplify it by sensitivity factor
            val normalizedSpeed = normalizeSpeed(speed) * sensitivity

            //Log.d("TouchPoint", "speed after scaling: $normalizedSpeed")
            if (normalizedSpeed == 0f) {
                zeroSpeedFrameCount++
                if (zeroSpeedFrameCount >= 3) {
                    aimTouchListener?.onAimTouchMoveMacro(0f, 0f)
                    //Log.d("TouchPoint", "No movement detected due to low speed$normalizedSpeed")
                    prevX = x
                    prevY = y
                    prevTime = currentTime
                    return true
                }
            } else {
                zeroSpeedFrameCount = 0 // Reset counter if there is movement
            }

            val innerCircleRadius = (startPercentage / 100f) * CIRCLE_RADIUS
            val maxEffectiveDistance = (1 - (startPercentage / 100f)) * CIRCLE_RADIUS
            // Adjust effective distance with sensitivity, giving more reach to higher speeds
            val effectiveDistance = maxEffectiveDistance * normalizedSpeed
            val targetRadius = innerCircleRadius + effectiveDistance

            // Calculate angle and position within bounds
            val angle = atan2(dy.toDouble(), dx.toDouble())
            var targetPointX = (cos(angle) * targetRadius).toFloat()
            var targetPointY = (sin(angle) * targetRadius).toFloat()

            // Enforce startPercentage boundary
            val distanceFromCenter = sqrt((targetPointX * targetPointX + targetPointY * targetPointY).toDouble()).toFloat()
            if (distanceFromCenter < innerCircleRadius) {
                targetPointX = (cos(angle) * innerCircleRadius).toFloat()
                targetPointY = (sin(angle) * innerCircleRadius).toFloat()
            }

            // Smoothly interpolate to avoid flickering
            currentPointX += (targetPointX - currentPointX) * smoothingFactor
            currentPointY += (targetPointY - currentPointY) * smoothingFactor

            updateTouchPoint(currentPointX, currentPointY)

            prevX = x
            prevY = y
            prevTime = currentTime
        } else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            aimTouchListener?.onAimTouchMoveMacro(0f, 0f)
        }
        return true
    }

    // Method to calculate speed
    private fun calculateSpeed(dx: Float, dy: Float, timeElapsed: Long): Float {
        return if (timeElapsed == 0L) 0f else (Math.sqrt((dx * dx + dy * dy).toDouble()) / timeElapsed).toFloat()
    }

    // Normalize speed to a value between 0 and 1
    private fun normalizeSpeed(speed: Float): Float {
        val minSpeed = 0.1f
        val maxSpeed = 5f
        return ((speed - minSpeed) / (maxSpeed - minSpeed)).coerceIn(0f, 1f)
    }

    // Update and log the touch point in your circle
    private fun updateTouchPoint(x: Float, y: Float) {
        val constrainedX = x.coerceIn(-CIRCLE_RADIUS, CIRCLE_RADIUS)
        val constrainedY = y.coerceIn(-CIRCLE_RADIUS, CIRCLE_RADIUS)

        aimTouchListener?.onAimTouchMoveMacro(constrainedX, -constrainedY)
        //Log.d("TouchPoint", "X: $constrainedX, Y: -$constrainedY")

        // Here, you can add the code to visually update your UI element (e.g., moving a point on the screen)
        // For example, update a View position based on constrainedX and constrainedY
        // yourView.x = constrainedX
        // yourView.y = constrainedY
    }

}