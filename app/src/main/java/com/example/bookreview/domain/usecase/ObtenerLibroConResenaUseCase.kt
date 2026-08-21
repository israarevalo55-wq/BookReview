package com.example.bookreview.domain.usecase

import com.example.bookreview.domain.model.LibroConResena
import com.example.bookreview.domain.repository.LibroRepository
import com.example.bookreview.domain.repository.ResenaRepository

/**
 * Caso de uso de la pantalla de Detalle: combina el libro que viene de
 * Open Library (remoto, vía [LibroRepository]) con la reseña que el
 * usuario ya haya guardado para ese libroId en Room (local, vía
 * [ResenaRepository]).
 *
 * Es la pieza que reemplaza lo que antes hacía DetalleViewModel llamando a
 * los dos repositorios por separado: ahora el ViewModel solo conoce este
 * caso de uso, y este caso de uso es el único punto de la app que conoce
 * *a la vez* LibroRepository y ResenaRepository para esta combinación.
 */
class ObtenerLibroConResenaUseCase(
    private val libroRepository: LibroRepository,
    private val resenaRepository: ResenaRepository
) {
    suspend operator fun invoke(libroId: String): LibroConResena? {
        val libro = libroRepository.getLibroPorId(libroId) ?: return null
        val resenaGuardada = resenaRepository.getResenaPorLibroId(libroId)
        return LibroConResena(libro = libro, resena = resenaGuardada)
    }
}
