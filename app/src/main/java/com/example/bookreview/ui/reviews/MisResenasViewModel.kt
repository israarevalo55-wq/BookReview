package com.example.bookreview.ui.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookreview.domain.model.Resena
import com.example.bookreview.domain.repository.ResenaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MisResenasViewModel(
    private val resenaRepository: ResenaRepository
) : ViewModel() {

    // stateIn convierte el Flow "en vivo" que viene de Room en un StateFlow
    // que la UI lee con collectAsState. WhileSubscribed(5_000) mantiene la
    // colección activa 5s después de que la pantalla deja de observarlo,
    // para no perder el estado en una rotación rápida ni seguir
    // recolectando para siempre en segundo plano sin necesidad.
    val uiState: StateFlow<List<Resena>> = resenaRepository.getResenas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun eliminar(resena: Resena) {
        viewModelScope.launch { resenaRepository.eliminarResena(resena) }
    }

    fun toggleFavorito(resena: Resena) {
        viewModelScope.launch { resenaRepository.toggleFavorito(resena) }
    }
}
