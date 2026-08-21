package com.example.bookreview.ui.reviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bookreview.domain.model.Resena
import com.example.bookreview.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisResenasScreen(
    onResenaClick: (String) -> Unit,
    viewModel: MisResenasViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val resenas by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Mis reseñas") }) }) { padding ->
        if (resenas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Todavía no escribiste ninguna reseña")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(resenas, key = { it.id }) { resena ->
                    ResenaItem(
                        resena = resena,
                        onClick = { onResenaClick(resena.libroId) },
                        onToggleFavorito = { viewModel.toggleFavorito(resena) },
                        onEliminar = { viewModel.eliminar(resena) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResenaItem(
    resena: Resena,
    onClick: () -> Unit,
    onToggleFavorito: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Miniatura de la foto guardada, si existe (fotoUri sigue
            // siendo null en cualquier reseña creada antes de la Semana 4,
            // así que esto es opcional a propósito).
            resena.fotoUri?.let { fotoUri ->
                AsyncImage(
                    model = fotoUri,
                    contentDescription = "Foto de la reseña de ${resena.titulo}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .padding(end = 12.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(resena.titulo, style = MaterialTheme.typography.titleMedium)
                Text(resena.autor, style = MaterialTheme.typography.bodyMedium)
                Text("★ ${resena.rating} / 5", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onToggleFavorito) {
                Icon(
                    if (resena.esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorito"
                )
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}
