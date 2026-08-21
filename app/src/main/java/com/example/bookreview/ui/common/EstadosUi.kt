package com.example.bookreview.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Estado "cargando" reutilizado por Búsqueda y Detalle. Un solo lugar para
 * que las dos pantallas usen exactamente el mismo spinner, en vez de que
 * cada una arme el suyo.
 */
@Composable
fun IndicadorDeCarga(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/**
 * Estado "error" reutilizado por Búsqueda y Detalle: mismo ícono, mismo
 * mensaje centrado, mismo botón "Reintentar". Los colores salen del Theme
 * (`MaterialTheme.colorScheme.error`), nunca un color suelto.
 */
@Composable
fun MensajeDeError(
    mensaje: String,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                mensaje,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Button(onClick = onReintentar) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Text("Reintentar", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
