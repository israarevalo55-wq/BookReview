package com.example.bookreview.ui.detail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookreview.domain.model.Libro
import com.example.bookreview.domain.model.Resena
import com.example.bookreview.domain.repository.ResenaRepository
import com.example.bookreview.domain.usecase.ObtenerLibroConResenaUseCase
import com.example.bookreview.ui.navigation.ARG_LIBRO_ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Resultado de cargar el libro + la reseña existente. Sealed por la misma
 * razón que [com.example.bookreview.ui.search.ResultadoBusqueda]: un
 * `when` exhaustivo en la pantalla, no banderas sueltas.
 */
sealed interface DetalleCarga {
    data object Cargando : DetalleCarga

    /** libro puede ser null: ni Open Library ni Room tenían nada para este
     *  id (caso de negocio válido, no un error). La pantalla igual deja
     *  escribir una reseña. */
    data class Exito(val libro: Libro?) : DetalleCarga
    data class Error(val mensaje: String) : DetalleCarga
}

data class DetalleUiState(
    val carga: DetalleCarga = DetalleCarga.Cargando,
    val rating: Float = 0f,
    val texto: String = "",
    val esFavorito: Boolean = false,
    val fotoUri: String? = null,
    val guardando: Boolean = false,
    val yaTieneResena: Boolean = false
)

class DetalleViewModel(
    savedStateHandle: SavedStateHandle,
    private val resenaRepository: ResenaRepository,
    private val obtenerLibroConResenaUseCase: ObtenerLibroConResenaUseCase
) : ViewModel() {

    // Navigation Compose entrega el argumento de ruta {libroId} a través de
    // SavedStateHandle: el ViewModel lo lee acá y así no depende de
    // NavController ni de la pantalla que lo llamó. Se decodifica porque
    // Screen.Detalle.createRoute() lo codificó con Uri.encode (el "key" de
    // Open Library trae barras, p.ej. "/works/OL27448W"). Si Navigation ya
    // lo entregó decodificado, Uri.decode sobre texto sin "%" es un no-op.
    private val libroId: String = Uri.decode(checkNotNull(savedStateHandle[ARG_LIBRO_ID]))

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    // Pública (no privada como antes): la pantalla la vuelve a llamar
    // desde el botón "Reintentar" cuando carga queda en Error.
    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(carga = DetalleCarga.Cargando) }
            // Un solo llamado: el caso de uso es quien fue a buscar el libro
            // (remoto) y la reseña existente (local, Room), las combinó, y
            // ya atrapó cualquier excepción de red devolviendo un Result.
            // El ViewModel solo desempaqueta el resultado hacia el UiState.
            obtenerLibroConResenaUseCase(libroId).fold(
                onSuccess = { libroConResena ->
                    val resenaExistente = libroConResena?.resena
                    _uiState.update {
                        it.copy(
                            carga = DetalleCarga.Exito(libroConResena?.libro),
                            rating = resenaExistente?.rating ?: 0f,
                            texto = resenaExistente?.texto ?: "",
                            esFavorito = resenaExistente?.esFavorito ?: false,
                            fotoUri = resenaExistente?.fotoUri,
                            yaTieneResena = resenaExistente != null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(carga = DetalleCarga.Error(error.message ?: "Ocurrió un error inesperado."))
                    }
                }
            )
        }
    }

    fun onRatingChange(nuevoRating: Float) =
        _uiState.update { it.copy(rating = nuevoRating) }

    fun onTextoChange(nuevoTexto: String) =
        _uiState.update { it.copy(texto = nuevoTexto) }

    fun onToggleFavorito() =
        _uiState.update { it.copy(esFavorito = !it.esFavorito) }

    // La pantalla es quien pidió el permiso y manejó la cámara (Semana 4);
    // acá solo llega el resultado final, como String. El ViewModel nunca
    // supo que existió un permiso, un FileProvider o un Intent de cámara.
    fun onFotoCapturada(uri: String) =
        _uiState.update { it.copy(fotoUri = uri) }

    fun guardarResena() {
        val estado = _uiState.value
        val libro = (estado.carga as? DetalleCarga.Exito)?.libro ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            resenaRepository.guardarResena(
                Resena(
                    libroId = libro.id,
                    titulo = libro.titulo,
                    autor = libro.autor,
                    rating = estado.rating,
                    texto = estado.texto,
                    fotoUri = estado.fotoUri,
                    esFavorito = estado.esFavorito
                )
            )
            _uiState.update { it.copy(guardando = false, yaTieneResena = true) }
        }
    }
}
