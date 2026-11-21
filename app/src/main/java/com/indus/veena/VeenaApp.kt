package com.indus.veena

import android.app.ActivityManager
import android.app.Application
import android.os.Process
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.indus.veena.crash.CrashRecovery
import com.indus.veena.extension.AddonAutoUpdater
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import javax.inject.Provider

@HiltAndroidApp
class VeenaApp: Application(), Configuration.Provider {
    @Inject lateinit var addonAutoUpdater: Provider<AddonAutoUpdater>
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override fun onCreate() {
        val processName = if (android.os.Build.VERSION.SDK_INT >= 28) getProcessName() else {
            (getSystemService(ACTIVITY_SERVICE) as ActivityManager).runningAppProcesses
                ?.firstOrNull { it.pid == Process.myPid() }?.processName.orEmpty()
        }
        val recoveryProcess = processName.endsWith(":crash")
        if (!recoveryProcess) CrashRecovery.install(this)
        super.onCreate()
        if (!recoveryProcess) addonAutoUpdater.get().start(applicationScope)
    }
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    override val workManagerConfiguration: Configuration get() =
        Configuration.Builder().setWorkerFactory(workerFactory).build()
}