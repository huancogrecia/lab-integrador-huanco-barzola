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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ListaViewModel(private val repo: ItemRepository) : ViewModel() {
    private val _estado = MutableStateFlow<UiState<List<Item>>>(UiState.Cargando)
    val estado = _estado.asStateFlow()
    private var carga: Job? = null
    private var primeraCargaTerminada = false

    init {
        viewModelScope.launch {
            repo.items
                .catch { error -> _estado.value = UiState.Error(mensajeDe(error)) }
                .collect { lista ->
                    if (lista.isNotEmpty() || primeraCargaTerminada) {
                        _estado.value = UiState.Exito(lista)
                    }
                }
        }
        cargar()
    }

    fun cargar() {
        carga?.cancel()
        carga = viewModelScope.launch {
            if (repo.items.first().isEmpty()) {
                _estado.value = UiState.Cargando
            }

            val resultado = repo.refrescar()
            primeraCargaTerminada = true
            val guardados = repo.items.first()
            _estado.value = resultado.fold(
                onSuccess = { UiState.Exito(guardados) },
                onFailure = { error ->
                    if (guardados.isNotEmpty()) UiState.Exito(guardados)
                    else UiState.Error(mensajeDe(error))
                },
            )
        }
    }

    fun alternarFavorito(id: Int) {
        viewModelScope.launch { repo.alternarFavorito(id) }
    }

    private fun mensajeDe(error: Throwable): String = when (error) {
        is IOException -> "No hay conexión a internet"
        is HttpException -> "El servidor respondió con error ${error.code()}"
        else -> error.localizedMessage ?: "No se pudieron cargar los productos"
    }
}
