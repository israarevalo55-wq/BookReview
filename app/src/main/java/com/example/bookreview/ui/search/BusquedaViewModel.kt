package com.example.bookreview.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookreview.domain.model.Libro
import com.example.bookreview.domain.usecase.BuscarLibrosConFavoritosUseCase
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
 * No conoce LibroRepository ni ResenaRepository directamente: solo conoce
 * BuscarLibrosConFavoritosUseCase, que es quien de verdad combina Open
 * Library (remoto) con los favoritos guardados en Room (local). El
 * ViewModel nunca hace esa combinación por su cuenta.
 */
class BusquedaViewModel(
    private val buscarLibrosConFavoritosUseCase: BuscarLibrosConFavoritosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BusquedaUiState())
    val uiState: StateFlow<BusquedaUiState> = _uiState.asStateFlow()

    // Ya no se dispara una búsqueda automática al entrar: con la API real
    // una query vacía no tiene un "resultado por defecto" como sí lo tenía
    // la lista mock de la Semana 1. La pantalla arranca invitando a escribir.

    fun onQueryChange(nuevoQuery: String) {
        _uiState.update { it.copy(query = nuevoQuery) }
    }

    fun buscar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            val resultados = buscarLibrosConFavoritosUseCase(_uiState.value.query)
            _uiState.update { it.copy(resultados = resultados, cargando = false, yaBusco = true) }
        }
    }
}
