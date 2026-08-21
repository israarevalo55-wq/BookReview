package com.example.bookreview.ui.search

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
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
import com.example.bookreview.ui.common.IndicadorDeCarga
import com.example.bookreview.ui.common.MensajeDeError

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

            when (val resultado = uiState.resultado) {
                ResultadoBusqueda.Inicial -> {
                    Text("Escribe un título o autor y toca la lupa para buscar")
                }
                ResultadoBusqueda.Cargando -> IndicadorDeCarga()
                is ResultadoBusqueda.Error -> {
                    MensajeDeError(mensaje = resultado.mensaje, onReintentar = viewModel::buscar)
                }
                is ResultadoBusqueda.Exito -> {
                    if (resultado.resultados.isEmpty()) {
                        Text("Sin resultados para \"${uiState.query}\"")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(resultado.resultados, key = { it.id }) { libro ->
                                LibroItem(libro = libro, onClick = { onLibroClick(libro.id) })
                            }
                        }
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
            Column(modifier = Modifier.weight(1f)) {
                Text(libro.titulo, style = MaterialTheme.typography.titleMedium)
                Text(libro.autor, style = MaterialTheme.typography.bodyMedium)
                libro.anioPublicacion?.let {
                    Text(it.toString(), style = MaterialTheme.typography.bodySmall)
                }
            }
            // libro.esFavorito lo llena BuscarLibrosConFavoritosUseCase
            // cruzando este resultado de la API con las reseñas guardadas
            // en Room; LibroRepositoryImpl/la API nunca lo tocan. El color
            // sale del Theme (colorScheme.primary), igual que en Detalle y
            // en Mis Reseñas: el mismo ícono de favorito se ve igual en
            // toda la app.
            if (libro.esFavorito) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Ya tiene reseña guardada como favorito",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
