package com.uni.remote.tech.common.premium

import kotlinx.coroutines.flow.StateFlow

interface IPremiumManager {
    fun updateUnlockByCodeState(isUnlocked: Boolean)
    fun updateSubscribedState(isSubscribed: Boolean)
    fun isSubscribed(): Boolean
    fun getSubscribedStateFlow(): StateFlow<Boolean>
}