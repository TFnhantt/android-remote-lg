package com.uni.remote.tech.common.extension.nav

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController

/**
 *
 * Created by tamle on 09/06/2023
 *
 */

fun Fragment.safeNavigate(@IdRes actionId: Int, bundle: Bundle? = null) {
    val controller = findNavController()
    val currentDestination =
        (controller.currentDestination as? FragmentNavigator.Destination)?.className
            ?: (controller.currentDestination as? DialogFragmentNavigator.Destination)?.className
    if (currentDestination == this.javaClass.name) {
        controller.navigate(actionId, bundle)
    }
}

fun Fragment.safeNavigateUp() {
    findNavController().navigateUp()
}