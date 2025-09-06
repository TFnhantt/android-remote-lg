package com.uni.remote.tech.common.premium

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PremiumManager private constructor() : IPremiumManager {
    private val _subscribedStateTrigger = MutableStateFlow(value = isSubscribed())

    override fun updateUnlockByCodeState(isUnlocked: Boolean) {
        PremiumPrefs.isUnlockByCode = isUnlocked
        _subscribedStateTrigger.tryEmit(isSubscribed())
    }

    override fun updateSubscribedState(isSubscribed: Boolean) {
        PremiumPrefs.isSubscribed = isSubscribed
        _subscribedStateTrigger.tryEmit(isSubscribed())
    }

    override fun isSubscribed(): Boolean {
        return PremiumPrefs.isSubscribed || PremiumPrefs.isUnlockByCode
    }

    override fun getSubscribedStateFlow(): StateFlow<Boolean> {
        return _subscribedStateTrigger.asStateFlow()
    }

    companion object {
        val INSTANCE: PremiumManager by lazy { PremiumManager() }
    }
}