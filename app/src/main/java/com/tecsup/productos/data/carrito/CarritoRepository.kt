package com.tecsup.productos.data.carrito

import com.tecsup.productos.domain.model.Carrito
import com.tecsup.productos.domain.model.LineaPedido
import kotlinx.coroutines.flow.Flow

enum class MotivoCarrito { LIMITE_PRODUCTOS, LIMITE_UNIDADES, ENVIO_PENDIENTE }

class CambioCarritoRechazado(val motivo: MotivoCarrito) : IllegalArgumentException()

interface CarritoRepository {
    fun observar(): Flow<Carrito>
    suspend fun agregar(linea: LineaPedido)
    suspend fun cambiarCantidad(productoId: Int, cantidad: Int)
    suspend fun quitar(productoId: Int)
    suspend fun preparar(): Carrito
    suspend fun liberar(solicitudId: String)
    suspend fun completar(solicitudId: String)
}
