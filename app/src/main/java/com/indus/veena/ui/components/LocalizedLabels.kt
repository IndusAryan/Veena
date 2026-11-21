package com.indus.veena.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.indus.veena.R
import com.indus.veena.database.DataStoreKeys
import com.indus.veena.database.sqlite.entities.DownloadState

@Composable
fun DataStoreKeys.AppTheme.localizedLabel(): String = stringResource(when (this) {
    DataStoreKeys.AppTheme.SYSTEM -> R.string.system_theme
    DataStoreKeys.AppTheme.LIGHT -> R.string.light_theme
    DataStoreKeys.AppTheme.DARK -> R.string.dark_theme
    DataStoreKeys.AppTheme.AMOLED -> R.string.amoled_theme
    DataStoreKeys.AppTheme.SYSTEM_AMOLED -> R.string.system_amoled_theme
})

@Composable
fun DataStoreKeys.AudioQuality.localizedLabel(): String = stringResource(when (this) {
    DataStoreKeys.AudioQuality.HIGH -> R.string.quality_high
    DataStoreKeys.AudioQuality.MEDIUM -> R.string.quality_medium
    DataStoreKeys.AudioQuality.LOW -> R.string.quality_low
})

@Composable
fun DownloadState.localizedLabel(): String = stringResource(when (this) {
    DownloadState.FETCHING -> R.string.preparing
    DownloadState.PENDING -> R.string.download_pending
    DownloadState.DOWNLOADING -> R.string.downloading
    DownloadState.PAUSED -> R.string.download_paused
    DownloadState.COMPLETED -> R.string.downloaded
    DownloadState.FAILED -> R.string.download_failed
})
