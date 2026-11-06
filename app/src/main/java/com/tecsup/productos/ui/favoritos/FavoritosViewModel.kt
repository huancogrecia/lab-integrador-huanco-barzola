package com.tecsup.productos.ui.favoritos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.ui.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class FavoritosViewModel(private val repo: ItemRepository) : ViewModel() {
    private val _estado = MutableStateFlow<UiState<List<Item>>>(UiState.Cargando)
    val estado = _estado.asStateFlow()
    private var observador: Job? = null

    init {
        observar()
    }

    fun observar() {
        observador?.cancel()
        observador = viewModelScope.launch {
            repo.favoritos
                .catch { error ->
                    _estado.value = UiState.Error(
                        error.localizedMessage ?: "No se pudieron cargar los favoritos"
                    )
                }
                .collect { lista -> _estado.value = UiState.Exito(lista) }
        }
    }

    fun alternarFavorito(id: Int) {
        viewModelScope.launch { repo.alternarFavorito(id) }
    }
}
