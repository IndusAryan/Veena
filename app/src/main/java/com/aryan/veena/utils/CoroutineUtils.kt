package com.aryan.veena.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object CoroutineUtils {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    /** For activities and fragments **/
    // IO Dispatcher
    fun ioScope(block: suspend CoroutineScope.() -> Unit): Job {
        return coroutineScope.launch(Dispatchers.IO, block = block)
    }

    // Main Dispatcher
    fun mainScope(block: suspend CoroutineScope.() -> Unit): Job {
        return coroutineScope.launch(Dispatchers.Main, block =  block)
    }

    // withContext Dispatcher IO
    suspend fun <T> ioScopeContext(block: suspend CoroutineScope.() -> T): T {
        return withContext(Dispatchers.IO, block = block)
    }

    fun defaultScope(block: suspend CoroutineScope.() -> Unit): Job {
        return coroutineScope.launch(Dispatchers.Default, block =  block)
    }

    /** For view models **/

    // Main Dispatcher
    fun launchMain(viewModel: ViewModel, block: suspend CoroutineScope.() -> Unit): Job {
        return viewModel.viewModelScope.launch(Dispatchers.Main, block = block)
    }

    // IO Dispatcher
    fun launchIO(viewModel: ViewModel, block: suspend CoroutineScope.() -> Unit): Job {
        return viewModel.viewModelScope.launch(Dispatchers.IO, block = block)
    }
}