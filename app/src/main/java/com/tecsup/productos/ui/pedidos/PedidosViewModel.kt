package com.tecsup.productos.ui.pedidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.productos.data.pedidos.PedidoRepository
import com.tecsup.productos.data.pedidos.StockInsuficiente
import com.tecsup.productos.domain.model.EstadoPedido
import com.tecsup.productos.domain.model.Pedido
import com.tecsup.productos.domain.model.Rol
import com.tecsup.productos.domain.model.Sesion
import com.tecsup.productos.ui.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PedidosViewModel(
    private val repo: PedidoRepository,
    private val sesion: Sesion,
) : ViewModel() {
    private val reintentos = MutableStateFlow(0)
    private val _filtro = MutableStateFlow<EstadoPedido?>(null)
    val filtro = _filtro.asStateFlow()

    fun filtrar(estado: EstadoPedido?) {
        if (sesion.rol == Rol.ADMIN) _filtro.value = estado
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarDetalle(id: String) = reintentos.flatMapLatest {
        repo.observarUno(id)
            .map<Pedido?, UiState<Pedido>> { pedido ->
                if (pedido == null) UiState.Error("El pedido no está disponible.")
                else UiState.Exito(pedido)
            }
            .onStart { emit(UiState.Cargando) }
            .catch { emit(UiState.Error("No pudimos cargar este pedido. Revisa tu conexión.")) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val estado = reintentos.flatMapLatest {
        repo.observar(sesion)
            .map<List<Pedido>, UiState<List<Pedido>>> { UiState.Exito(it) }
            .onStart { emit(UiState.Cargando) }
            .catch { emit(UiState.Error("No pudimos cargar los pedidos. Revisa tu conexión.")) }
    }.combine(filtro) { estado, filtro ->
        if (sesion.rol == Rol.ADMIN && estado is UiState.Exito) {
            UiState.Exito(estado.datos.filter { filtro == null || it.estado == filtro }
                .sortedWith(compareBy<Pedido> { it.estado.ordinal }.thenByDescending { it.fecha }))
        } else {
            estado
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), UiState.Cargando)

    private val _actualizando = MutableStateFlow<String?>(null)
    val actualizando = _actualizando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun reintentar() {
        reintentos.update { it + 1 }
    }

    fun avanzar(pedido: Pedido) {
        if (sesion.rol != Rol.ADMIN || _actualizando.value != null || pedido.estado.siguiente() == null) return
        _actualizando.value = pedido.id
        _error.value = null
        viewModelScope.launch {
            try {
                repo.avanzar(pedido.id, pedido.estado)
            } catch (e: CancellationException) {
                throw e
            } catch (e: StockInsuficiente) {
                _error.value = "No hay suficientes unidades de ${e.producto}. Actualiza el stock antes de confirmar."
            } catch (_: Exception) {
                _error.value = "No pudimos cambiar el estado del pedido. Inténtalo de nuevo."
            } finally {
                _actualizando.value = null
            }
        }
    }
}
