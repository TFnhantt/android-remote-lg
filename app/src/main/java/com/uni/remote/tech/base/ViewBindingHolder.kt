package com.uni.remote.tech.base

import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding

interface ViewBindingHolder<ViewBindingType : ViewBinding> {
    fun initBinding(
        binding: ViewBindingType,
        lifecycle: Lifecycle,
        className: String?,
        onBound: (ViewBindingType.() -> Unit)?
    ): View

    fun requireBinding(block: (ViewBindingType.() -> Unit)? = null): ViewBindingType
}

class ViewBindingHolderImpl<ViewBindingType : ViewBinding> :
    ViewBindingHolder<ViewBindingType>,
    DefaultLifecycleObserver {
    private var binding: ViewBindingType? = null
    private var lifecycle: Lifecycle? = null

    private lateinit var className: String

    override fun requireBinding(block: (ViewBindingType.() -> Unit)?) =
        binding?.apply { block?.invoke(this) }
            ?: throw IllegalStateException("Accessing binding outside of Fragment lifecycle: $className")

    override fun initBinding(
        binding: ViewBindingType,
        lifecycle: Lifecycle,
        className: String?,
        onBound: (ViewBindingType.() -> Unit)?
    ): View {
        this.binding = binding
        this.lifecycle = lifecycle
        this.lifecycle?.addObserver(this)
        this.className = className ?: "N/A"
        onBound?.invoke(binding)
        return binding.root
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        lifecycle?.removeObserver(this)
        lifecycle = null
        binding = null
    }
}
