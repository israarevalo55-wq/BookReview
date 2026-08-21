package com.example.bookreview.ui.detail

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Crea (vacío) el archivo donde va a quedar la próxima foto de reseña y
 * devuelve su Uri "segura" vía FileProvider — nunca una ruta file://
 * directa (Android la bloquea entre apps desde la API 24).
 *
 * Vive en ui/detail porque crear archivos y pedirle un content:// Uri al
 * FileProvider es un detalle de la plataforma Android, no una regla de
 * negocio: domain/ y el ViewModel nunca ven esto, solo reciben la Uri ya
 * resuelta como String cuando la foto se toma con éxito.
 */
fun crearUriParaNuevaFoto(context: Context): Uri {
    val carpetaFotos = File(context.filesDir, "fotos").apply { mkdirs() }
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val archivo = File(carpetaFotos, "resena_$timestamp.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
}
