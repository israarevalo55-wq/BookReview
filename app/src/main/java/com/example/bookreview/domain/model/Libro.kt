package com.example.bookreview.domain.model

/**
 * Modelo de dominio de un libro, tal como lo necesita la UI.
 *
 * [com.example.bookreview.data.repository.LibroRepositoryImpl] lo llena a
 * partir del JSON de Open Library — pero solo con lo que la API sabe
 * (título, autor, portada). Open Library no tiene idea de qué libros
 * marcamos como favoritos nosotros: eso vive en Room.
 */
data class Libro(
    val id: String,
    val titulo: String,
    val autor: String,
    val anioPublicacion: Int? = null,
    val portadaUrl: String? = null,
    // OJO: este campo NUNCA lo pone LibroRepositoryImpl/el DTO (la API no lo
    // conoce). Siempre queda en false hasta que un caso de uso
    // (BuscarLibrosConFavoritosUseCase) cruce el id con las reseñas
    // guardadas en Room y lo actualice con .copy(esFavorito = true).
    val esFavorito: Boolean = false
)
