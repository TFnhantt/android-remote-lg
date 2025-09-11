package com.uni.remote.tech.features.main.trackpad

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import com.uni.remote.tech.base.BaseFragment
import com.uni.remote.tech.databinding.FragmentTrackPadBinding
import com.uni.remote.tech.features.main.MainViewModel
import java.util.Timer
import java.util.TimerTask
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt


class TrackPadFragment : BaseFragment<FragmentTrackPadBinding, MainViewModel>() {
    override val viewModel: MainViewModel = MainViewModel()

    private var isDown = false
    private var isMoving = false
    private var isScroll = false
    private var startX = 0f
    private var startY = 0f
    private var lastX = Float.NaN
    private var lastY = Float.NaN
    private var scrollDx = 0
    private var scrollDy: Int = 0
    private var eventStart: Long = 0
    private var timer = Timer()
    private var autoScrollTimerTask: TimerTask? = null
    override fun setupViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTrackPadBinding.inflate(inflater)

    @SuppressLint("ClickableViewAccessibility")
    override fun init(savedInstanceState: Bundle?) {
        binding.imvTrackPad.setOnTouchListener { _, motionEvent ->
            var dx = 0f
            var dy = 0f
            val wasMoving: Boolean = isMoving
            val wasScroll: Boolean = isScroll
            isScroll = isScroll || motionEvent.pointerCount > 1
            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    isDown = true
                    eventStart = motionEvent.eventTime
                    startX = motionEvent.x
                    startY = motionEvent.y
                }

                MotionEvent.ACTION_UP -> {
                    isDown = false
                    isMoving = false
                    isScroll = false
                    lastX = Float.NaN
                    lastY = Float.NaN
                }
            }
            if (!lastX.isNaN() || !lastY.isNaN()) {
                dx = (motionEvent.x - lastX).roundToInt().toFloat()
                dy = (motionEvent.y - lastY).roundToInt().toFloat()
            }
            lastX = motionEvent.x
            lastY = motionEvent.y
            val xDistFromStart: Float = abs(motionEvent.x - startX)
            val yDistFromStart: Float = abs(motionEvent.y - startY)
            if (isDown && !isMoving) {
                if (xDistFromStart > 1 && yDistFromStart > 1) {
                    isMoving = true
                }
            }
            if (isDown && isMoving) {
                if (dx != 0f && dy != 0f) {
                    // Scale dx and dy to simulate acceleration
                    val dxSign = if (dx >= 0) 1 else -1
                    val dySign = if (dy >= 0) 1 else -1
                    dx = dxSign * abs(dx).toDouble().pow(1.1).roundToInt().toFloat()
                    dy = dySign * abs(dy).toDouble().pow(1.1).roundToInt().toFloat()
                    if (!isScroll) {
                        viewModel.lgConnectManager.getMouseControl()
                            ?.move(dx.toDouble(), dy.toDouble())
                    } else {
                        val now = SystemClock.uptimeMillis()
                        scrollDx = (motionEvent.x - startX).toInt()
                        scrollDy = (motionEvent.y - startY).toInt()
                        if (now - eventStart > 1000 && autoScrollTimerTask == null) {
                            autoScrollTimerTask = object : TimerTask() {
                                override fun run() {
                                    viewModel.lgConnectManager.getMouseControl()
                                        ?.scroll(scrollDx.toDouble(), scrollDy.toDouble())
                                }
                            }
                            timer.schedule(autoScrollTimerTask, 100, 750)
                        }
                    }
                }
            } else if (!isDown && !wasMoving) {
                viewModel.lgConnectManager.getMouseControl()?.click()
            } else if (!isDown && wasMoving && wasScroll) {
                dx = motionEvent.x - startX
                dy = motionEvent.y - startY
                viewModel.lgConnectManager.getMouseControl()
                    ?.scroll(dx.toDouble(), dy.toDouble())
            }
            if (!isDown) {
                isMoving = false
                if (autoScrollTimerTask != null) {
                    autoScrollTimerTask!!.cancel()
                    autoScrollTimerTask = null
                }
            }
            true
        }
    }
}