package com.uni.remote.tech.common.extension.flow

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapConcat

@FlowPreview
fun <T> triggerFlow(trigger: Trigger, flowProvider: () -> Flow<T>) =
    trigger.triggerEvent
        .flatMapConcat { flowProvider.invoke() }

fun triggerFlow(trigger: Trigger) = trigger.triggerEvent
    .mapToUnit()

class Trigger {
    val triggerEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 64)

    fun trigger() {
        triggerEvent.tryEmit(Unit)
    }
}
