package com.example.bookreview.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookreview.domain.model.Libro
import com.example.bookreview.domain.model.Resena
import com.example.bookreview.domain.repository.LibroRepository
import com.example.bookreview.domain.repository.ResenaRepository
import com.example.bookreview.ui.navigation.ARG_LIBRO_ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetalleUiState(
    val libro: Libro? = null,
    val rating: Float = 0f,
    val texto: String = "",
    val esFavorito: Boolean = false,
    val guardando: Boolean = false,
    val yaTieneResena: Boolean = false
)

class DetalleViewModel(
    savedStateHandle: SavedStateHandle,
    private val libroRepository: LibroRepository,
    private val resenaRepository: ResenaRepository
) : ViewModel() {

    // Navigation Compose entrega el argumento de ruta {libroId} a través de
    // SavedStateHandle: el ViewModel lo lee acá y así no depende de
    // NavController ni de la pantalla que lo llamó.
    private val libroId: String = checkNotNull(savedStateHandle[ARG_LIBRO_ID])

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    private fun cargar() {
        viewModelScope.launch {
            val libro = libroRepository.getLibroPorId(libroId)
            val resenaExistente = resenaRepository.getResenaPorLibroId(libroId)
            _uiState.update {
                it.copy(
                    libro = libro,
                    rating = resenaExistente?.rating ?: 0f,
                    texto = resenaExistente?.texto ?: "",
                    esFavorito = resenaExistente?.esFavorito ?: false,
                    yaTieneResena = resenaExistente != null
                )
            }
        }
    }

    fun onRatingChange(nuevoRating: Float) =
        _uiState.update { it.copy(rating = nuevoRating) }

    fun onTextoChange(nuevoTexto: String) =
        _uiState.update { it.copy(texto = nuevoTexto) }

    fun onToggleFavorito() =
        _uiState.update { it.copy(esFavorito = !it.esFavorito) }

    fun guardarResena() {
        val estado = _uiState.value
        val libro = estado.libro ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            resenaRepository.guardarResena(
                Resena(
                    libroId = libro.id,
                    titulo = libro.titulo,
                    autor = libro.autor,
                    rating = estado.rating,
                    texto = estado.texto,
                    fotoUri = null, // se completa cuando agreguemos la cámara
                    esFavorito = estado.esFavorito
                )
            )
            _uiState.update { it.copy(guardando = false, yaTieneResena = true) }
        }
    }
}
