package com.aryan.veena.helpers

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object PermissionHelper {

    fun request(activity: FragmentActivity, context: Context, permission: String) {
        // Check if the permission is already granted.
        if (isGranted(context, permission)) {
            return
        }

        // Create a launcher for requesting permissions.
        val requestPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { _ ->
            // Check if the permission was granted.
            if (!isGranted(context, permission)) {
                // If the permission was not granted, show a toast message to inform the user.
                Toast.makeText(context, "Permission Important",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Ask for the permission.
        requestPermissionLauncher.launch(permission)
    }

    fun isGranted(context: Context, permission: String): Boolean {
        // Check if the permission is granted.
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}