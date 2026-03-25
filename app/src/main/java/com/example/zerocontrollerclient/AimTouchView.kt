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
    private val STOP_TIMEOUT_MS = 20L
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
            val intervalMs = 4L  // ~250 Hz
            while (running.get()) {
                val loopStart = System.nanoTime()

                // If finger is still but hasn't moved for STOP_TIMEOUT_MS → force 0
                val mt = lastMoveTime
                if (mt > 0L && (System.currentTimeMillis() - mt) > STOP_TIMEOUT_MS) {
                    targetJoyX = 0f
                    targetJoyY = 0f
                }

                val tx = targetJoyX
                val ty = targetJoyY

                // Always relay the current target at 250 Hz.
                // When transitioning to (0,0) make sure it is sent at least once.
                if (tx != 0f || ty != 0f) {
                    lastSentX = tx
                    lastSentY = ty
                    aimTouchListener?.onAimTouchMoveMacro(tx, ty)
                } else if (lastSentX != 0f || lastSentY != 0f) {
                    // Target just became 0 — send the reset packet exactly once
                    lastSentX = 0f
                    lastSentY = 0f
                    aimTouchListener?.onAimTouchMoveMacro(0f, 0f)
                }
                // If both are already 0 and we already sent reset → do nothing (idle)

                val elapsedMs = (System.nanoTime() - loopStart) / 1_000_000L
                val sleepMs = intervalMs - elapsedMs
                if (sleepMs > 0) Thread.sleep(sleepMs)
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
        sensitivity = context.getSharedPreferences("selected_macros", Context.MODE_PRIVATE)
            .getFloat("sensitivity", 1800f)
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

                // Consume all batched historical points so no pixel is missed
                val histCount = event.historySize
                for (h in 0 until histCount) {
                    processMove(event.getHistoricalX(h), event.getHistoricalY(h))
                }
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

        // Boost slow movements so the game always detects them
        if (joyX != 0f && kotlin.math.abs(joyX) < MIN_JOY_OUTPUT)
            joyX = kotlin.math.sign(joyX) * MIN_JOY_OUTPUT
        if (joyY != 0f && kotlin.math.abs(joyY) < MIN_JOY_OUTPUT)
            joyY = kotlin.math.sign(joyY) * MIN_JOY_OUTPUT

        targetJoyX = joyX
        targetJoyY = joyY
        lastMoveTime = System.currentTimeMillis()
    }
}