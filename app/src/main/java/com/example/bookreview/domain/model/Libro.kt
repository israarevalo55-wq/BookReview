package com.example.bookreview.domain.model

/**
 * Modelo de dominio de un libro, tal como lo necesita la UI.
 *
 * Hoy [com.example.bookreview.data.repository.LibroRepositoryImpl] lo llena
 * con datos mock. La próxima semana pasará a construirse a partir del JSON
 * de Open Library (un DTO en data/remote se mapea a este modelo dentro del
 * repositorio) sin que la UI ni el ViewModel de Búsqueda cambien.
 */
data class Libro(
    val id: String,
    val titulo: String,
    val autor: String,
    val anioPublicacion: Int? = null,
    val portadaUrl: String? = null
)
