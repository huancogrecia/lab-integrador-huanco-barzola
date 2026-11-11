package com.tecsup.productos.data.carrito

import androidx.room.withTransaction
import com.tecsup.productos.data.local.AppDatabase
import com.tecsup.productos.data.local.CarritoEntity
import com.tecsup.productos.domain.model.Carrito
import com.tecsup.productos.domain.model.LineaPedido
import com.tecsup.productos.domain.model.Pedido
import java.util.UUID
import kotlinx.coroutines.flow.map

class CarritoRepositoryRoom(private val db: AppDatabase, private val uid: String) : CarritoRepository {
    private val dao = db.carritoDao()

    override fun observar() = dao.observar(uid).map { filas ->
        Carrito(filas.map(CarritoEntity::aLinea), filas.firstOrNull()?.solicitudId)
    }

    override suspend fun agregar(linea: LineaPedido) = db.withTransaction {
        linea.validar()
        val filas = leerEditable()
        val anterior = filas.find { it.productoId == linea.productoId }
        if (anterior == null && filas.size >= Pedido.MAX_PRODUCTOS) {
            throw CambioCarritoRechazado(MotivoCarrito.LIMITE_PRODUCTOS)
        }
        val cantidad = (anterior?.cantidad ?: 0) + linea.cantidad
        if (cantidad > Pedido.CANTIDAD_MAXIMA) {
            throw CambioCarritoRechazado(MotivoCarrito.LIMITE_UNIDADES)
        }
        dao.guardar(CarritoEntity(uid, linea.productoId, linea.producto, cantidad, requireNotNull(linea.precioCentavos)))
    }

    override suspend fun cambiarCantidad(productoId: Int, cantidad: Int) = db.withTransaction {
        require(cantidad in 1..Pedido.CANTIDAD_MAXIMA)
        val fila = leerEditable().first { it.productoId == productoId }
        dao.guardar(fila.copy(cantidad = cantidad))
    }

    override suspend fun quitar(productoId: Int) = db.withTransaction {
        leerEditable()
        dao.quitar(uid, productoId)
    }

    override suspend fun preparar(): Carrito = db.withTransaction {
        val filas = dao.leer(uid)
        require(filas.isNotEmpty()) { "Agrega productos antes de hacer un pedido." }
        if (filas.size > Pedido.MAX_PRODUCTOS && filas.first().solicitudId == null) {
            throw CambioCarritoRechazado(MotivoCarrito.LIMITE_PRODUCTOS)
        }
        val solicitudId = filas.first().solicitudId ?: UUID.randomUUID().toString().also {
            dao.reservar(uid, it)
        }
        Carrito(filas.map(CarritoEntity::aLinea), solicitudId)
    }

    override suspend fun liberar(solicitudId: String) = dao.liberar(uid, solicitudId)

    override suspend fun completar(solicitudId: String) = dao.vaciarEnviado(uid, solicitudId)

    private suspend fun leerEditable(): List<CarritoEntity> = dao.leer(uid).also { filas ->
        if (filas.firstOrNull()?.solicitudId != null) {
            throw CambioCarritoRechazado(MotivoCarrito.ENVIO_PENDIENTE)
        }
    }
}
