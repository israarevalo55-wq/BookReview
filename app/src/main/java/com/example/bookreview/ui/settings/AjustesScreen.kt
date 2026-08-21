package com.example.bookreview.ui.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bookreview.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    viewModel: AjustesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val modoOscuro by viewModel.modoOscuro.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Ajustes") }) }) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Modo oscuro", modifier = Modifier.weight(1f))
            Switch(checked = modoOscuro, onCheckedChange = viewModel::setModoOscuro)
        }
    }
}
