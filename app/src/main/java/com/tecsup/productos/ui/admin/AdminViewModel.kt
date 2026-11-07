package com.tecsup.productos.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.ui.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class AdminResumen(
    val totalGuardado: Int,
    val totalFavoritos: Int,
)

class AdminViewModel(private val repo: ItemRepository) : ViewModel() {
    private val _estado = MutableStateFlow<UiState<AdminResumen>>(UiState.Cargando)
    val estado = _estado.asStateFlow()

    private val _ocupado = MutableStateFlow(false)
    val ocupado = _ocupado.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje = _mensaje.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repo.items, repo.favoritos) { items, favoritos ->
                AdminResumen(items.size, favoritos.size)
            }
                .catch { error -> _estado.value = UiState.Error(mensajeDe(error)) }
                .collect { resumen -> _estado.value = UiState.Exito(resumen) }
        }
    }

    fun sincronizar() {
        if (_ocupado.value) return
        viewModelScope.launch {
            _ocupado.value = true
            _mensaje.value = null
            try {
                repo.refrescar().fold(
                    onSuccess = { cantidad ->
                        _mensaje.value = "Sincronización completa: $cantidad productos guardados."
                    },
                    onFailure = { error ->
                        _mensaje.value = "No se pudo sincronizar: ${mensajeDe(error)}"
                    },
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensaje.value = "No se pudo sincronizar: ${mensajeDe(e)}"
            } finally {
                _ocupado.value = false
            }
        }
    }

    fun limpiarCache() {
        if (_ocupado.value) return
        viewModelScope.launch {
            _ocupado.value = true
            _mensaje.value = null
            try {
                repo.limpiarCache()
                _mensaje.value = "Caché local eliminada."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensaje.value = "No se pudo limpiar la caché."
            } finally {
                _ocupado.value = false
            }
        }
    }

    private fun mensajeDe(error: Throwable): String = when (error) {
        is IOException -> "No hay conexión a internet"
        is HttpException -> "El servidor respondió con error ${error.code()}"
        else -> error.localizedMessage ?: "Ocurrió un error inesperado"
    }
}
