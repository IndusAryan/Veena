package com.aryan.veena.utils

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

object AppUtils {
    fun setBackPressCallback(fragment: Fragment, onBackPressed: () -> Unit) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
        fragment.activity?.onBackPressedDispatcher?.addCallback(fragment.viewLifecycleOwner, callback)
    }
}