package com.aryan.veena.ui.activities

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.aryan.veena.R
import com.aryan.veena.databinding.ActivityMainBinding
import com.aryan.veena.helpers.PermissionHelper.isGranted
import com.aryan.veena.helpers.PermissionHelper.request
import com.aryan.veena.helpers.ThemeHelper.applyThemeAndAccent
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
    private val navController by lazy {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container_view) as? NavHostFragment?
        navHostFragment?.navController
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runBlocking {
            applyThemeAndAccent(this@MainActivity)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        binding?.navView?.setupWithNavController(navController ?: return)

        //

        if (Build.VERSION.SDK_INT >= 33) {
            if (!isGranted(this, Manifest.permission.POST_NOTIFICATIONS)) {
                request(this@MainActivity, application, Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}