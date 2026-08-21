package com.example.bookreview.domain.usecase

import java.io.IOException

/**
 * Traduce una excepción a un mensaje que se puede mostrar directamente en
 * la UI, sin que el ViewModel tenga que conocer tipos de excepción.
 *
 * A propósito NO importa nada de Retrofit/OkHttp: domain/ no debe conocer
 * la librería HTTP que hay detrás del LibroRepository. IOException ya
 * cubre UnknownHostException (sin DNS/sin red) y SocketTimeoutException
 * (timeout) porque ambas son subclases suyas — es la única distinción que
 * de verdad le importa al usuario: "no hay conexión" vs. "pasó otra cosa".
 */
internal fun Throwable.aMensajeDeErrorAmigable(): String = when (this) {
    is IOException -> "Sin conexión a internet. Verifica tu red e intenta de nuevo."
    else -> "Ocurrió un error inesperado. Intenta de nuevo."
}
