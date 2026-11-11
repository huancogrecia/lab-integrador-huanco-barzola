package com.tecsup.productos.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.productos.data.preferences.PreferenciasRepository
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.ui.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val repo: ItemRepository,
    private val preferencias: PreferenciasRepository,
) : ViewModel() {
    private val _estado = MutableStateFlow<UiState<Int>>(UiState.Cargando)
    val estado = _estado.asStateFlow()
    val modoOscuro = preferencias.modoOscuro.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    private var observador: Job? = null

    init {
        observar()
    }

    fun observar() {
        observador?.cancel()
        _estado.value = UiState.Cargando
        observador = viewModelScope.launch {
            repo.favoritos
                .catch { _estado.value = UiState.Error("No pudimos cargar tu cantidad de favoritos.") }
                .collect { favoritos -> _estado.value = UiState.Exito(favoritos.size) }
        }
    }

    fun actualizarModoOscuro(activado: Boolean) {
        viewModelScope.launch { preferencias.guardarModoOscuro(activado) }
    }
}
