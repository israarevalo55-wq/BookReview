package com.example.bookreview.data.repository

import com.example.bookreview.data.local.ResenaDao
import com.example.bookreview.data.local.ResenaEntity
import com.example.bookreview.domain.model.Resena
import com.example.bookreview.domain.repository.ResenaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Única clase de la app que conoce a la vez ResenaDao (Room) y el modelo de
 * dominio Resena: aquí se hace la traducción entre ambos mundos.
 */
class ResenaRepositoryImpl(
    private val resenaDao: ResenaDao
) : ResenaRepository {

    override fun getResenas(): Flow<List<Resena>> =
        resenaDao.getAll().map { lista -> lista.map { it.toDomain() } }

    override fun getFavoritos(): Flow<List<Resena>> =
        resenaDao.getFavoritos().map { lista -> lista.map { it.toDomain() } }

    override suspend fun getResenaPorLibroId(libroId: String): Resena? =
        resenaDao.getByLibroId(libroId)?.toDomain()

    override suspend fun guardarResena(resena: Resena) {
        resenaDao.insert(resena.toEntity())
    }

    override suspend fun eliminarResena(resena: Resena) {
        resenaDao.delete(resena.toEntity())
    }

    override suspend fun toggleFavorito(resena: Resena) {
        resenaDao.update(resena.copy(esFavorito = !resena.esFavorito).toEntity())
    }
}

private fun ResenaEntity.toDomain() = Resena(
    id = id,
    libroId = libroId,
    titulo = titulo,
    autor = autor,
    rating = rating,
    texto = texto,
    fotoUri = fotoUri,
    esFavorito = esFavorito
)

private fun Resena.toEntity() = ResenaEntity(
    id = id,
    libroId = libroId,
    titulo = titulo,
    autor = autor,
    rating = rating,
    texto = texto,
    fotoUri = fotoUri,
    esFavorito = esFavorito
)
