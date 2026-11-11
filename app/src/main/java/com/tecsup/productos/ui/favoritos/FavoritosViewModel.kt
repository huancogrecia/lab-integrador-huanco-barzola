package com.tecsup.productos.ui.favoritos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.ui.UiState
import kotlinx.coroutines.CancellationException
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
        _estado.value = UiState.Cargando
        observador = viewModelScope.launch {
            repo.favoritos
                .catch { _estado.value = UiState.Error("No pudimos cargar tus favoritos.") }
                .collect { lista -> _estado.value = UiState.Exito(lista) }
        }
    }

    private val _errorFavorito = MutableStateFlow<String?>(null)
    val errorFavorito = _errorFavorito.asStateFlow()

    fun alternarFavorito(id: Int) {
        viewModelScope.launch {
            _errorFavorito.value = null
            try {
                repo.alternarFavorito(id)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _errorFavorito.value = "No pudimos cambiar tus favoritos. Vuelve a tocar el corazón."
            }
        }
    }
}
