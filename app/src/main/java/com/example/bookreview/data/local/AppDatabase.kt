package com.example.bookreview.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ResenaEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun resenaDao(): ResenaDao

    companion object {
        // Patrón singleton clásico de Room: una sola instancia de la base de
        // datos para toda la app, protegida con @Volatile + synchronized
        // para que dos hilos no la creen dos veces a la vez.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bookreview_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
