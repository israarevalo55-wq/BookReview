package com.example.bookreview.domain.repository

import com.example.bookreview.domain.model.Resena
import kotlinx.coroutines.flow.Flow

/**
 * Contrato que el ViewModel conoce. El ViewModel nunca ve ResenaDao ni
 * AppDatabase directamente — siempre pasa por esta interfaz. Esa regla es
 * la que permite, por ejemplo, reemplazar la implementación real por un
 * fake en un test de ViewModel sin tocar Room para nada.
 */
interface ResenaRepository {
    fun getResenas(): Flow<List<Resena>>
    fun getFavoritos(): Flow<List<Resena>>
    suspend fun getResenaPorLibroId(libroId: String): Resena?
    suspend fun guardarResena(resena: Resena)
    suspend fun eliminarResena(resena: Resena)
    suspend fun toggleFavorito(resena: Resena)
}
