package com.example.bookreview.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookreview.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AjustesViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val modoOscuro: StateFlow<Boolean> = settingsRepository.modoOscuro
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = false)

    fun setModoOscuro(activado: Boolean) {
        viewModelScope.launch { settingsRepository.setModoOscuro(activado) }
    }
}
