package com.uni.remote.tech.common.utils.timber

import timber.log.Timber

class DebugLogTree : Timber.DebugTree() {

    override fun createStackElementTag(element: StackTraceElement): String {
        return String.format(
            ">>> %s:%s",
            super.createStackElementTag(element),
            element.lineNumber
        )
    }
}
