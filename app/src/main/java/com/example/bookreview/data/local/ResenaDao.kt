package com.example.bookreview.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ResenaDao {

    // Devolver Flow<List<...>> le dice a Room que emita una lista nueva
    // automáticamente cada vez que la tabla "resenas" cambie. La pantalla
    // de Mis Reseñas nunca hace polling ni recarga manual: solo colecciona
    // este Flow.
    @Query("SELECT * FROM resenas ORDER BY id DESC")
    fun getAll(): Flow<List<ResenaEntity>>

    @Query("SELECT * FROM resenas WHERE esFavorito = 1 ORDER BY id DESC")
    fun getFavoritos(): Flow<List<ResenaEntity>>

    @Query("SELECT * FROM resenas WHERE libroId = :libroId LIMIT 1")
    suspend fun getByLibroId(libroId: String): ResenaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(resena: ResenaEntity): Long

    @Update
    suspend fun update(resena: ResenaEntity)

    @Delete
    suspend fun delete(resena: ResenaEntity)
}
