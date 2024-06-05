package com.aryan.veena.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.aryan.veena.R
import com.aryan.veena.databinding.ActivityQractivityBinding
import com.aryan.veena.helpers.PermissionHelper.isGranted
import com.aryan.veena.helpers.PermissionHelper.request
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode

class QRActivity : AppCompatActivity() {

    private val TAG = "QRActivity"
    private lateinit var binding: ActivityQractivityBinding
    private val scanQrCode = registerForActivityResult(ScanQRCode(), ::handleResult)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQractivityBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_qractivity)

        if (!isGranted(this, Manifest.permission.CAMERA)) {
            request(this@QRActivity, application, Manifest.permission.CAMERA)
        }
        else {
            scanQrCode.launch(null)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun handleResult(result: QRResult) {
        when (result) {
            is QRResult.QRSuccess -> {
                //Toast.makeText(applicationContext, result.content.rawValue, LENGTH_SHORT).show()
                result.content.rawValue ?: result.content.rawBytes?.let { String(it) }.orEmpty()
                val scannedText = result.content.rawValue ?: return
                val intent = Intent().apply {
                    putExtra("SCANNED_TEXT", scannedText)
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
            QRResult.QRUserCanceled -> {
                Log.i(TAG, "User canceled")
                finish()
            }
            QRResult.QRMissingPermission -> Log.i(TAG, "Missing permission")
            is QRResult.QRError -> "${result.exception.javaClass.simpleName}: ${result.exception.localizedMessage}"
        }
    }
}