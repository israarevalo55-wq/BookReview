package com.example.bookreview.domain.model

/**
 * Modelo de dominio de una reseña.
 *
 * Es una clase Kotlin "pura": no tiene anotaciones de Room (@Entity,
 * @PrimaryKey) ni nada de Retrofit/Gson. ui/ y domain/ programan contra
 * esta clase, nunca contra ResenaEntity. Así, si el día de mañana cambiamos
 * Room por otra solución de almacenamiento, solo se toca data/ — el
 * ViewModel y las pantallas quedan intactos.
 */
data class Resena(
    val id: Long = 0L,
    val libroId: String,
    val titulo: String,
    val autor: String,
    val rating: Float,
    val texto: String,
    val fotoUri: String? = null,
    val esFavorito: Boolean = false
)
