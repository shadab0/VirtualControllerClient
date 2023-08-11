package com.example.zerocontrollerclient

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Region
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class DpadView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val path1 = Path()
    private val path2 = Path()
    private val path3 = Path()
    private val path4 = Path()
    private var previousPath: Path? = null
    private val activePaths: MutableMap<Int, Path> = mutableMapOf()
    private val pathPaint1 = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val pathPaint2 = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    private var activePaint1: Paint = pathPaint1
    private var activePaint2: Paint = pathPaint1
    private var activePaint3: Paint = pathPaint1
    private var activePaint4: Paint = pathPaint1
    private val region1 = Region()
    private val region2 = Region()
    private val region3 = Region()
    private val region4 = Region()
    private val viewBounds: Rect = Rect()

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val scale = width / 73f

        // Create a Path object and define your desired path

        path1.reset()
        path2.reset()
        path3.reset()
        path4.reset()
        path1.moveTo(0 * scale, 24 * scale)
        path1.lineTo(22 * scale, 24 * scale)
        path1.lineTo(34 * scale, 36 * scale)
        path1.lineTo(22 * scale, 49 * scale)
        path1.lineTo(0 * scale, 49 * scale)
        path1.lineTo(0 * scale, 24 * scale)
        path1.close()

        path2.moveTo(24 * scale, 0 * scale)
        path2.lineTo(24 * scale, 22 * scale)
        path2.lineTo(36 * scale, 34 * scale)
        path2.lineTo(49 * scale, 22 * scale)
        path2.lineTo(49 * scale, 0 * scale)
        path2.lineTo(24 * scale, 0 * scale)
        path2.close()

        path3.moveTo(73 * scale, 24 * scale)
        path3.lineTo(51 * scale, 24 * scale)
        path3.lineTo(39 * scale, 36 * scale)
        path3.lineTo(51 * scale, 49 * scale)
        path3.lineTo(73 * scale, 49 * scale)
        path3.lineTo(73 * scale, 24 * scale)
        path3.close()

        path4.moveTo(24 * scale, 73 * scale)
        path4.lineTo(24 * scale, 51 * scale)
        path4.lineTo(36 * scale, 39 * scale)
        path4.lineTo(49 * scale, 51 * scale)
        path4.lineTo(49 * scale, 73 * scale)
        path4.lineTo(24 * scale, 73 * scale)
        path4.close()
        // Add more path operations as needed
        viewBounds.set(0, 0, width, height)
        region1.setPath(path1, Region(viewBounds))
        region2.setPath(path2, Region(viewBounds))
        region3.setPath(path3, Region(viewBounds))
        region4.setPath(path4, Region(viewBounds))
        // Draw the path on the canvas

        canvas.drawPath(path1, activePaint1)
        canvas.drawPath(path2, activePaint2)
        canvas.drawPath(path3, activePaint3)
        canvas.drawPath(path4, activePaint4)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val actionIndex = event.actionIndex
        val actionId = event.getPointerId(actionIndex)
        val touchX = event.getX(actionIndex)
        val touchY = event.getY(actionIndex)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (region1.contains(touchX.toInt(), touchY.toInt()) && !activePaths.containsValue(path1)) {
                    activePaths[actionId] = path1
                    activePaint1 = pathPaint2
                    Log.d("DpadView", "Left Down=== $actionId")
                }
                if (region2.contains(touchX.toInt(), touchY.toInt()) && !activePaths.containsValue(path2)) {
                    activePaths[actionId] = path2
                    activePaint2 = pathPaint2
                    Log.d("DpadView", "Top Down=== $actionId")
                }
                if (region3.contains(touchX.toInt(), touchY.toInt()) && !activePaths.containsValue(path3)) {
                    activePaths[actionId] = path3
                    activePaint3 = pathPaint2
                    Log.d("DpadView", "Right Down=== $actionId")
                }
                if (region4.contains(touchX.toInt(), touchY.toInt()) && !activePaths.containsValue(path4)) {
                    activePaths[actionId] = path4
                    activePaint4 = pathPaint2
                    Log.d("DpadView", "Bottom Down=== $actionId")
                }
                previousPath = activePaths[actionId]
            }

            MotionEvent.ACTION_MOVE -> {
                Log.d("dpad","x=$touchX, y=$touchY")
                if (previousPath == null)
                {
                    return true
                }
                if (region1.contains(touchX.toInt(), touchY.toInt()) && previousPath != path1) {
                    actionUp(actionId)
                    activePaths[actionId]=path1
                    previousPath = path1
                    activePaint1 = pathPaint2
                    Log.d("DpadView", "moving left=== $actionId")
                }
                if (region2.contains(touchX.toInt(), touchY.toInt()) && previousPath != path2) {
                    actionUp(actionId)
                    activePaths[actionId]=path2
                    previousPath = path2
                    activePaint2 = pathPaint2
                    Log.d("DpadView", "moving top=== $actionId")
                }
                if (region3.contains(touchX.toInt(), touchY.toInt()) && previousPath != path3) {
                    actionUp(actionId)
                    activePaths[actionId]=path3
                    previousPath = path3
                    activePaint3 = pathPaint2
                    Log.d("DpadView", "moving right=== $actionId")
                }
                if (region4.contains(touchX.toInt(), touchY.toInt()) && previousPath != path4) {
                    actionUp(actionId)
                    activePaths[actionId]=path4
                    previousPath = path4
                    activePaint4 = pathPaint2
                    Log.d("DpadView", "moving bottom=== $actionId")
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                actionUp(actionId)
            }

            MotionEvent.ACTION_CANCEL -> {
                activePaint1 = pathPaint1
                activePaint2 = pathPaint1
                activePaint3 = pathPaint1
                activePaint4 = pathPaint1
                activePaths.clear()
            }
        }
        invalidate()
        return true
    }

    private fun actionUp(actionId: Int){
        Log.d("DpadView", "UP=== $actionId")
        if (activePaths[actionId] == path1) {
            activePaint1 = pathPaint1
            Log.d("DpadView", "Left Up $actionId")
        }
        if (activePaths[actionId] == path2) {
            activePaint2 = pathPaint1
            Log.d("DpadView", "Top Up $actionId")
        }
        if (activePaths[actionId] == path3) {
            activePaint3 = pathPaint1
            Log.d("DpadView", "Right Up $actionId")
        }
        if (activePaths[actionId] == path4) {
            activePaint4 = pathPaint1
            Log.d("DpadView", "Bottom Up $actionId")
        }
        activePaths.remove(actionId)
    }
}
