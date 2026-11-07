package com.tecsup.productos.ui.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.notification.Notificador
import com.tecsup.productos.ui.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ListaViewModel(
    private val repo: ItemRepository,
    private val notificador: Notificador,
) : ViewModel() {
    private val _estado = MutableStateFlow<UiState<List<Item>>>(UiState.Cargando)
    val estado = _estado.asStateFlow()

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso = _aviso.asStateFlow()

    private var carga: Job? = null
    private var primeraCargaTerminada = false
    private var refrescoEnCurso = false
    private var errorSinDatos: Throwable? = null

    init {
        viewModelScope.launch {
            repo.items
                .catch { error ->
                    errorSinDatos = error
                    _estado.value = UiState.Error(mensajeDe(error))
                }
                .collect { lista ->
                    when {
                        lista.isNotEmpty() -> _estado.value = UiState.Exito(lista)
                        primeraCargaTerminada && !refrescoEnCurso && errorSinDatos == null -> {
                            _estado.value = UiState.Exito(lista)
                        }
                    }
                }
        }
        cargar()
    }

    fun cargar() {
        carga?.cancel()
        carga = viewModelScope.launch {
            val actuales = repo.items.first()
            refrescoEnCurso = true
            if (actuales.isEmpty()) {
                _estado.value = UiState.Cargando
            }

            val resultado = repo.refrescar()
            val guardados = repo.items.first()
            primeraCargaTerminada = true
            resultado.fold(
                onSuccess = { cantidad ->
                    errorSinDatos = null
                    refrescoEnCurso = false
                    _aviso.value = null
                    _estado.value = UiState.Exito(guardados)
                    notificador.mostrar("Datos actualizados", "Se guardaron $cantidad elementos")
                },
                onFailure = { error ->
                    if (guardados.isEmpty()) {
                        errorSinDatos = error
                        refrescoEnCurso = false
                        _aviso.value = null
                        _estado.value = UiState.Error(mensajeDe(error))
                    } else {
                        errorSinDatos = null
                        refrescoEnCurso = false
                        _aviso.value = if (error is IOException) {
                            "Sin conexión: mostrando datos guardados"
                        } else {
                            "No se pudo actualizar: mostrando datos guardados"
                        }
                        _estado.value = UiState.Exito(guardados)
                    }
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
