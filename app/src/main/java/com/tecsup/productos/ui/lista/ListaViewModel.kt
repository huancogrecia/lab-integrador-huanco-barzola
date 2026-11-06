package com.tecsup.productos.ui.lista

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

class ListaViewModel(private val repo: ItemRepository) : ViewModel() {
    private val _estado = MutableStateFlow<UiState<List<Item>>>(UiState.Cargando)
    val estado = _estado.asStateFlow()
    private var carga: Job? = null

    init {
        cargar()
    }

    fun cargar() {
        carga?.cancel()
        _estado.value = UiState.Cargando
        carga = viewModelScope.launch {
            repo.items.catch {
                _estado.value = UiState.Error("No se pudieron cargar los productos")
            }.collect { lista ->
                _estado.value = UiState.Exito(lista)
            }
        }
    }
}
