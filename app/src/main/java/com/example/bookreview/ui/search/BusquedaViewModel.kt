package com.example.bookreview.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookreview.domain.model.Libro
import com.example.bookreview.domain.repository.LibroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BusquedaUiState(
    val query: String = "",
    val resultados: List<Libro> = emptyList(),
    val cargando: Boolean = false,
    val yaBusco: Boolean = false
)

/**
 * Solo conoce LibroRepository (interfaz de dominio), no sabe si detrás hay
 * una lista mock o Retrofit. Por eso la próxima semana, cuando
 * LibroRepositoryImpl empiece a llamar a Open Library, esta clase no
 * necesita ningún cambio.
 */
class BusquedaViewModel(
    private val libroRepository: LibroRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BusquedaUiState())
    val uiState: StateFlow<BusquedaUiState> = _uiState.asStateFlow()

    init {
        // Carga inicial para no mostrar la pantalla vacía antes de escribir nada.
        buscar()
    }

    fun onQueryChange(nuevoQuery: String) {
        _uiState.update { it.copy(query = nuevoQuery) }
    }

    fun buscar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            val resultados = libroRepository.buscarLibros(_uiState.value.query)
            _uiState.update { it.copy(resultados = resultados, cargando = false, yaBusco = true) }
        }
    }
}
