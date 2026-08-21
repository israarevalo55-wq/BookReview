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

/**
 * Resultado de la última búsqueda. Sealed a propósito: la pantalla resuelve
 * qué mostrar con un `when` exhaustivo, en vez de combinar banderas sueltas
 * (cargando, yaBusco, hayError...) que podrían quedar en una combinación
 * inválida (¿cargando Y con error a la vez? con esto no puede pasar).
 */
sealed interface ResultadoBusqueda {
    /** Todavía no se disparó ninguna búsqueda en esta pantalla. */
    data object Inicial : ResultadoBusqueda
    data object Cargando : ResultadoBusqueda
    data class Exito(val resultados: List<Libro>) : ResultadoBusqueda
    data class Error(val mensaje: String) : ResultadoBusqueda
}

data class BusquedaUiState(
    val query: String = "",
    val resultado: ResultadoBusqueda = ResultadoBusqueda.Inicial
)

/**
 * No conoce LibroRepository ni ResenaRepository directamente: solo conoce
 * BuscarLibrosConFavoritosUseCase, que es quien de verdad combina Open
 * Library (remoto) con los favoritos guardados en Room (local). El
 * ViewModel nunca hace esa combinación por su cuenta, y tampoco conoce
 * excepciones de red: el caso de uso ya le entrega un [Result] con un
 * mensaje amigable si algo falló.
 */
class BusquedaViewModel(
    private val buscarLibrosConFavoritosUseCase: BuscarLibrosConFavoritosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BusquedaUiState())
    val uiState: StateFlow<BusquedaUiState> = _uiState.asStateFlow()

    fun onQueryChange(nuevoQuery: String) {
        _uiState.update { it.copy(query = nuevoQuery) }
    }

    fun buscar() {
        viewModelScope.launch {
            _uiState.update { it.copy(resultado = ResultadoBusqueda.Cargando) }
            buscarLibrosConFavoritosUseCase(_uiState.value.query).fold(
                onSuccess = { libros ->
                    _uiState.update { it.copy(resultado = ResultadoBusqueda.Exito(libros)) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(resultado = ResultadoBusqueda.Error(error.message ?: "Ocurrió un error inesperado."))
                    }
                }
            )
        }
    }
}
