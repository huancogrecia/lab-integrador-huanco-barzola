package com.tecsup.productos.ui.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.productos.data.carrito.CambioCarritoRechazado
import com.tecsup.productos.data.carrito.CarritoRepository
import com.tecsup.productos.data.carrito.MotivoCarrito
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.data.inventario.InventarioRepository
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.domain.model.LineaPedido
import com.tecsup.productos.domain.model.Pedido
import com.tecsup.productos.domain.model.Rol
import com.tecsup.productos.domain.model.Sesion
import com.tecsup.productos.ui.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetalleViewModel(
    private val repo: ItemRepository,
    private val carrito: CarritoRepository,
    inventario: InventarioRepository,
    sesion: Sesion,
    private val id: Int,
) : ViewModel() {
    private val _estado = MutableStateFlow<UiState<Item>>(UiState.Cargando)
    val estado = _estado.asStateFlow()
    private val _solicitud = MutableStateFlow<UiState<Unit>?>(null)
    val solicitud = _solicitud.asStateFlow()
    val puedePedir = sesion.rol == Rol.USUARIO
    private var carga: Job? = null
    private val reintentosStock = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val stock = reintentosStock.flatMapLatest {
        inventario.observar()
            .map<Map<Int, Int>, UiState<Int>> { UiState.Exito(it[id] ?: 0) }
            .onStart { emit(UiState.Cargando) }
            .catch { emit(UiState.Error("No pudimos consultar el stock.")) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), UiState.Cargando)

    fun reintentarStock() {
        reintentosStock.update { it + 1 }
    }

    init {
        cargar()
    }

    fun cargar() {
        carga?.cancel()
        carga = viewModelScope.launch {
            _estado.value = UiState.Cargando
            _estado.value = repo.obtenerDetalle(id).fold(
                onSuccess = { UiState.Exito(it) },
                onFailure = { UiState.Error("No pudimos cargar este producto. Revisa tu conexión.") }
            )
        }
    }

    fun subtotal(cantidad: Int) =
        (_estado.value as? UiState.Exito)?.datos?.precioUnitario?.multiply(cantidad.toBigDecimal())

    fun agregarAlCarrito(cantidad: Int) {
        val producto = (_estado.value as? UiState.Exito)?.datos ?: return
        val precio = producto.precioUnitario ?: return
        val disponibles = (stock.value as? UiState.Exito)?.datos ?: return
        if (!puedePedir || _solicitud.value is UiState.Cargando ||
            cantidad !in 1..minOf(disponibles, Pedido.CANTIDAD_MAXIMA)
        ) return
        _solicitud.value = UiState.Cargando
        viewModelScope.launch {
            try {
                val enCarrito = carrito.observar().first().lineas
                    .firstOrNull { it.productoId == id }?.cantidad ?: 0
                if (enCarrito + cantidad > disponibles) {
                    _solicitud.value = UiState.Error("Ya tienes $enCarrito en el carrito. Solo hay $disponibles unidades disponibles en total.")
                    return@launch
                }
                val centavos = precio.movePointRight(2).longValueExact()
                carrito.agregar(LineaPedido(producto.id, producto.titulo, cantidad, centavos))
                _solicitud.value = UiState.Exito(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: CambioCarritoRechazado) {
                val mensaje = when (e.motivo) {
                    MotivoCarrito.LIMITE_PRODUCTOS -> "Puedes agregar hasta ${Pedido.MAX_PRODUCTOS} productos distintos."
                    MotivoCarrito.LIMITE_UNIDADES -> "Puedes agregar hasta ${Pedido.CANTIDAD_MAXIMA} unidades por producto."
                    MotivoCarrito.ENVIO_PENDIENTE -> "Abre el carrito y reintenta el envío del pedido pendiente."
                }
                _solicitud.value = UiState.Error(mensaje)
            } catch (_: Exception) {
                _solicitud.value = UiState.Error("No pudimos agregar el producto al carrito. Inténtalo de nuevo.")
            }
        }
    }

    fun limpiarSolicitud() {
        if (_solicitud.value !is UiState.Cargando) _solicitud.value = null
    }
}
