package com.tecsup.productos.ui.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.productos.data.inventario.InventarioRepository
import com.tecsup.productos.data.inventario.StockModificado
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.domain.model.Sesion
import com.tecsup.productos.domain.model.Rol
import com.tecsup.productos.ui.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductoInventario(val id: Int, val nombre: String, val unidades: Int?)

class InventarioViewModel(
    productos: ItemRepository,
    private val inventario: InventarioRepository,
    private val sesion: Sesion,
) : ViewModel() {
    private val reintentos = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val estado = reintentos.flatMapLatest {
        combine(productos.items, inventario.observar()) { lista, existencias ->
            val filas = lista.map { ProductoInventario(it.id, it.titulo, existencias[it.id]) }
            val resultado: UiState<List<ProductoInventario>> = UiState.Exito(filas)
            resultado
        }.onStart { emit(UiState.Cargando) }
            .catch { emit(UiState.Error("No pudimos cargar el inventario. Revisa tu conexión.")) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), UiState.Cargando)

    private val _guardado = MutableStateFlow<UiState<Unit>?>(null)
    val guardado = _guardado.asStateFlow()

    fun reintentar() {
        reintentos.update { it + 1 }
    }

    fun limpiarGuardado() {
        if (_guardado.value !is UiState.Cargando) _guardado.value = null
    }

    fun guardar(productoId: Int, texto: String, unidadesEsperadas: Int?) {
        if (sesion.rol != Rol.ADMIN || _guardado.value is UiState.Cargando) return
        val unidades = texto.toIntOrNull()
        if (unidades == null || unidades !in 0..9999) {
            _guardado.value = UiState.Error("Ingresa una cantidad entre 0 y 9999.")
            return
        }
        _guardado.value = UiState.Cargando
        viewModelScope.launch {
            try {
                inventario.guardar(productoId, unidades, unidadesEsperadas)
                _guardado.value = UiState.Exito(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (_: StockModificado) {
                _guardado.value = UiState.Error("El stock cambió. Cierra este ajuste y vuelve a abrirlo para ver la cantidad actual.")
            } catch (_: Exception) {
                _guardado.value = UiState.Error("No pudimos guardar el stock. Inténtalo de nuevo.")
            }
        }
    }
}
