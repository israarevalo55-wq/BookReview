package com.example.bookreview.domain.usecase

import com.example.bookreview.domain.model.Libro
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
        val libro = libroRepository.getLibroPorId(libroId)
        val resenaGuardada = resenaRepository.getResenaPorLibroId(libroId)

        if (libro == null && resenaGuardada == null) return null

        // libroRepository.getLibroPorId() resuelve desde una caché en
        // memoria de la última búsqueda (ver LibroRepositoryImpl): si el
        // proceso de la app murió y se recreó -algo que Android puede
        // hacer, por ejemplo, al volver de la cámara del sistema con un
        // permiso "solo esta vez"- esa caché queda vacía, aunque la reseña
        // siga perfectamente guardada en Room. Para no perder rating/
        // texto/foto ya guardados solo porque se perdió el caché de
        // búsqueda, reconstruimos un Libro mínimo con los datos que la
        // propia Resena ya guarda duplicados (titulo, autor) para
        // exactamente este caso.
        val libroParaMostrar = libro ?: Libro(
            id = libroId,
            titulo = resenaGuardada!!.titulo,
            autor = resenaGuardada.autor
        )

        return LibroConResena(libro = libroParaMostrar, resena = resenaGuardada)
    }
}
