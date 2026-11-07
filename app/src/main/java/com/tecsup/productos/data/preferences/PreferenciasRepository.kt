package com.tecsup.productos.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "preferencias_usuario",
)

class PreferenciasRepository(private val context: Context) {
    private val claveModoOscuro = booleanPreferencesKey("modo_oscuro")

    val modoOscuro: Flow<Boolean> = context.dataStore.data.map { preferencias ->
        preferencias[claveModoOscuro] ?: false
    }

    suspend fun guardarModoOscuro(activado: Boolean) {
        context.dataStore.edit { preferencias ->
            preferencias[claveModoOscuro] = activado
        }
    }
}
