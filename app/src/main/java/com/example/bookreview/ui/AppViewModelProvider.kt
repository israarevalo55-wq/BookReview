package com.example.bookreview.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bookreview.BookReviewApplication
import com.example.bookreview.ui.detail.DetalleViewModel
import com.example.bookreview.ui.reviews.MisResenasViewModel
import com.example.bookreview.ui.search.BusquedaViewModel
import com.example.bookreview.ui.settings.AjustesViewModel

/**
 * Fábrica única de ViewModels para toda la app.
 *
 * No usamos Hilt: en su lugar usamos el mecanismo estándar de
 * androidx.lifecycle (el mismo que usa el codelab oficial "Inventory" de
 * Android Basics with Compose). Cada `initializer` sabe construir un
 * ViewModel pidiéndole sus repositorios al AppContainer de
 * [BookReviewApplication]. El ViewModel en sí solo recibe interfaces de
 * dominio por constructor: nunca ve a AppContainer, Room ni Retrofit.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            BusquedaViewModel(bookReviewApplication().container.libroRepository)
        }
        initializer {
            DetalleViewModel(
                savedStateHandle = createSavedStateHandle(),
                libroRepository = bookReviewApplication().container.libroRepository,
                resenaRepository = bookReviewApplication().container.resenaRepository
            )
        }
        initializer {
            MisResenasViewModel(bookReviewApplication().container.resenaRepository)
        }
        initializer {
            AjustesViewModel(bookReviewApplication().container.settingsRepository)
        }
    }
}

/**
 * Extrae la Application desde las CreationExtras que Compose Navigation le
 * pasa a cada `viewModel(factory = ...)`. Es la forma estándar (sin Hilt)
 * de llegar desde un initializer hasta el AppContainer.
 */
fun CreationExtras.bookReviewApplication(): BookReviewApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BookReviewApplication)
