package com.aryan.veena.ui.activities

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.aryan.veena.R
import com.aryan.veena.databinding.ActivityMainBinding
import com.aryan.veena.helpers.PermissionHelper.isGranted
import com.aryan.veena.helpers.PermissionHelper.request
import com.aryan.veena.ui.fragments.PlayerSheetFragment
//import com.aryan.veena.utils.NowPlayingNotification
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_downloads // R.id.navigation_notifications
            )
        )

        //setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        if (Build.VERSION.SDK_INT >= 33) {
            if (!isGranted(this, Manifest.permission.POST_NOTIFICATIONS)) {
                request(this@MainActivity, application, Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        /*if (!isGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            request(this@MainActivity, application, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }*/
    }
}