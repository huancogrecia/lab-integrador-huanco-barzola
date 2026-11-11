package com.tecsup.productos.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.notification.Notificador
import com.tecsup.productos.ui.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AdminViewModel(
    private val repo: ItemRepository,
    private val notificador: Notificador,
) : ViewModel() {
    private val _estado = MutableStateFlow<UiState<Int>>(UiState.Cargando)
    val estado = _estado.asStateFlow()

    private val _ocupado = MutableStateFlow(false)
    val ocupado = _ocupado.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje = _mensaje.asStateFlow()

    private var observador: Job? = null

    init {
        observarProductos()
    }

    fun observarProductos() {
        observador?.cancel()
        _estado.value = UiState.Cargando
        observador = viewModelScope.launch {
            repo.items
                .catch { _estado.value = UiState.Error("No pudimos cargar los productos guardados.") }
                .collect { items -> _estado.value = UiState.Exito(items.size) }
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
                        notificador.mostrar("Datos actualizados", "Se guardaron $cantidad elementos")
                    },
                    onFailure = { error ->
                        _mensaje.value = mensajeDe(error)
                    },
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensaje.value = mensajeDe(e)
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
                _mensaje.value = "No pudimos borrar los productos guardados. Inténtalo de nuevo."
            } finally {
                _ocupado.value = false
            }
        }
    }

    private fun mensajeDe(error: Throwable): String = when (error) {
        is IOException -> "No pudimos actualizar el catálogo. Revisa tu conexión e inténtalo de nuevo."
        is HttpException -> "El catálogo no está disponible en este momento. Inténtalo más tarde."
        else -> "No pudimos actualizar el catálogo. Inténtalo de nuevo."
    }
}
