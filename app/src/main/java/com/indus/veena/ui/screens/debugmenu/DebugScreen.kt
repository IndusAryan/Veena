package com.indus.veena.ui.screens.debugmenu

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.indus.veena.ui.screens.home.HomeViewModel

@Composable
fun DebugBackupScreen(
    viewModel: DebugViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val operationState by viewModel.operationState.collectAsState()

    // Launcher for Saving File (Export)
    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportData(it) }
    }

    // Launcher for Opening File (Import)
    val openLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importData(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Debug Data Management", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(20.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        saveLauncher.launch("veena_backup_${System.currentTimeMillis()}.json")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Backup Data (Export to JSON)")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        openLauncher.launch(arrayOf("application/json"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text("Restore Data (Import from JSON)")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Status Indicator
        when (val state = operationState) {
            is DebugViewModel.OperationState.Loading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Processing...")
            }
            is DebugViewModel.OperationState.Success -> {
                Text(state.message, color = MaterialTheme.colorScheme.primary)
            }
            is DebugViewModel.OperationState.Error -> {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}