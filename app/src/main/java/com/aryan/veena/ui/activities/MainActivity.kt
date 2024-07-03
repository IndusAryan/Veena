package com.aryan.veena.ui.activities

import android.Manifest
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
import com.aryan.veena.utils.CoroutineUtils.ioScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.toastbits.ytmkt.endpoint.SearchEndpoint
import dev.toastbits.ytmkt.endpoint.SearchResults
import dev.toastbits.ytmkt.endpoint.SongFeedEndpoint
import dev.toastbits.ytmkt.endpoint.SongFeedLoadResult
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val client = HttpClient(CIO)

        val navView: BottomNavigationView = binding?.navView ?: return

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