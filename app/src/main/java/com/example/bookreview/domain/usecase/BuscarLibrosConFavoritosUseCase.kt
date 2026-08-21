package com.example.bookreview.domain.usecase

import com.example.bookreview.domain.model.Libro
import com.example.bookreview.domain.repository.LibroRepository
import com.example.bookreview.domain.repository.ResenaRepository
import kotlinx.coroutines.flow.first

/**
 * Caso de uso de la pantalla de Búsqueda: busca libros en Open Library
 * (remoto, vía [LibroRepository]) y marca cuáles de esos resultados ya
 * están guardados como favoritos en Room (local, vía [ResenaRepository]),
 * cruzando por libroId.
 *
 * Igual que [ObtenerLibroConResenaUseCase]: es el único lugar de la app
 * donde se combinan las dos fuentes para este propósito. BusquedaViewModel
 * solo ve la lista de [Libro] ya combinada.
 */
class BuscarLibrosConFavoritosUseCase(
    private val libroRepository: LibroRepository,
    private val resenaRepository: ResenaRepository
) {
    suspend operator fun invoke(query: String): List<Libro> {
        val resultados = libroRepository.buscarLibros(query)
        if (resultados.isEmpty()) return resultados

        // Un solo vistazo a los favoritos guardados (no una colección en
        // vivo): esta búsqueda ya es una operación puntual, igual que hace
        // LibroRepository.buscarLibros(). .first() toma el valor actual del
        // Flow que expone Room y sigue.
        val idsFavoritos = resenaRepository.getFavoritos().first()
            .map { it.libroId }
            .toSet()

        return resultados.map { libro ->
            libro.copy(esFavorito = libro.id in idsFavoritos)
        }
    }
}
