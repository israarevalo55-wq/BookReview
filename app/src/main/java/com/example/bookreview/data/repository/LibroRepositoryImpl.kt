package com.example.bookreview.data.repository

import com.example.bookreview.domain.model.Libro
import com.example.bookreview.domain.repository.LibroRepository
import kotlinx.coroutines.delay

/**
 * Implementación MOCK de LibroRepository para esta semana.
 *
 * La próxima semana esta clase pasará a recibir un OpenLibraryApi (Retrofit)
 * por constructor y a mapear la respuesta JSON de
 * https://openlibrary.org/search.json a [Libro]. La interfaz
 * [LibroRepository] no cambia, así que BusquedaViewModel y DetalleViewModel
 * no se tocan: es el punto exacto de la app donde se conecta la API real.
 */
class LibroRepositoryImpl : LibroRepository {

    override suspend fun buscarLibros(query: String): List<Libro> {
        delay(400) // simula latencia de red para poder probar el estado "cargando"
        if (query.isBlank()) return librosMock
        return librosMock.filter {
            it.titulo.contains(query, ignoreCase = true) ||
                it.autor.contains(query, ignoreCase = true)
        }
    }

    override suspend fun getLibroPorId(id: String): Libro? =
        librosMock.find { it.id == id }

    companion object {
        private val librosMock = listOf(
            Libro("ol1", "Cien años de soledad", "Gabriel García Márquez", 1967, "https://picsum.photos/seed/ol1/300/450"),
            Libro("ol2", "1984", "George Orwell", 1949, "https://picsum.photos/seed/ol2/300/450"),
            Libro("ol3", "El nombre del viento", "Patrick Rothfuss", 2007, "https://picsum.photos/seed/ol3/300/450"),
            Libro("ol4", "Fahrenheit 451", "Ray Bradbury", 1953, "https://picsum.photos/seed/ol4/300/450"),
            Libro("ol5", "Sapiens", "Yuval Noah Harari", 2011, "https://picsum.photos/seed/ol5/300/450"),
            Libro("ol6", "El hobbit", "J. R. R. Tolkien", 1937, "https://picsum.photos/seed/ol6/300/450")
        )
    }
}
