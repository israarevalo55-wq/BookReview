package com.example.bookreview.di

import android.content.Context
import android.content.pm.ApplicationInfo
import com.example.bookreview.data.local.AppDatabase
import com.example.bookreview.data.local.dataStore
import com.example.bookreview.data.remote.OpenLibraryApi
import com.example.bookreview.data.repository.LibroRepositoryImpl
import com.example.bookreview.data.repository.ResenaRepositoryImpl
import com.example.bookreview.data.repository.SettingsRepositoryImpl
import com.example.bookreview.domain.repository.LibroRepository
import com.example.bookreview.domain.repository.ResenaRepository
import com.example.bookreview.domain.repository.SettingsRepository
import com.example.bookreview.domain.usecase.BuscarLibrosConFavoritosUseCase
import com.example.bookreview.domain.usecase.ObtenerLibroConResenaUseCase
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val OPEN_LIBRARY_BASE_URL = "https://openlibrary.org/"

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

    // Casos de uso: combinan libroRepository (remoto) + resenaRepository
    // (local). Viven acá, no en los repositorios ni en los ViewModels.
    val obtenerLibroConResenaUseCase: ObtenerLibroConResenaUseCase
    val buscarLibrosConFavoritosUseCase: BuscarLibrosConFavoritosUseCase
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    // El interceptor de logging solo imprime el body completo de cada
    // request/response cuando la app es "debuggable" (build de debug).
    // En un build de release quedaría en NONE: no queremos loguear tráfico
    // de red completo -por costo y por privacidad- en producción.
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = if (isDebuggable()) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(OPEN_LIBRARY_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val openLibraryApi: OpenLibraryApi by lazy {
        retrofit.create(OpenLibraryApi::class.java)
    }

    override val resenaRepository: ResenaRepository by lazy {
        ResenaRepositoryImpl(AppDatabase.getDatabase(context).resenaDao())
    }

    override val libroRepository: LibroRepository by lazy {
        LibroRepositoryImpl(openLibraryApi)
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(context.dataStore)
    }

    override val obtenerLibroConResenaUseCase: ObtenerLibroConResenaUseCase by lazy {
        ObtenerLibroConResenaUseCase(libroRepository, resenaRepository)
    }

    override val buscarLibrosConFavoritosUseCase: BuscarLibrosConFavoritosUseCase by lazy {
        BuscarLibrosConFavoritosUseCase(libroRepository, resenaRepository)
    }

    private fun isDebuggable(): Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
}
