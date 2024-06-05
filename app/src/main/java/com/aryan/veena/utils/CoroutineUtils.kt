package com.aryan.veena.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object CoroutineUtils {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    // For activities and fragments
    fun ioScope(block: suspend CoroutineScope.() -> Unit): Job {
        return coroutineScope.launch(Dispatchers.IO, block = block)
    }

    fun mainScope(block: suspend CoroutineScope.() -> Unit): Job {
        return coroutineScope.launch(Dispatchers.Main, block =  block)
    }

    // For view models
    fun launchMain(viewModel: ViewModel, block: suspend CoroutineScope.() -> Unit): Job {
        return viewModel.viewModelScope.launch(Dispatchers.Main, block = block)
    }

    fun launchIO(viewModel: ViewModel, block: suspend CoroutineScope.() -> Unit): Job {
        return viewModel.viewModelScope.launch(Dispatchers.IO, block = block)
    }
}