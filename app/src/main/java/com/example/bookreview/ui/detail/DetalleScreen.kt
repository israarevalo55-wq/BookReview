package com.example.bookreview.ui.detail

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bookreview.ui.AppViewModelProvider
import com.example.bookreview.ui.common.IndicadorDeCarga
import com.example.bookreview.ui.common.MensajeDeError
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    onBack: () -> Unit,
    viewModel: DetalleViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Uri del archivo que se le pasa a la app de cámara como destino. Se
    // crea justo antes de lanzar la cámara y solo se usa si la captura
    // sale bien (por eso vive acá, no en el ViewModel: es un detalle de
    // "cómo" se toma la foto, no del resultado final).
    //
    // rememberSaveable (no remember): mientras la app de cámara está en
    // primer plano, Android puede matar el proceso de BookReview (por
    // ejemplo, al revocar un permiso "solo esta vez" apenas la app pasa a
    // segundo plano). Con remember, uriFotoPendiente se perdería y el
    // callback de éxito no sabría a qué Uri corresponde la foto. Uri es
    // Parcelable, así que rememberSaveable lo guarda solo, sin Saver
    // manual.
    var uriFotoPendiente by rememberSaveable { mutableStateOf<Uri?>(null) }

    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito) {
            uriFotoPendiente?.let { viewModel.onFotoCapturada(it.toString()) }
        }
        // exito == false (el usuario canceló o la cámara falló): no hacemos
        // nada, el formulario sigue como estaba. No es un error que haya
        // que mostrar, el usuario decidió no tomar la foto.
    }

    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            val uri = crearUriParaNuevaFoto(context)
            uriFotoPendiente = uri
            lanzadorCamara.launch(uri)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Sin permiso de cámara no se puede adjuntar una foto. " +
                        "Puedes seguir escribiendo tu reseña con normalidad."
                )
            }
        }
    }

    fun onTomarFotoClick() {
        val tienePermiso = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (tienePermiso) {
            val uri = crearUriParaNuevaFoto(context)
            uriFotoPendiente = uri
            lanzadorCamara.launch(uri)
        } else {
            lanzadorPermiso.launch(Manifest.permission.CAMERA)
        }
    }

    val carga = uiState.carga
    val tituloTopBar = (carga as? DetalleCarga.Exito)?.libro?.titulo ?: "Detalle"

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(tituloTopBar) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Mismo color de "favorito" que en Búsqueda y Mis
                    // Reseñas (colorScheme.primary del Theme): el ícono se
                    // ve igual en toda la app.
                    IconButton(onClick = viewModel::onToggleFavorito) {
                        Icon(
                            if (uiState.esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (uiState.esFavorito) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                LocalContentColor.current
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (carga) {
                DetalleCarga.Cargando -> IndicadorDeCarga()
                is DetalleCarga.Error -> {
                    MensajeDeError(mensaje = carga.mensaje, onReintentar = viewModel::cargar)
                }
                is DetalleCarga.Exito -> {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        carga.libro?.let { libro ->
                            AsyncImage(
                                model = libro.portadaUrl,
                                contentDescription = libro.titulo,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            )
                            Text(
                                libro.autor,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            libro.anioPublicacion?.let {
                                Text("Publicado en $it", style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Text(
                            "Tu calificación: ${uiState.rating.toInt()} / 5",
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Slider(
                            value = uiState.rating,
                            onValueChange = viewModel::onRatingChange,
                            valueRange = 0f..5f,
                            steps = 4
                        )

                        OutlinedTextField(
                            value = uiState.texto,
                            onValueChange = viewModel::onTextoChange,
                            label = { Text("Tu reseña") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            minLines = 4
                        )

                        Text(
                            "Tu foto",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        uiState.fotoUri?.let { fotoUri ->
                            AsyncImage(
                                model = fotoUri,
                                contentDescription = "Foto de tu reseña",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(top = 8.dp)
                            )
                        }
                        OutlinedButton(
                            onClick = ::onTomarFotoClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null)
                            Text(
                                if (uiState.fotoUri == null) "Tomar foto" else "Tomar otra foto",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        Button(
                            onClick = viewModel::guardarResena,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            enabled = !uiState.guardando
                        ) {
                            Text(if (uiState.yaTieneResena) "Actualizar reseña" else "Guardar reseña")
                        }
                    }
                }
            }
        }
    }
}
