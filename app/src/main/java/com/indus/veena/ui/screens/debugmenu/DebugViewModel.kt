package com.indus.veena.ui.screens.debugmenu

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indus.veena.database.BackupRestoreManager
import com.indus.veena.database.sqlite.VeenaDB
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val backupRestoreManager: BackupRestoreManager
) : ViewModel() {
    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState

    fun exportData(uri: android.net.Uri) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val result = backupRestoreManager.exportData(uri)
            _operationState.value = if (result.isSuccess) {
                OperationState.Success(context.getString(com.indus.veena.R.string.backup_saved))
            } else {
                OperationState.Error(result.exceptionOrNull()?.message ?: context.getString(com.indus.veena.R.string.backup_failed))
            }
        }
    }

    fun importData(uri: android.net.Uri) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val result = backupRestoreManager.importData(uri)
            _operationState.value = if (result.isSuccess) {
                OperationState.Success(context.getString(com.indus.veena.R.string.restore_saved))
            } else {
                OperationState.Error(result.exceptionOrNull()?.message ?: context.getString(com.indus.veena.R.string.restore_failed))
            }
        }
    }

    sealed class OperationState {
        object Idle : OperationState()
        object Loading : OperationState()
        data class Success(val message: String) : OperationState()
        data class Error(val message: String) : OperationState()
    }
}