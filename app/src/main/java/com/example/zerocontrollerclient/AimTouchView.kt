package com.example.zerocontrollerclient

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.concurrent.atomic.AtomicBoolean

class AimTouchView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    interface AimTouchListener {
        fun onAimTouchMoveMacro(aimTouchX: Float, aimTouchY: Float)
    }

    private var aimTouchListener: AimTouchListener? = null

    fun setAimTouchListener(listener: AimTouchListener) {
        aimTouchListener = listener
    }

    // ─── Constants ──────────────────────────────────────────────────────────
    private val MAX_JOYSTICK = 32767f
    // If no movement is detected for this long, target snaps to 0 (finger is stationary)
    // Increased to 40ms to avoid stuttering on 60Hz/120Hz displays
    private val STOP_TIMEOUT_MS = 40L
    // Minimum output magnitude so slow drags always register in the game
    private val MIN_JOY_OUTPUT = 8000f

    // ─── State (volatile: written by UI thread, read by loop thread) ─────────
    @Volatile private var targetJoyX = 0f
    @Volatile private var targetJoyY = 0f
    @Volatile private var lastMoveTime = 0L
    @Volatile private var sensitivity = 1800f

    // Touch tracking — UI thread only
    private var isActive = false
    private var lastX = 0f
    private var lastY = 0f

    // Last value sent — only on loop thread, no sync needed
    private var lastSentX = 0f
    private var lastSentY = 0f

    // ─── 250 Hz output thread ────────────────────────────────────────────────
    private val running = AtomicBoolean(false)
    private var loopThread: Thread? = null

    private fun startLoop() {
        loopThread = Thread {
            val intervalNs = 4_000_000L  // 4ms = ~250 Hz

            while (running.get()) {
                val loopStart = System.nanoTime()

                // If finger is still but hasn't moved for STOP_TIMEOUT_MS → force 0
                val mt = lastMoveTime
                if (mt > 0L && (System.currentTimeMillis() - mt) > STOP_TIMEOUT_MS) {
                    targetJoyX = 0f
                    targetJoyY = 0f
                }

                if (!isActive) {
                    // Eliminate post-lift cursor drift by immediately clearing
                    targetJoyX = 0f
                    targetJoyY = 0f
                }

                val cx = targetJoyX
                val cy = targetJoyY

                // Always relay the current target at 250 Hz, BUT only if the integer value changed
                // This prevents TCP buffer bloat causing 10-15s delayed stick movement
                if (kotlin.math.abs(cx) > 0.5f || kotlin.math.abs(cy) > 0.5f) {
                    if (cx.toInt() != lastSentX.toInt() || cy.toInt() != lastSentY.toInt()) {
                        lastSentX = cx
                        lastSentY = cy
                        aimTouchListener?.onAimTouchMoveMacro(cx, cy)
                    }
                } else if (lastSentX != 0f || lastSentY != 0f) {
                    // Target just became 0 — send the reset packet exactly once
                    lastSentX = 0f
                    lastSentY = 0f
                    aimTouchListener?.onAimTouchMoveMacro(0f, 0f)
                }

                val elapsedNs = System.nanoTime() - loopStart
                val sleepNs = intervalNs - elapsedNs
                if (sleepNs > 0) {
                    val sleepMs = sleepNs / 1_000_000L
                    val sleepNano = (sleepNs % 1_000_000L).toInt()
                    Thread.sleep(sleepMs, sleepNano)
                }
            }
        }.also {
            it.name = "AimTouchLoop"
            it.priority = Thread.MAX_PRIORITY
            it.isDaemon = true
            it.start()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val baseSens = context.getSharedPreferences("selected_macros", Context.MODE_PRIVATE)
            .getFloat("sensitivity", 50f).coerceIn(1f, 100f)
        sensitivity = baseSens * 36f
        running.set(true)
        startLoop()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        running.set(false)
        loopThread?.interrupt()
        loopThread = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                isActive = true
                lastX = event.x
                lastY = event.y
                lastMoveTime = System.currentTimeMillis()
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isActive) return true

                // Use the native event interval delta instead of slicing into micro-historical deltas
                processMove(event.x, event.y)
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_CANCEL -> {
                isActive = false
                // Snap target to 0; loop thread will send (0,0) on its next tick (~4ms)
                targetJoyX = 0f
                targetJoyY = 0f
                lastMoveTime = 0L
            }
        }
        return true
    }

    private fun processMove(x: Float, y: Float) {
        val deltaX = x - lastX
        val deltaY = y - lastY  // negate below — finger down = look down = negative Y

        lastX = x
        lastY = y

        val rawX = deltaX * sensitivity
        val rawY = -deltaY * sensitivity  // Y-axis fix

        var joyX = rawX.coerceIn(-MAX_JOYSTICK, MAX_JOYSTICK)
        var joyY = rawY.coerceIn(-MAX_JOYSTICK, MAX_JOYSTICK)

        // Apply linear Anti-Deadzone scaling to preserve perfect accurate aiming
        val TOUCH_JITTER = 100f
        val magnitude = kotlin.math.sqrt(joyX * joyX + joyY * joyY)
        
        if (magnitude > TOUCH_JITTER) {
            val mag = magnitude.coerceAtMost(MAX_JOYSTICK)
            val t = (mag - TOUCH_JITTER) / (MAX_JOYSTICK - TOUCH_JITTER)
            val outputMag = MIN_JOY_OUTPUT + t * (MAX_JOYSTICK - MIN_JOY_OUTPUT)
            val scale = outputMag / magnitude
            joyX *= scale
            joyY *= scale
        } else {
            joyX = 0f
            joyY = 0f
        }

        targetJoyX = joyX
        targetJoyY = joyY
        lastMoveTime = System.currentTimeMillis()
    }
}