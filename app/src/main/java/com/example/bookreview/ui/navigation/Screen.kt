package com.example.bookreview.ui.navigation

import android.net.Uri

/** Nombre del argumento de ruta que recibe la pantalla de Detalle. */
const val ARG_LIBRO_ID = "libroId"

/**
 * Rutas de navegación centralizadas en un solo lugar, en vez de strings
 * sueltos repartidos por las pantallas. Detalle es la única con argumento
 * (libroId), que Navigation Compose entrega al ViewModel vía SavedStateHandle.
 */
sealed class Screen(val route: String) {
    data object Busqueda : Screen("busqueda")
    data object MisResenas : Screen("mis_resenas")
    data object Ajustes : Screen("ajustes")

    data object Detalle : Screen("detalle/{$ARG_LIBRO_ID}") {
        // El "key" de Open Library viene con barras, p.ej. "/works/OL27448W".
        // Sin codificar, "detalle/$libroId" quedaría "detalle//works/OL27448W"
        // (doble barra) y Navigation Compose no lo matchea contra
        // "detalle/{libroId}" -> crash. Uri.encode lo deja como un solo
        // segmento seguro (p.ej. "%2Fworks%2FOL27448W").
        fun createRoute(libroId: String) = "detalle/${Uri.encode(libroId)}"
    }
}
