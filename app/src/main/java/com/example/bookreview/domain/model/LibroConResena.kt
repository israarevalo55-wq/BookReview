package com.example.bookreview.domain.model

/**
 * Resultado de combinar un [Libro] (remoto, Open Library) con la [Resena]
 * local que el usuario ya haya guardado para ese mismo libroId (Room), si
 * existe. Es el modelo que arma [com.example.bookreview.domain.usecase.ObtenerLibroConResenaUseCase]
 * y el único que la pantalla de Detalle necesita para saber qué mostrar.
 */
data class LibroConResena(
    val libro: Libro,
    val resena: Resena? // null = todavía no hay reseña guardada para este libro
)
