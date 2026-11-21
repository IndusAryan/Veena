package com.indus.veena.crash

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.AtomicFile
import com.indus.veena.BuildConfig
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

object CrashRecovery {
    private fun report(context: Context) = File(context.filesDir, "crashes/latest.txt")
    private fun pending(context: Context) = File(context.filesDir, "crashes/pending")
    fun hasPending(context: Context) = pending(context).exists() && report(context).isFile
    fun consume(context: Context) {
        pending(context).delete()
    }

    fun read(context: Context): String = runCatching {
        AtomicFile(report(context)).openRead().bufferedReader().use { it.readText() }
    }.getOrDefault("")

    fun open(context: Context) {
        context.startActivity(
            Intent(
                context,
                CrashActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    fun install(app: Application) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        val handling = AtomicBoolean(false)
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            if (handling.compareAndSet(false, true)) {
                runCatching {
                    val text = CrashReport.format(
                        error,
                        thread.name,
                        "App: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n" + "Device: ${Build.MANUFACTURER} ${Build.MODEL}\nAndroid: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                        java.time.Instant.now().toString()
                    )
                    val file = report(app)
                    file.parentFile?.mkdirs()
                    val atomic = AtomicFile(file)
                    val output = atomic.startWrite()
                    try {
                        output.write(text.toByteArray(Charsets.UTF_8)); atomic.finishWrite(output)
                    } catch (failure: Throwable) {
                        atomic.failWrite(output); throw failure
                    }
                    pending(app).writeText("pending")
                    // Separate process survives termination of the failed player/application process.
                    open(app)
                }
            }
            try {
                previous?.uncaughtException(thread, error)
            } finally {
                android.os.Process.killProcess(android.os.Process.myPid()); kotlin.system.exitProcess(
                    10
                )
            }
        }
    }
}
