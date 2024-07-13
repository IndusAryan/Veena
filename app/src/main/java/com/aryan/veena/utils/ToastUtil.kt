package com.aryan.veena.utils

import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.StringRes
import com.aryan.veena.utils.CoroutineUtils.mainScope

object ToastUtil {
    fun showToast(context: Context, message: Int, duration: Int? = LENGTH_SHORT) {
       mainScope {
            Toast.makeText(context, message, duration!!).show()
        }
    }
}