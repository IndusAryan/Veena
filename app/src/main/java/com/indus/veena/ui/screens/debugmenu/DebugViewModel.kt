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
    private val backupRestoreManager: BackupRestoreManager
) : ViewModel() {
    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState

    fun exportData(uri: android.net.Uri) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val result = backupRestoreManager.exportData(uri)
            _operationState.value = if (result.isSuccess) {
                OperationState.Success("Backup Saved Successfully")
            } else {
                OperationState.Error(result.exceptionOrNull()?.message ?: "Backup Failed")
            }
        }
    }

    fun importData(uri: android.net.Uri) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val result = backupRestoreManager.importData(uri)
            _operationState.value = if (result.isSuccess) {
                OperationState.Success("Data Restored Successfully")
            } else {
                OperationState.Error(result.exceptionOrNull()?.message ?: "Restore Failed")
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