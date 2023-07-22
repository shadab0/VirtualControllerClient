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

class ActionButtonView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val path1 = Path()
    private val path2 = Path()
    private val path3 = Path()
    private val path4 = Path()
    private var previousPath: Path? = null
    private val activePaths: MutableMap<Int, Path> = mutableMapOf()
    private val pathPaint1 = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
    }
    private val pathPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
    }
    private val pathPaint2 = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
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

        val scale = width / 188f
        pathPaint1.strokeWidth = 5 * scale
        pathPaint2.strokeWidth = 5 * scale

        path1.reset()
        path2.reset()
        path3.reset()
        path4.reset()
        path1.addCircle(34 * scale,94 * scale,31 * scale, Path.Direction.CW)
        path2.addCircle(94 * scale,34 * scale,31 * scale, Path.Direction.CW)
        path3.addCircle(154 * scale,94 * scale,31 * scale, Path.Direction.CW)
        path4.addCircle(94 * scale,154 * scale,31 * scale, Path.Direction.CW)

        viewBounds.set(0, 0, width, height)
        region1.setPath(path1, Region(viewBounds))
        region2.setPath(path2, Region(viewBounds))
        region3.setPath(path3, Region(viewBounds))
        region4.setPath(path4, Region(viewBounds))

        val path_a = Path()
        path_a.reset()
        path_a.moveTo(91.890625f * scale, 139.9375f * scale)
        path_a.lineTo(96.109375f * scale, 139.9375f * scale)
        path_a.lineTo(105.25f * scale, 164.8105469f * scale)
        path_a.lineTo(105.25f * scale, 168.0625f * scale)
        path_a.lineTo(99.625f * scale, 168.0625f * scale)
        path_a.lineTo(99.625f * scale, 165.5136719f * scale)
        path_a.lineTo(98.5703125f * scale, 162.4375f * scale)
        path_a.lineTo(89.4296875f * scale, 162.4375f * scale)
        path_a.lineTo(88.375f * scale, 165.5136719f * scale)
        path_a.lineTo(88.375f * scale, 168.0625f * scale)
        path_a.lineTo(82.75f * scale, 168.0625f * scale)
        path_a.lineTo(82.75f * scale, 164.8105469f * scale)
        path_a.lineTo(91.890625f * scale, 139.9375f * scale)
        path_a.close()
        path_a.moveTo(94f * scale, 150.484375f * scale)
        path_a.lineTo(91.5390625f * scale, 156.8125f * scale)
        path_a.lineTo(96.4609375f * scale, 156.8125f * scale)
        path_a.lineTo(94f * scale, 150.484375f * scale)
        path_a.close()

        val path_b = Path()
        path_b.reset()
        path_b.moveTo(145.5625f * scale, 79.9375f * scale)
        path_b.lineTo(157.0761719f * scale, 79.9375f * scale)
        path_b.lineTo(158.4824219f * scale, 80.11328125f * scale)
        path_b.lineTo(161.1191406f * scale, 81.16796875f * scale)
        path_b.lineTo(162.9648438f * scale, 82.66210938f * scale)
        path_b.lineTo(164.7226563f * scale, 85.47460938f * scale)
        path_b.lineTo(165.25f * scale, 88.46289063f * scale)
        path_b.lineTo(164.7226563f * scale, 90.57226563f * scale)
        path_b.lineTo(163.3164063f * scale, 92.68164063f * scale)
        path_b.lineTo(164.9863281f * scale, 93.82421875f * scale)
        path_b.lineTo(166.65625f * scale, 95.4941406f * scale)
        path_b.lineTo(167.7109375f * scale, 97.4277344f * scale)
        path_b.lineTo(168.0625f * scale, 99.8886719f * scale)
        path_b.lineTo(167.5351563f * scale, 102.5253906f * scale)
        path_b.lineTo(166.4804688f * scale, 104.4589844f * scale)
        path_b.lineTo(165.1621094f * scale, 105.953125f * scale)
        path_b.lineTo(162.5253906f * scale, 107.5351563f * scale)
        path_b.lineTo(159.8886719f * scale, 108.0625f * scale)
        path_b.lineTo(145.5625f * scale, 108.0625f * scale)
        path_b.lineTo(145.5625f * scale, 79.9375f * scale)
        path_b.close()
        path_b.moveTo(151.1875f * scale, 85.5625f * scale)
        path_b.lineTo(151.1875f * scale, 91.1875f * scale)
        path_b.lineTo(157.6914063f * scale, 91.1875f * scale)
        path_b.lineTo(158.7460938f * scale, 90.66015625f * scale)
        path_b.lineTo(159.625f * scale, 89.25390625f * scale)
        path_b.lineTo(159.625f * scale, 87.671875f * scale)
        path_b.lineTo(159.0976563f * scale, 86.6171875f * scale)
        path_b.lineTo(157.6914063f * scale, 85.5625f * scale)
        path_b.lineTo(151.1875f * scale, 85.5625f * scale)
        path_b.close()
        path_b.moveTo(151.1875f * scale, 96.8125f * scale)
        path_b.lineTo(151.1875f * scale, 102.4375f * scale)
        path_b.lineTo(160.5039063f * scale, 102.4375f * scale)
        path_b.lineTo(161.5585938f * scale, 101.9101563f * scale)
        path_b.lineTo(162.4375f * scale, 100.5039063f * scale)
        path_b.lineTo(162.4375f * scale, 98.921875f * scale)
        path_b.lineTo(161.9101563f * scale, 97.8671875f * scale)
        path_b.lineTo(160.5039063f * scale, 96.8125f * scale)
        path_b.lineTo(151.1875f * scale, 96.8125f * scale)
        path_b.close()

        val path_x = Path()
        path_x.reset()
        path_x.moveTo(22.92578125f * scale, 79.9375f * scale)
        path_x.lineTo(28.375f * scale, 79.9375f * scale)
        path_x.lineTo(34f * scale, 89.4296875f * scale)
        path_x.lineTo(39.625f * scale, 79.9375f * scale)
        path_x.lineTo(45.07421875f * scale, 79.9375f * scale)
        path_x.lineTo(36.63671875f * scale, 94f * scale)
        path_x.lineTo(45.07421875f * scale, 107.8867188f * scale)
        path_x.lineTo(39.625f * scale, 107.8867188f * scale)
        path_x.lineTo(34f * scale, 98.5703125f * scale)
        path_x.lineTo(28.375f * scale, 107.8867188f * scale)
        path_x.lineTo(22.92578125f * scale, 107.8867188f * scale)
        path_x.lineTo(31.36328125f * scale, 94f * scale)
        path_x.lineTo(22.92578125f * scale, 79.9375f * scale)
        path_x.close()

        val path_y = Path()
        path_y.reset()
        path_y.moveTo(82.92578125f * scale, 19.9375f * scale)
        path_y.lineTo(88.375f * scale, 19.9375f * scale)
        path_y.lineTo(94f * scale, 33.12109375f * scale)
        path_y.lineTo(99.625f * scale, 19.9375f * scale)
        path_y.lineTo(105.0742188f * scale, 19.9375f * scale)
        path_y.lineTo(96.6367188f * scale, 39.625f * scale)
        path_y.lineTo(96.6367188f * scale, 47.88671875f * scale)
        path_y.lineTo(91.1875f * scale, 47.88671875f * scale)
        path_y.lineTo(91.1875f * scale, 39.625f * scale)
        path_y.lineTo(82.92578125f * scale, 19.9375f * scale)
        path_y.close()

        canvas.drawPath(path_a,pathPaint)
        canvas.drawPath(path_b,pathPaint)
        canvas.drawPath(path_x,pathPaint)
        canvas.drawPath(path_y,pathPaint)
        canvas.drawPath(path1,activePaint1)
        canvas.drawPath(path2,activePaint2)
        canvas.drawPath(path3,activePaint3)
        canvas.drawPath(path4,activePaint4)
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