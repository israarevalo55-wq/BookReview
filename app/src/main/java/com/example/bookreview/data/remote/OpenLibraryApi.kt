package com.example.bookreview.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Cliente Retrofit de la API pública de Open Library. Solo se usa desde
 * data/repository/LibroRepositoryImpl.kt — el ViewModel nunca ve esta
 * interfaz.
 */
interface OpenLibraryApi {

    // GET https://openlibrary.org/search.json?q={query}
    @GET("search.json")
    suspend fun buscarLibros(@Query("q") query: String): OpenLibrarySearchResponseDto
}
