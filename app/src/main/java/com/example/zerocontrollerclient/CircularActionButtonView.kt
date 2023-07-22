package com.example.zerocontrollerclient

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Region
import android.graphics.Shader
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class CircularActionButtonView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val path1 = Path()
    private val path2 = Path()
    private val path3 = Path()
    private val path4 = Path()
    private var previousPath: Path? = null
    private val activePaths: MutableMap<Int, Path> = mutableMapOf()
    private val pathDownPaint1 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pathDownPaint2 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pathDownPaint3 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pathDownPaint4 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pathUpPaint1 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pathUpPaint2 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pathUpPaint3 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pathUpPaint4 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pathiconDownPaint1 = Paint().apply {
        color = Color.argb(255, 255,255,0)
        style = Paint.Style.FILL
    }
    private val pathiconDownPaint2 = Paint().apply {
        color = Color.argb(255, 77,153,255)
        style = Paint.Style.FILL
    }
    private val pathiconDownPaint3 = Paint().apply {
        color = Color.argb(255, 255,0,0)
        style = Paint.Style.FILL
    }
    private val pathiconDownPaint4 = Paint().apply {
        color = Color.argb(255, 0,255,0)
        style = Paint.Style.FILL
    }
    private val pathiconUpPaint1 = Paint().apply {
        color = Color.argb(230, 161,160,31)
        style = Paint.Style.FILL
    }
    private val pathiconUpPaint2 = Paint().apply {
        color = Color.argb(230, 45,122,203)
        style = Paint.Style.FILL
    }
    private val pathiconUpPaint3 = Paint().apply {
        color = Color.argb(230, 110,0,0)
        style = Paint.Style.FILL
    }
    private val pathiconUpPaint4 = Paint().apply {
        color = Color.argb(230, 0,132,0)
        style = Paint.Style.FILL
    }
    private var activePaint1: Paint = pathUpPaint1
    private var activePaint2: Paint = pathUpPaint2
    private var activePaint3: Paint = pathUpPaint3
    private var activePaint4: Paint = pathUpPaint4
    private var activeiconPaint1: Paint = pathiconUpPaint1
    private var activeiconPaint2: Paint = pathiconUpPaint2
    private var activeiconPaint3: Paint = pathiconUpPaint3
    private var activeiconPaint4: Paint = pathiconUpPaint4
    private val region1 = Region()
    private val region2 = Region()
    private val region3 = Region()
    private val region4 = Region()
    private val viewBounds: Rect = Rect()

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val scale = width / 100f
        pathDownPaint1.shader = RadialGradient(50 * scale,50 * scale,53 * scale,intArrayOf(Color.argb(150,255,255,0),Color.TRANSPARENT),null,Shader.TileMode.CLAMP)
        pathDownPaint2.shader = RadialGradient(50 * scale,50 * scale,53 * scale,intArrayOf(Color.argb(200, 77,153,255), Color.TRANSPARENT),null,Shader.TileMode.CLAMP)
        pathDownPaint3.shader = RadialGradient(50 * scale,50 * scale,53 * scale,intArrayOf(Color.argb(150, 255,0,0), Color.TRANSPARENT),null,Shader.TileMode.CLAMP)
        pathDownPaint4.shader = RadialGradient(50 * scale,50 * scale,53 * scale,intArrayOf(Color.argb(150, 0,255,0), Color.TRANSPARENT),null,Shader.TileMode.CLAMP)
        pathUpPaint1.shader = RadialGradient(50 * scale,50 * scale,49 * scale,intArrayOf(Color.argb(125,161,160,31),Color.TRANSPARENT),null,Shader.TileMode.CLAMP)
        pathUpPaint2.shader = RadialGradient(50 * scale,50 * scale,49 * scale,intArrayOf(Color.argb(125, 45,122,203), Color.TRANSPARENT),null,Shader.TileMode.CLAMP)
        pathUpPaint3.shader = RadialGradient(50 * scale,50 * scale,49 * scale,intArrayOf(Color.argb(125, 110,0,0), Color.TRANSPARENT),null,Shader.TileMode.CLAMP)
        pathUpPaint4.shader = RadialGradient(50 * scale,50 * scale,49 * scale,intArrayOf(Color.argb(125, 0,132,0), Color.TRANSPARENT),null,Shader.TileMode.CLAMP)
        activeiconPaint1.setShadowLayer(0.1f * scale, 1f * scale, 1f * scale, Color.BLACK)
        activeiconPaint2.setShadowLayer(0.1f * scale, 1f * scale, 1f * scale, Color.BLACK)
        activeiconPaint3.setShadowLayer(0.1f * scale, 1f * scale, 1f * scale, Color.BLACK)
        activeiconPaint4.setShadowLayer(0.1f * scale, 1f * scale, 1f * scale, Color.BLACK)

        path1.reset()
        path1.moveTo(0 * scale, 0 * scale)
        path1.lineTo(50 * scale, 50 * scale)
        path1.lineTo(0 * scale, 100 * scale)
        path1.close()
        path2.reset()
        path2.moveTo(0 * scale, 0 * scale)
        path2.lineTo(50 * scale, 50 * scale)
        path2.lineTo(100 * scale, 0 * scale)
        path2.close()
        path3.reset()
        path3.moveTo(100 * scale, 0 * scale)
        path3.lineTo(50 * scale, 50 * scale)
        path3.lineTo(100 * scale, 100 * scale)
        path3.close()
        path4.reset()
        path4.moveTo(0 * scale, 100 * scale)
        path4.lineTo(50 * scale, 50 * scale)
        path4.lineTo(100 * scale, 100 * scale)
        path4.close()

        val pathicon1 = Path()
        pathicon1.reset()
        pathicon1.moveTo(20.55f * scale, 38.2f * scale)
        pathicon1.quadTo(21.28f * scale, 38.08f * scale, 21.6f * scale, 38.55f * scale)
        pathicon1.lineTo(27.9f * scale, 46.45f * scale)
        pathicon1.lineTo(27.9f * scale, 54.6f * scale)
        pathicon1.lineTo(18.8f * scale, 55.4f * scale)
        pathicon1.lineTo(16.5f * scale, 49.15f * scale)
        pathicon1.lineTo(15.8f * scale, 46.85f * scale)
        pathicon1.lineTo(20.0f * scale, 38.65f * scale)
        pathicon1.lineTo(20.35f * scale, 38.3f * scale)
        pathicon1.lineTo(20.55f * scale, 38.2f * scale)
        pathicon1.close()  
        pathicon1.moveTo(17.15f * scale, 41.7f * scale)
        pathicon1.lineTo(17.2f * scale, 41.85f * scale)
        pathicon1.lineTo(14.3f * scale, 47.05f * scale)
        pathicon1.lineTo(18.1f * scale, 56.85f * scale)
        pathicon1.lineTo(18.2f * scale, 57.45f * scale)
        pathicon1.lineTo(15.65f * scale, 59f * scale)
        pathicon1.quadTo(15.04f * scale, 59.34f * scale, 13.95f * scale, 59.2f * scale)
        pathicon1.lineTo(11.65f * scale, 58.3f * scale)
        pathicon1.lineTo(10.7f * scale, 57.45f * scale)
        pathicon1.lineTo(9.9f * scale, 56.05f * scale)
        pathicon1.lineTo(7.2f * scale, 48.55f * scale)
        pathicon1.lineTo(7.1f * scale, 47.35f * scale)
        pathicon1.lineTo(7.4f * scale, 45.05f * scale)
        pathicon1.lineTo(10.15f * scale, 42.0f * scale)
        pathicon1.lineTo(12.75f * scale, 41.9f * scale)
        pathicon1.close()
        pathicon1.moveTo(35.75f * scale, 43.7f * scale)
        pathicon1.lineTo(36.2f * scale, 43.7f * scale)
        pathicon1.lineTo(36.1f * scale, 45.05f * scale)
        pathicon1.lineTo(35.5f * scale, 55.75f * scale)
        pathicon1.lineTo(35.4f * scale, 56.0f * scale)
        pathicon1.lineTo(29.2f * scale, 54.6f * scale)
        pathicon1.lineTo(29.2f * scale, 53.35f * scale)
        pathicon1.lineTo(28.9f * scale, 45.3f * scale)
        pathicon1.close()

        val pathicon2 = Path()
        pathicon2.reset()
        pathicon2.moveTo(50.3f * scale, 6.7f * scale)
        pathicon2.lineTo(61.2f * scale, 10.55f * scale)
        pathicon2.lineTo(58.55f * scale, 17.7f * scale)
        pathicon2.lineTo(48.5f * scale, 14.65f * scale)
        pathicon2.close()
        pathicon2.moveTo(48.15f * scale, 7.9f * scale)
        pathicon2.quadTo(48.74f * scale, 7.69f * scale, 48.6f * scale, 8.15f * scale)
        pathicon2.lineTo(48.1f * scale, 10.1f * scale)
        pathicon2.quadTo(43.96f * scale, 10.73f * scale, 41.8f * scale, 13.45f * scale)
        pathicon2.lineTo(40.45f * scale, 15.2f * scale)
        pathicon2.quadTo(39.99f * scale, 15.33f * scale, 40.2f * scale, 14.75f * scale)
        pathicon2.quadTo(41.22f * scale, 12.02f * scale, 43.25f * scale, 10.3f * scale)
        pathicon2.quadTo(44.4f * scale, 9.25f * scale, 45.95f * scale, 8.6f * scale)
        pathicon2.close()
        pathicon2.moveTo(46.95f * scale, 11.4f * scale)
        pathicon2.quadTo(47.4f * scale, 11.35f * scale, 47.4f * scale, 11.45f * scale)
        pathicon2.lineTo(47.4f * scale, 12.95f * scale)
        pathicon2.lineTo(46.45f * scale, 13.2f * scale)
        pathicon2.lineTo(45.55f * scale, 13.5f * scale)
        pathicon2.lineTo(44.2f * scale, 14.55f * scale)
        pathicon2.lineTo(43.25f * scale, 15.8f * scale)
        pathicon2.quadTo(42.95f * scale, 15.85f * scale, 43.1f * scale, 15.15f * scale)
        pathicon2.quadTo(43.63f * scale, 13.58f * scale, 44.75f * scale, 12.6f * scale)
        pathicon2.quadTo(45.65f * scale, 11.8f * scale, 46.95f * scale, 11.4f * scale)
        pathicon2.close()
        pathicon2.moveTo(46.65f * scale, 14.6f * scale)
        pathicon2.quadTo(46.98f * scale, 14.49f * scale, 46.9f * scale, 14.75f * scale)
        pathicon2.lineTo(47.4f * scale, 16.15f * scale)
        pathicon2.lineTo(45.45f * scale, 17.0f * scale)
        pathicon2.lineTo(44.6f * scale, 17.85f * scale)
        pathicon2.lineTo(44.1f * scale, 18.7f * scale)
        pathicon2.quadTo(43.61f * scale, 18.88f * scale, 43.8f * scale, 18.25f * scale)
        pathicon2.lineTo(44.4f * scale, 16.85f * scale)
        pathicon2.lineTo(45.3f * scale, 15.6f * scale)
        pathicon2.lineTo(46.65f * scale, 14.6f * scale)
        pathicon2.close()
        pathicon2.moveTo(49.5f * scale, 16.4f * scale)
        pathicon2.lineTo(50.05f * scale, 16.6f * scale)
        pathicon2.lineTo(51.3f * scale, 17.65f * scale)
        pathicon2.lineTo(51.15f * scale, 17.9f * scale)
        pathicon2.lineTo(50.3f * scale, 18.65f * scale)
        pathicon2.lineTo(49.9f * scale, 19.35f * scale)
        pathicon2.lineTo(49.6f * scale, 20.55f * scale)
        pathicon2.lineTo(49.6f * scale, 21.05f * scale)
        pathicon2.lineTo(50.2f * scale, 22.35f * scale)
        pathicon2.lineTo(51.15f * scale, 23.2f * scale)
        pathicon2.lineTo(51.1f * scale, 22.15f * scale)
        pathicon2.lineTo(51.1f * scale, 21.65f * scale)
        pathicon2.lineTo(51.4f * scale, 20.85f * scale)
        pathicon2.lineTo(52.45f * scale, 19.6f * scale)
        pathicon2.lineTo(53.6f * scale, 19.3f * scale)
        pathicon2.lineTo(53.6f * scale, 17.65f * scale)
        pathicon2.lineTo(55.15f * scale, 17.9f * scale)
        pathicon2.lineTo(56.3f * scale, 18.0f * scale)
        pathicon2.lineTo(56.15f * scale, 18.3f * scale)
        pathicon2.quadTo(55.54f * scale, 18.44f * scale, 55.3f * scale, 18.95f * scale)
        pathicon2.lineTo(55.1f * scale, 19.55f * scale)
        pathicon2.lineTo(56.4f * scale, 20.45f * scale)
        pathicon2.lineTo(57.0f * scale, 21.75f * scale)
        pathicon2.lineTo(57.0f * scale, 22.95f * scale)
        pathicon2.lineTo(56.9f * scale, 24.05f * scale)
        pathicon2.lineTo(56.5f * scale, 24.85f * scale)
        pathicon2.lineTo(55.45f * scale, 26.0f * scale)
        pathicon2.lineTo(54.85f * scale, 26.3f * scale)
        pathicon2.lineTo(53.95f * scale, 26.3f * scale)
        pathicon2.lineTo(53.8f * scale, 26.45f * scale)
        pathicon2.lineTo(53.8f * scale, 27.45f * scale)
        pathicon2.lineTo(52.3f * scale, 29.55f * scale)
        pathicon2.quadTo(51.82f * scale, 30.42f * scale, 51.7f * scale, 31.65f * scale)
        pathicon2.quadTo(52.46f * scale, 34.44f * scale, 54.45f * scale, 36.0f * scale)
        pathicon2.lineTo(55.9f * scale, 37.05f * scale)
        pathicon2.lineTo(55.8f * scale, 37.45f * scale)
        pathicon2.lineTo(54.6f * scale, 39.55f * scale)
        pathicon2.lineTo(53.75f * scale, 40.8f * scale)
        pathicon2.lineTo(52.05f * scale, 40.8f * scale)
        pathicon2.lineTo(51.9f * scale, 40.65f * scale)
        pathicon2.lineTo(52.0f * scale, 39.45f * scale)
        pathicon2.quadTo(52.85f * scale, 38.67f * scale, 53.1f * scale, 37.4f * scale)
        pathicon2.quadTo(51.49f * scale, 36.75f * scale, 50.45f * scale, 35.4f * scale)
        pathicon2.lineTo(50.4f * scale, 36.85f * scale)
        pathicon2.lineTo(50.5f * scale, 40.8f * scale)
        pathicon2.lineTo(45.25f * scale, 40.8f * scale)
        pathicon2.lineTo(44.3f * scale, 39.85f * scale)
        pathicon2.lineTo(44.1f * scale, 39.25f * scale)
        pathicon2.lineTo(44.35f * scale, 39.0f * scale)
        pathicon2.lineTo(45.75f * scale, 38.8f * scale)
        pathicon2.lineTo(47.6f * scale, 38.1f * scale)
        pathicon2.lineTo(47.6f * scale, 31.55f * scale)
        pathicon2.quadTo(47.08f * scale, 30.97f * scale, 47.3f * scale, 29.65f * scale)
        pathicon2.lineTo(48.6f * scale, 27.25f * scale)
        pathicon2.lineTo(49.65f * scale, 26.0f * scale)
        pathicon2.lineTo(50.45f * scale, 25.6f * scale)
        pathicon2.quadTo(51.31f * scale, 25.33f * scale, 51.05f * scale, 25.4f * scale)
        pathicon2.lineTo(47.6f * scale, 22.25f * scale)
        pathicon2.lineTo(47.6f * scale, 20.35f * scale)
        pathicon2.lineTo(47.8f * scale, 19.55f * scale)
        pathicon2.lineTo(48.5f * scale, 17.85f * scale)
        pathicon2.lineTo(49.5f * scale, 16.4f * scale)
        pathicon2.close()

        val pathicon3 = Path()
        pathicon3.reset()
        pathicon3.moveTo(83.55f * scale, 34.1f * scale)
        pathicon3.lineTo(84.09f * scale, 33.91f * scale)
        pathicon3.lineTo(85.0f * scale, 35.15f * scale)
        pathicon3.quadTo(85.11f * scale, 35.48f * scale, 84.85f * scale, 35.4f * scale)
        pathicon3.quadTo(82.56f * scale, 36.56f * scale, 81f * scale, 38.45f * scale)
        pathicon3.lineTo(79.8f * scale, 40.05f * scale)
        pathicon3.quadTo(81.41f * scale, 41.04f * scale, 82.6f * scale, 42.45f * scale)
        pathicon3.quadTo(82.73f * scale, 42.83f * scale, 82.35f * scale, 42.7f * scale)
        pathicon3.lineTo(80.85f * scale, 43.1f * scale)
        pathicon3.lineTo(80.5f * scale, 43.35f * scale)
        pathicon3.lineTo(82.4f * scale, 45.45f * scale)
        pathicon3.lineTo(83.5f * scale, 47.35f * scale)
        pathicon3.lineTo(84.0f * scale, 48.75f * scale)
        pathicon3.lineTo(84.3f * scale, 50.15f * scale)
        pathicon3.lineTo(84.3f * scale, 52.85f * scale)
        pathicon3.quadTo(83.59f * scale, 56.79f * scale, 81.15f * scale, 59f * scale)
        pathicon3.lineTo(80.85f * scale, 59.2f * scale)
        pathicon3.lineTo(79.35f * scale, 60.4f * scale)
        pathicon3.lineTo(77.35f * scale, 61.3f * scale)
        pathicon3.lineTo(75.65f * scale, 61.7f * scale)
        pathicon3.lineTo(73.55f * scale, 61.8f * scale)
        pathicon3.lineTo(72.05f * scale, 61.6f * scale)
        pathicon3.lineTo(70.95f * scale, 61.3f * scale)
        pathicon3.lineTo(69.55f * scale, 60.7f * scale)
        pathicon3.quadTo(67.69f * scale, 59.71f * scale, 66.4f * scale, 58.15f * scale)
        pathicon3.quadTo(65.17f * scale, 56.73f * scale, 64.5f * scale, 54.75f * scale)
        pathicon3.lineTo(64.0f * scale, 52.35f * scale)
        pathicon3.lineTo(64.0f * scale, 50.65f * scale)
        pathicon3.lineTo(64.5f * scale, 48.25f * scale)
        pathicon3.lineTo(65.2f * scale, 46.65f * scale)
        pathicon3.quadTo(66.12f * scale, 45.02f * scale, 67.45f * scale, 43.8f * scale)
        pathicon3.quadTo(68.86f * scale, 42.56f * scale, 70.75f * scale, 41.8f * scale)
        pathicon3.lineTo(72.25f * scale, 41.4f * scale)
        pathicon3.lineTo(74.9f * scale, 41.15f * scale)
        pathicon3.lineTo(74.9f * scale, 39.85f * scale)
        pathicon3.lineTo(74.2f * scale, 38.75f * scale)
        pathicon3.lineTo(74.45f * scale, 38.6f * scale)
        pathicon3.lineTo(75.95f * scale, 38.6f * scale)
        pathicon3.lineTo(76.95f * scale, 38.8f * scale)
        pathicon3.lineTo(77.55f * scale, 39.1f * scale)
        pathicon3.quadTo(78.61f * scale, 37.36f * scale, 80.15f * scale, 36.1f * scale)
        pathicon3.lineTo(82.05f * scale, 34.7f * scale)
        pathicon3.lineTo(83.55f * scale, 34.1f * scale)
        pathicon3.close()
        
        val pathicon4 = Path()    
        pathicon4.reset()
        pathicon4.moveTo(47.25f * scale, 58.8f * scale)
        pathicon4.lineTo(47.4f * scale, 58.85f * scale)
        pathicon4.lineTo(47.8f * scale, 65.05f * scale)
        pathicon4.quadTo(48.83f * scale, 67.57f * scale, 50.55f * scale, 69.4f * scale)
        pathicon4.lineTo(50.6f * scale, 69.15f * scale)
        pathicon4.lineTo(49.7f * scale, 67.55f * scale)
        pathicon4.lineTo(49.5f * scale, 66.65f * scale)
        pathicon4.lineTo(49.5f * scale, 65.65f * scale)
        pathicon4.quadTo(49.81f * scale, 63.91f * scale, 50.95f * scale, 63.0f * scale)
        pathicon4.lineTo(52.05f * scale, 62.3f * scale)
        pathicon4.lineTo(53.05f * scale, 62.3f * scale)
        pathicon4.lineTo(53.75f * scale, 62.7f * scale)
        pathicon4.lineTo(56.1f * scale, 64.75f * scale)
        pathicon4.quadTo(56.54f * scale, 65.51f * scale, 56.6f * scale, 66.65f * scale)
        pathicon4.lineTo(56.2f * scale, 67.95f * scale)
        pathicon4.lineTo(53.3f * scale, 71.45f * scale)
        pathicon4.lineTo(53.3f * scale, 71.7f * scale)
        pathicon4.quadTo(54.27f * scale, 72.03f * scale, 54.9f * scale, 72.85f * scale)
        pathicon4.lineTo(55.9f * scale, 74.25f * scale)
        pathicon4.lineTo(57.4f * scale, 77.25f * scale)
        pathicon4.lineTo(57.5f * scale, 78.05f * scale)
        pathicon4.lineTo(57.5f * scale, 78.65f * scale)
        pathicon4.lineTo(57.3f * scale, 79.85f * scale)
        pathicon4.lineTo(55.5f * scale, 82.0f * scale)
        pathicon4.lineTo(55.15f * scale, 82.2f * scale)
        pathicon4.lineTo(54.55f * scale, 82.2f * scale)
        pathicon4.lineTo(54.2f * scale, 81.75f * scale)
        pathicon4.quadTo(54.3f * scale, 80.7f * scale, 54.1f * scale, 80.1f * scale)
        pathicon4.lineTo(53.5f * scale, 79.8f * scale)
        pathicon4.quadTo(53.39f * scale, 79.53f * scale, 53.65f * scale, 79.6f * scale)
        pathicon4.lineTo(54.2f * scale, 79.25f * scale)
        pathicon4.lineTo(54.5f * scale, 78.35f * scale)
        pathicon4.lineTo(54.4f * scale, 77.5f * scale)
        pathicon4.lineTo(54.0f * scale, 76.65f * scale)
        pathicon4.lineTo(53.15f * scale, 75.7f * scale)
        pathicon4.lineTo(52.45f * scale, 75.5f * scale)
        pathicon4.lineTo(51.7f * scale, 75.95f * scale)
        pathicon4.lineTo(49.9f * scale, 79.85f * scale)
        pathicon4.lineTo(50.4f * scale, 81.35f * scale)
        pathicon4.lineTo(51.25f * scale, 82.3f * scale)
        pathicon4.lineTo(51.95f * scale, 82.8f * scale)
        pathicon4.lineTo(55.0f * scale, 84.35f * scale)
        pathicon4.lineTo(52.0f * scale, 89.9f * scale)
        pathicon4.lineTo(49.7f * scale, 89.3f * scale)
        pathicon4.lineTo(49.7f * scale, 88.45f * scale)
        pathicon4.lineTo(50.8f * scale, 85.95f * scale)
        pathicon4.lineTo(43.25f * scale, 78.4f * scale)
        pathicon4.lineTo(42.45f * scale, 78.0f * scale)
        pathicon4.lineTo(41.55f * scale, 78.0f * scale)
        pathicon4.lineTo(40.7f * scale, 78.55f * scale)
        pathicon4.lineTo(40.5f * scale, 79.15f * scale)
        pathicon4.lineTo(40.25f * scale, 79.4f * scale)
        pathicon4.lineTo(37.15f * scale, 80.4f * scale)
        pathicon4.lineTo(37.0f * scale, 80.25f * scale)
        pathicon4.lineTo(35.5f * scale, 73.75f * scale)
        pathicon4.lineTo(36.95f * scale, 72.9f * scale)
        pathicon4.lineTo(37.45f * scale, 72.8f * scale)
        pathicon4.lineTo(38.0f * scale, 73.15f * scale)
        pathicon4.lineTo(39.2f * scale, 76.05f * scale)
        pathicon4.lineTo(39.55f * scale, 76.6f * scale)
        pathicon4.quadTo(40.37f * scale, 75.82f * scale, 41.65f * scale, 75.5f * scale)
        pathicon4.lineTo(42.95f * scale, 75.5f * scale)
        pathicon4.quadTo(44.27f * scale, 75.88f * scale, 45.25f * scale, 76.6f * scale)
        pathicon4.lineTo(45.5f * scale, 74.65f * scale)
        pathicon4.lineTo(45.9f * scale, 73.35f * scale)
        pathicon4.lineTo(46.2f * scale, 72.75f * scale)
        pathicon4.lineTo(47.75f * scale, 70.8f * scale)
        pathicon4.lineTo(48.9f * scale, 70.2f * scale)
        pathicon4.lineTo(46.4f * scale, 67.75f * scale)
        pathicon4.quadTo(44.74f * scale, 65.96f * scale, 43.8f * scale, 63.45f * scale)
        pathicon4.lineTo(47.25f * scale, 58.8f * scale)
        pathicon4.close()
        pathicon4.moveTo(58.85f * scale, 74.2f * scale)
        pathicon4.lineTo(60.8f * scale, 78.35f * scale)
        pathicon4.lineTo(65.9f * scale, 90.0f * scale)
        pathicon4.lineTo(58.9f * scale, 78.45f * scale)
        pathicon4.lineTo(58.9f * scale, 75.65f * scale)
        pathicon4.close()
        pathicon4.moveTo(57.85f * scale, 81.8f * scale)
        pathicon4.lineTo(58.1f * scale, 82.05f * scale)
        pathicon4.lineTo(62.7f * scale, 91.95f * scale)
        pathicon4.lineTo(62.4f * scale, 91.75f * scale)
        pathicon4.lineTo(56.5f * scale, 83.75f * scale)
        pathicon4.lineTo(57.85f * scale, 81.8f * scale)
        pathicon4.close()
        pathicon4.moveTo(54.65f * scale, 86.8f * scale)
        pathicon4.lineTo(54.9f * scale, 86.9f * scale)
        pathicon4.lineTo(58.4f * scale, 92.45f * scale)
        pathicon4.lineTo(58.4f * scale, 92.65f * scale)
        pathicon4.lineTo(58.15f * scale, 92.5f * scale)
        pathicon4.lineTo(53.7f * scale, 88.65f * scale)
        pathicon4.lineTo(54.65f * scale, 86.8f * scale)
        pathicon4.close()

        viewBounds.set(0, 0, width, height)
        region1.setPath(path1, Region(viewBounds))
        region2.setPath(path2, Region(viewBounds))
        region3.setPath(path3, Region(viewBounds))
        region4.setPath(path4, Region(viewBounds))

        canvas.drawPath(path1, activePaint1)
        canvas.drawPath(path2, activePaint2)
        canvas.drawPath(path3, activePaint3)
        canvas.drawPath(path4, activePaint4)
        canvas.drawPath(pathicon1, activeiconPaint1)
        canvas.drawPath(pathicon2, activeiconPaint2)
        canvas.drawPath(pathicon3, activeiconPaint3)
        canvas.drawPath(pathicon4, activeiconPaint4)
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
                    activePaint1 = pathDownPaint1
                    activeiconPaint1 = pathiconDownPaint1
                    Log.d("DpadView", "Left Down=== $actionId")
                }
                if (region2.contains(touchX.toInt(), touchY.toInt()) && !activePaths.containsValue(path2)) {
                    activePaths[actionId] = path2
                    activePaint2 = pathDownPaint2
                    activeiconPaint2 = pathiconDownPaint2
                    Log.d("DpadView", "Top Down=== $actionId")
                }
                if (region3.contains(touchX.toInt(), touchY.toInt()) && !activePaths.containsValue(path3)) {
                    activePaths[actionId] = path3
                    activePaint3 = pathDownPaint3
                    activeiconPaint3 = pathiconDownPaint3
                    Log.d("DpadView", "Right Down=== $actionId")
                }
                if (region4.contains(touchX.toInt(), touchY.toInt()) && !activePaths.containsValue(path4)) {
                    activePaths[actionId] = path4
                    activePaint4 = pathDownPaint4
                    activeiconPaint4 = pathiconDownPaint4
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
                    activePaint1 = pathDownPaint1
                    activeiconPaint1 = pathiconDownPaint1
                    Log.d("DpadView", "moving left=== $actionId")
                }
                if (region2.contains(touchX.toInt(), touchY.toInt()) && previousPath != path2) {
                    actionUp(actionId)
                    activePaths[actionId]=path2
                    previousPath = path2
                    activePaint2 = pathDownPaint2
                    activeiconPaint2 = pathiconDownPaint2
                    Log.d("DpadView", "moving top=== $actionId")
                }
                if (region3.contains(touchX.toInt(), touchY.toInt()) && previousPath != path3) {
                    actionUp(actionId)
                    activePaths[actionId]=path3
                    previousPath = path3
                    activePaint3 = pathDownPaint3
                    activeiconPaint3 = pathiconDownPaint3
                    Log.d("DpadView", "moving right=== $actionId")
                }
                if (region4.contains(touchX.toInt(), touchY.toInt()) && previousPath != path4) {
                    actionUp(actionId)
                    activePaths[actionId]=path4
                    previousPath = path4
                    activePaint4 = pathDownPaint4
                    activeiconPaint4 = pathiconDownPaint4
                    Log.d("DpadView", "moving bottom=== $actionId")
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                actionUp(actionId)
            }

            MotionEvent.ACTION_CANCEL -> {
                activePaint1 = pathUpPaint1
                activePaint2 = pathUpPaint2
                activePaint3 = pathUpPaint3
                activePaint4 = pathUpPaint4
                activeiconPaint1 = pathiconUpPaint1
                activeiconPaint2 = pathiconUpPaint2
                activeiconPaint3 = pathiconUpPaint3
                activeiconPaint4 = pathiconUpPaint4
                activePaths.clear()
            }
        }
        invalidate()
        return true
    }

    private fun actionUp(actionId: Int){
        Log.d("DpadView", "UP===$actionId")
        if (activePaths[actionId] == path1) {
            activePaint1 = pathUpPaint1
            activeiconPaint1 = pathiconUpPaint1
            Log.d("DpadView", "Left Up $actionId")
        }
        if (activePaths[actionId] == path2) {
            activePaint2 = pathUpPaint2
            activeiconPaint2 = pathiconUpPaint2
            Log.d("DpadView", "Top Up $actionId")
        }
        if (activePaths[actionId] == path3) {
            activePaint3 = pathUpPaint3
            activeiconPaint3 = pathiconUpPaint3
            Log.d("DpadView", "Right Up $actionId")
        }
        if (activePaths[actionId] == path4) {
            activePaint4 = pathUpPaint4
            activeiconPaint4 = pathiconUpPaint4
            Log.d("DpadView", "Bottom Up $actionId")
        }
        activePaths.remove(actionId)
    }
}