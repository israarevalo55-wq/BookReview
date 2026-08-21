package com.example.bookreview.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.example.bookreview.BookReviewApplication
import com.example.bookreview.ui.navigation.BookReviewNavGraph
import com.example.bookreview.ui.theme.BookReviewTheme

@Composable
fun BookReviewApp() {
    val context = LocalContext.current
    val container = (context.applicationContext as BookReviewApplication).container

    // El tema se lee directamente del repositorio (Flow -> State) porque es
    // solo una bandera de presentación para toda la app; no justifica un
    // ViewModel propio. AjustesViewModel sí existe porque, además de leerlo,
    // expone la acción de cambiarlo desde la pantalla de Ajustes.
    val modoOscuro by container.settingsRepository.modoOscuro.collectAsState(initial = false)

    BookReviewTheme(darkTheme = modoOscuro) {
        BookReviewNavGraph()
    }
}
