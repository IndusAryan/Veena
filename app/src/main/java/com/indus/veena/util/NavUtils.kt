package com.indus.veena.util

import android.os.Parcelable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.indus.veena.MainActivity

fun <T : Parcelable> NavController.navigateWithArgs(
    route: Any,
    data: T
) {
    this.navigate(route)
    this.currentBackStackEntry?.savedStateHandle?.set("args", data)
}

fun <T : Parcelable> NavBackStackEntry.collectArgs(): T {
    return savedStateHandle.get<T>("args") ?: throw IllegalStateException("Args missing")
}

fun NavHostController.popCurrentPage() = runCatching {
    (context as MainActivity).onBackPressedDispatcher.onBackPressed()
}.recoverCatching { popBackStack() }