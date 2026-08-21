package com.example.bookreview.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bookreview.domain.model.Libro
import com.example.bookreview.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusquedaScreen(
    onLibroClick: (String) -> Unit,
    viewModel: BusquedaViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Buscar libros") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = viewModel::onQueryChange,
                    label = { Text("Título o autor") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = viewModel::buscar) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                }
            }

            Spacer(Modifier.height(12.dp))

            when {
                uiState.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                uiState.yaBusco && uiState.resultados.isEmpty() -> {
                    Text("Sin resultados para \"${uiState.query}\"")
                }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.resultados, key = { it.id }) { libro ->
                        LibroItem(libro = libro, onClick = { onLibroClick(libro.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun LibroItem(libro: Libro, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = libro.portadaUrl,
                contentDescription = libro.titulo,
                modifier = Modifier.size(56.dp, 84.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(libro.titulo, style = MaterialTheme.typography.titleMedium)
                Text(libro.autor, style = MaterialTheme.typography.bodyMedium)
                libro.anioPublicacion?.let {
                    Text(it.toString(), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
