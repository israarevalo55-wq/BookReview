package com.example.bookreview.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// Un único DataStore de preferencias para toda la app. Es el lugar correcto
// para un ajuste simple clave-valor como el modo oscuro; NO reemplaza a
// Room, que es para colecciones/entidades como las reseñas.
val Context.dataStore by preferencesDataStore(name = "ajustes")

object PreferenciasKeys {
    val MODO_OSCURO = booleanPreferencesKey("modo_oscuro")
}
