package com.example.bookreview.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Contrato para el único ajuste de esta semana (modo oscuro). Detrás usa
 * DataStore, pero ni el ViewModel de Ajustes ni BookReviewApp lo saben.
 */
interface SettingsRepository {
    val modoOscuro: Flow<Boolean>
    suspend fun setModoOscuro(activado: Boolean)
}
