package com.example.bookreview.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room: mapeo exacto de la tabla "resenas".
 *
 * Vive solo dentro de data/local. Es data/repository (ResenaRepositoryImpl)
 * quien la traduce hacia/desde el modelo de dominio
 * [com.example.bookreview.domain.model.Resena] — así esta clase, con sus
 * anotaciones de Room, nunca se filtra hacia ui/ ni domain/.
 */
@Entity(tableName = "resenas")
data class ResenaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val libroId: String,
    val titulo: String,
    val autor: String,
    val rating: Float,
    val texto: String,
    val fotoUri: String? = null,
    val esFavorito: Boolean = false
)
