package com.example.zerocontrollerclient

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class JoystickView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paintBase = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
    }
    private var joystickX: Int = 0
    private var joystickY: Int = 0
    private var centerPoint = 50f
    private var maxDistanceFromCenter = 30f
    private val paintStick = Paint().apply {
        style = Paint.Style.FILL
    }
    var left = true
    private var actionId: Int? = null
    private var touchPoint: PointF? = null

    private var isRunning = false
    private var isTouching = false
    var joystickInput = StringBuilder()

    private val loggingThread = Thread {
        while (isRunning) {
            if (isTouching)
                joystickInput.append("$joystickX $joystickY ")
            try {
                Thread.sleep(1000 / 24.toLong())
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isRunning = true
        loggingThread.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isRunning = false
        try {
            loggingThread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val scale = width / 100f
        paintBase.strokeWidth = 5 * scale
        centerPoint = 50f * scale
        maxDistanceFromCenter = 35f * scale
        if (touchPoint==null)
        {
            touchPoint=PointF(centerPoint,centerPoint)
        }

        canvas.drawCircle(centerPoint, centerPoint, 42 * scale, paintBase)

        val circle = Path()
        circle.addCircle(50f*scale,50f*scale,15f*scale,Path.Direction.CW)

        val rectL = Path()
        rectL.reset()
        rectL.moveTo(46.5f * scale,42 * scale)
        rectL.lineTo(50 * scale,42 * scale)
        rectL.lineTo(50 * scale,53.5f * scale)
        rectL.lineTo(56 * scale,53.5f * scale)
        rectL.lineTo(56 * scale,57 * scale)
        rectL.lineTo(46.5f * scale,57 * scale)
        rectL.close()

        val rectR = Path()
        rectR.reset()
        rectR.moveTo(51.8f * scale, 42 * scale)
        rectR.lineTo(45.5f * scale, 42 * scale)
        rectR.lineTo(45.5f * scale, 57 * scale)
        rectR.lineTo(48.5f * scale, 57 * scale)
        rectR.lineTo(48.5f * scale, 51 * scale)
        rectR.lineTo(52.5f * scale, 57 * scale)
        rectR.lineTo(56 * scale, 57 * scale)
        rectR.lineTo(51.8f * scale, 50.5f * scale)
        rectR.quadTo(55.5f * scale, 50 * scale, 55.5f * scale, 46.5f * scale)
        rectR.quadTo(55.5f * scale, 43 * scale, 51.8f * scale, 42 * scale)
        rectR.lineTo(48.5f * scale, 44.5f * scale)
        rectR.lineTo(50.5f * scale, 44.5f * scale)
        rectR.quadTo(52.4f * scale, 44.5f * scale, 52.5f * scale, 46 * scale)
        rectR.quadTo(52.6f * scale, 48 * scale, 50.5f * scale, 48 * scale)
        rectR.lineTo(48.5f * scale, 48 * scale)
        rectR.lineTo(48.5f * scale, 44.5f * scale)
        rectR.close()

        if(left) {
            touchPoint?.let {
                val matrix = Matrix()
                matrix.setTranslate(it.x - centerPoint, it.y - centerPoint)
                circle.transform(matrix)
                rectL.transform(matrix)
                paintStick.color = Color.WHITE
                canvas.drawPath(circle, paintStick)
                paintStick.color = Color.BLACK
                canvas.drawPath(rectL, paintStick)
            }
        } else {
            touchPoint?.let {
                val matrix = Matrix()
                matrix.setTranslate(it.x - centerPoint, it.y - centerPoint)
                circle.transform(matrix)
                rectR.transform(matrix)
                paintStick.color = Color.WHITE
                canvas.drawPath(circle, paintStick)
                paintStick.color = Color.BLACK
                canvas.drawPath(rectR, paintStick)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val actionIndex = event.actionIndex
        val touchX = event.getX(actionIndex)
        val touchY = event.getY(actionIndex)
        val distance = sqrt((touchX - centerPoint).toDouble().pow(2.0) + (touchY - centerPoint).toDouble().pow(2.0)).toFloat()
        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_POINTER_DOWN -> {
                isTouching = true
                if(actionId == null || actionId == event.getPointerId(actionIndex)) {
                    touchPoint = if (distance <= maxDistanceFromCenter) {
                        PointF(touchX, touchY)
                    } else {
                        val angle = atan2(touchY - centerPoint, touchX - centerPoint)
                        PointF(centerPoint + maxDistanceFromCenter * cos(angle), centerPoint + maxDistanceFromCenter * sin(angle))
                    }
                    joystickX = (((touchPoint!!.x - centerPoint) / maxDistanceFromCenter) * 32767).toInt().coerceIn(-32767, 32767)
                    joystickY = (((touchPoint!!.y - centerPoint) / maxDistanceFromCenter) * 32767).toInt().coerceIn(-32767, 32767)
                    actionId = event.getPointerId(actionIndex)
//                    Log.d("joy","$joystickX,$joystickY")
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                isTouching = false
                joystickInput.clear()
                if (actionId == event.getPointerId(actionIndex)) {
                    touchPoint = PointF(centerPoint, centerPoint)
                    joystickX = 0
                    joystickY = 0
                    actionId = null
                    invalidate()
                }
            }
        }
        return true
    }
}