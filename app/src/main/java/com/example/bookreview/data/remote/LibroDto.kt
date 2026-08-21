package com.example.bookreview.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Forma exacta de la respuesta de https://openlibrary.org/search.json?q=...
 *
 * Son clases "tontas": solo reflejan el JSON. La traducción hacia el modelo
 * de dominio [com.example.bookreview.domain.model.Libro] vive en
 * data/repository/LibroRepositoryImpl.kt, no acá.
 */
data class OpenLibrarySearchResponseDto(
    @SerializedName("docs")
    val docs: List<LibroDto> = emptyList()
)

data class LibroDto(
    // "key" identifica al libro en Open Library, p.ej. "/works/OL27448W".
    // Lo usamos tal cual como id de dominio.
    @SerializedName("key")
    val key: String,

    @SerializedName("title")
    val title: String? = null,

    // Open Library devuelve una lista de autores (puede haber más de uno).
    @SerializedName("author_name")
    val authorName: List<String>? = null,

    @SerializedName("first_publish_year")
    val firstPublishYear: Int? = null,

    // Id numérico de la portada. No toda edición tiene una; puede venir null.
    // Con este id se arma la URL de covers.openlibrary.org.
    @SerializedName("cover_i")
    val coverId: Int? = null
)
