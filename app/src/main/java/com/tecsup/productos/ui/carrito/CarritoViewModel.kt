package com.tecsup.productos.ui.carrito

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.productos.data.carrito.CambioCarritoRechazado
import com.tecsup.productos.data.carrito.CarritoRepository
import com.tecsup.productos.data.carrito.MotivoCarrito
import com.tecsup.productos.data.pedidos.PedidoRepository
import com.tecsup.productos.data.pedidos.StockInsuficiente
import com.tecsup.productos.data.pedidos.PedidoSinPermiso
import com.tecsup.productos.domain.model.Carrito
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CarritoViewModel(
    private val carrito: CarritoRepository,
    private val pedidos: PedidoRepository,
    private val sesion: Sesion,
) : ViewModel() {
    private val reintentos = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val estado = reintentos.flatMapLatest {
        carrito.observar()
            .map<Carrito, UiState<Carrito>> { UiState.Exito(it) }
            .onStart { emit(UiState.Cargando) }
            .catch { emit(UiState.Error("No pudimos cargar tu carrito.")) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Cargando)

    fun reintentarCarga() {
        reintentos.update { it + 1 }
    }

    private val _ocupado = MutableStateFlow(false)
    val ocupado = _ocupado.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private val _pedidoCreado = MutableStateFlow<String?>(null)
    val pedidoCreado = _pedidoCreado.asStateFlow()

    fun cambiarCantidad(productoId: Int, cantidad: Int) = ejecutar("No pudimos cambiar la cantidad. Inténtalo de nuevo.") {
        carrito.cambiarCantidad(productoId, cantidad)
    }

    fun quitar(productoId: Int) = ejecutar("No pudimos quitar el producto. Inténtalo de nuevo.") {
        carrito.quitar(productoId)
    }

    fun enviar() = ejecutar("No pudimos completar el envío del pedido. Inténtalo de nuevo.") {
        val solicitud = carrito.preparar()
        val solicitudId = requireNotNull(solicitud.solicitudId)
        val id = try {
            pedidos.crear(sesion, solicitudId, solicitud.lineas)
        } catch (e: StockInsuficiente) {
            carrito.liberar(solicitudId)
            _error.value = "No hay suficientes unidades de ${e.producto}. Ajusta la cantidad o quita el producto."
            return@ejecutar
        } catch (e: PedidoSinPermiso) {
            carrito.liberar(solicitudId)
            _error.value = "Tu cuenta no pudo enviar este pedido. Comunícate con el administrador."
            return@ejecutar
        }
        carrito.completar(solicitudId)
        _pedidoCreado.value = id
    }

    fun confirmarNavegacion() {
        _pedidoCreado.value = null
    }

    private fun ejecutar(mensajeError: String, accion: suspend () -> Unit) {
        if (sesion.rol != Rol.USUARIO || _ocupado.value || _pedidoCreado.value != null) return
        _ocupado.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                accion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: CambioCarritoRechazado) {
                _error.value = if (e.motivo == MotivoCarrito.LIMITE_PRODUCTOS) {
                    "Deja hasta ${Pedido.MAX_PRODUCTOS} productos distintos en el carrito para enviar el pedido."
                } else {
                    mensajeError
                }
            } catch (_: Exception) {
                _error.value = mensajeError
            } finally {
                _ocupado.value = false
            }
        }
    }
}
