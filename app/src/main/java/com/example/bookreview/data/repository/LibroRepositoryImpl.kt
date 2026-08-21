package com.example.bookreview.data.repository

import com.example.bookreview.data.remote.LibroDto
import com.example.bookreview.data.remote.OpenLibraryApi
import com.example.bookreview.domain.model.Libro
import com.example.bookreview.domain.repository.LibroRepository

private const val TAMANO_PORTADA = "M" // S / M / L, ver covers.openlibrary.org

/**
 * Implementación real de LibroRepository: llama a Open Library vía Retrofit
 * y mapea LibroDto -> Libro (dominio). La interfaz LibroRepository no
 * cambió, así que BusquedaViewModel y DetalleViewModel siguen exactamente
 * igual que la Semana 1.
 */
class LibroRepositoryImpl(
    private val api: OpenLibraryApi
) : LibroRepository {

    // Open Library no tiene un endpoint "obtener por key" en el mismo
    // formato que /search.json, así que cacheamos en memoria los últimos
    // resultados buscados y getLibroPorId() resuelve desde ahí (por ejemplo,
    // al entrar a Detalle tocando un resultado de Búsqueda). Simplificación
    // a propósito para esta semana: si la app se reinicia y se entra a
    // Detalle directo desde Mis Reseñas, uiState.libro queda en null y la
    // pantalla solo muestra el formulario de reseña, sin la portada/autor.
    // Se puede resolver más adelante agregando un endpoint de detalle
    // (openlibrary.org/{key}.json) cuando toquemos estados de carga/error
    // en la Semana 5.
    private val cache = mutableMapOf<String, Libro>()

    override suspend fun buscarLibros(query: String): List<Libro> {
        if (query.isBlank()) return emptyList()
        val libros = api.buscarLibros(query).docs.map { it.toDomain() }
        libros.forEach { cache[it.id] = it }
        return libros
    }

    override suspend fun getLibroPorId(id: String): Libro? = cache[id]
}

private fun LibroDto.toDomain() = Libro(
    id = key,
    titulo = title ?: "Título desconocido",
    autor = authorName?.joinToString(", ") ?: "Autor desconocido",
    anioPublicacion = firstPublishYear,
    portadaUrl = coverId?.let { "https://covers.openlibrary.org/b/id/$it-$TAMANO_PORTADA.jpg" }
)
