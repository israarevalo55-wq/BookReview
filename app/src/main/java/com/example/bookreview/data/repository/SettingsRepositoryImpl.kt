package com.example.bookreview.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.bookreview.data.local.PreferenciasKeys
import com.example.bookreview.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override val modoOscuro: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[PreferenciasKeys.MODO_OSCURO] ?: false }

    override suspend fun setModoOscuro(activado: Boolean) {
        dataStore.edit { prefs -> prefs[PreferenciasKeys.MODO_OSCURO] = activado }
    }
}
