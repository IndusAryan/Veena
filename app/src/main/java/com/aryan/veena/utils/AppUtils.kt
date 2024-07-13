package com.aryan.veena.utils

import android.app.Activity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import com.aryan.veena.R

object AppUtils {

    fun Activity?.navigate(@IdRes navigation: Int, arguments: Bundle? = null) {
        try {
            if (this is FragmentActivity) {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view) as? NavHostFragment?
                navHostFragment?.navController?.navigate(navigation, arguments)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun setBackPressCallback(fragment: Fragment, onBackPressed: () -> Unit) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
        fragment.activity?.onBackPressedDispatcher?.addCallback(fragment.viewLifecycleOwner, callback)
    }

    /*
    sealed class Resource<out T> {
    data class Success<out T>(val value: T) : Resource<T>()
    data class Failure(
        val isNetworkError: Boolean,
        val errorCode: Int?,
        val errorResponse: Any?, //ResponseBody
        val errorString: String,
    ) : Resource<Nothing>()

    data class Loading(val url: String? = null) : Resource<Nothing>()
}

fun logError(throwable: Throwable) {
    Log.d("ApiError", "-------------------------------------------------------------------")
    Log.d("ApiError", "safeApiCall: " + throwable.localizedMessage)
    Log.d("ApiError", "safeApiCall: " + throwable.message)
    throwable.printStackTrace()
    Log.d("ApiError", "-------------------------------------------------------------------")
}

fun <T> normalSafeApiCall(apiCall: () -> T): T? {
    return try {
        apiCall.invoke()
    } catch (throwable: Throwable) {
        logError(throwable)
        return null
    }
}

suspend fun <T> suspendSafeApiCall(apiCall: suspend () -> T): T? {
    return try {
        apiCall.invoke()
    } catch (throwable: Throwable) {
        logError(throwable)
        return null
    }
}
     */
}