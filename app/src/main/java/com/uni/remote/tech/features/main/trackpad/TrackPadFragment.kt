package com.uni.remote.tech.features.main.trackpad

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import com.uni.remote.tech.base.BaseFragment
import com.uni.remote.tech.databinding.FragmentTrackPadBinding
import com.uni.remote.tech.features.main.MainViewModel


class TrackPadFragment : BaseFragment<FragmentTrackPadBinding, MainViewModel>() {
    override val viewModel: MainViewModel = MainViewModel()

    private var isDown = false
    private var isMoving = false
    private var isScroll = false
    private var startX = 0f
    private var startY = 0f
    override fun setupViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTrackPadBinding.inflate(inflater)

    @SuppressLint("ClickableViewAccessibility")
    override fun init(savedInstanceState: Bundle?) {
        binding.imvTrackPad.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    isDown = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - startX
                    val dy = event.y - startY

                    if (isScroll) {
                        // Vertical scroll (2-finger OR long vertical drag)
                        viewModel.lgConnectManager.getMouseControl()?.scroll(0.0,
                            dy.toDouble()
                        )
                    } else {
                        // Normal relative move
                        viewModel.lgConnectManager.getMouseControl()?.move(dx.toDouble(), dy.toDouble())
                    }

                    startX = event.x
                    startY = event.y
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isDown && !isMoving) {
                        // Treat as click
                        viewModel.lgConnectManager.getMouseControl()?.click()
                    }
                    isDown = false
                    isMoving = false
                    isScroll = false
                    true
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    // Detect 2-finger gesture → switch to scroll mode
                    if (event.pointerCount == 2) {
                        isScroll = true
                    }
                    true
                }
                else -> false
            }
        }

        binding.imvTrackPad.setClickable(true)
        binding.imvTrackPad.setFocusable(true)
    }
}