package com.indus.veena.crash

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.indus.veena.MainActivity
import com.indus.veena.R
import com.indus.veena.ui.theme.VeenaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** No Hilt, Media3, addon loading, or network dependencies in the recovery UI. */
class CrashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VeenaTheme {
                val report by produceState<String?>(null) {
                    value = withContext(Dispatchers.IO) {
                        CrashRecovery.read(this@CrashActivity).also { CrashRecovery.consume(this@CrashActivity) }
                    }
                }
                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.safeDrawingPadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.crash_title), style = MaterialTheme.typography.headlineMedium)
                        Text(stringResource(R.string.crash_explanation), style = MaterialTheme.typography.bodyMedium)
                        Surface(Modifier.weight(1f).fillMaxWidth(), shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surfaceContainer) {
                            SelectionContainer {
                                Text(report ?: stringResource(R.string.loading_report), fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp))
                            }
                        }
                        Button(enabled = !report.isNullOrBlank(), onClick = {
                            copy(report.orEmpty())
                            val summary = report.orEmpty().lineSequence().firstOrNull { it.contains("Exception") && it != "Exception and causes" }.orEmpty()
                            val body = getString(R.string.crash_issue_body, summary.take(300))
                            open("https://github.com/indusAryan/Veena/issues/new".toUri().buildUpon()
                                .appendQueryParameter("title", "Crash: ${summary.take(100)}")
                                .appendQueryParameter("body", body).build().toString())
                        }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.crash_github)) }
                        FilledTonalButton(enabled = !report.isNullOrBlank(), onClick = {
                            copy(report.orEmpty())
                            open("https://t.me/VeenaApp")
                            Toast.makeText(this@CrashActivity, R.string.crash_paste_telegram, Toast.LENGTH_LONG).show()
                        }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.crash_telegram)) }
                        OutlinedButton(onClick = {
                            CrashRecovery.consume(this@CrashActivity)
                            startActivity(Intent(this@CrashActivity, MainActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                            finish()
                        }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.restart_app)) }
                    }
                }
            }
        }
    }
    private fun copy(report: String) {
        getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText(getString(R.string.crash_log), report))
    }
    private fun open(url: String) {
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
            .onFailure { Toast.makeText(this, R.string.no_link_handler, Toast.LENGTH_LONG).show() }
    }
}
