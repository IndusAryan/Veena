package com.indus.veena.lifecycle

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

fun <T> MutableStateFlow<T>.updateState(transform: (T) -> T) {
    this.update(transform)
}
