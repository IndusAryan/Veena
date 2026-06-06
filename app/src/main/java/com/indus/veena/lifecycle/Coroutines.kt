package com.indus.veena.lifecycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun ViewModel.ioScope(block: suspend () -> Unit) = viewModelScope.launch(Dispatchers.IO) { block() }