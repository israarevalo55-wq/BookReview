package com.example.bookreview.domain.repository

import com.example.bookreview.domain.model.Libro

/**
 * Contrato para buscar libros. Hoy [com.example.bookreview.data.repository.LibroRepositoryImpl]
 * devuelve una lista mock; la próxima semana la misma clase pasará a recibir
 * un OpenLibraryApi (Retrofit) por constructor y mapear su respuesta JSON a
 * [Libro]. Esta interfaz no cambia, por lo tanto BusquedaViewModel y
 * DetalleViewModel tampoco.
 */
interface LibroRepository {
    suspend fun buscarLibros(query: String): List<Libro>
    suspend fun getLibroPorId(id: String): Libro?
}
