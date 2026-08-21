package com.example.bookreview.ui.navigation

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
        fun createRoute(libroId: String) = "detalle/$libroId"
    }
}
