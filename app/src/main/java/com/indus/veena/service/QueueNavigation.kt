package com.indus.veena.service

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Singleton
class QueueNavigation @Inject constructor() {
    private val _steps = MutableSharedFlow<Int>(extraBufferCapacity = 8)
    val steps = _steps.asSharedFlow()

    fun moveBy(step: Int) {
        _steps.tryEmit(step)
    }

    companion object {
        const val PREVIOUS = "com.indus.veena.PREVIOUS_TRACK"
        const val NEXT = "com.indus.veena.NEXT_TRACK"
    }
}
