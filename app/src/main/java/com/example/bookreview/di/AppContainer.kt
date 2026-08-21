package com.example.bookreview.di

import android.content.Context
import com.example.bookreview.data.local.AppDatabase
import com.example.bookreview.data.local.dataStore
import com.example.bookreview.data.repository.LibroRepositoryImpl
import com.example.bookreview.data.repository.ResenaRepositoryImpl
import com.example.bookreview.data.repository.SettingsRepositoryImpl
import com.example.bookreview.domain.repository.LibroRepository
import com.example.bookreview.domain.repository.ResenaRepository
import com.example.bookreview.domain.repository.SettingsRepository

/**
 * Contenedor manual de dependencias (no usamos Hilt en este proyecto):
 * construye una única vez cada repositorio y los entrega bajo demanda.
 * Vive colgado de [com.example.bookreview.BookReviewApplication], por lo
 * que sobrevive a la navegación entre pantallas y a cambios de
 * configuración (rotación, etc).
 */
interface AppContainer {
    val resenaRepository: ResenaRepository
    val libroRepository: LibroRepository
    val settingsRepository: SettingsRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val resenaRepository: ResenaRepository by lazy {
        ResenaRepositoryImpl(AppDatabase.getDatabase(context).resenaDao())
    }

    override val libroRepository: LibroRepository by lazy {
        LibroRepositoryImpl()
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(context.dataStore)
    }
}
