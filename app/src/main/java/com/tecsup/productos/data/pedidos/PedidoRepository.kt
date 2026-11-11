package com.tecsup.productos.data.pedidos

import com.tecsup.productos.domain.model.EstadoPedido
import com.tecsup.productos.domain.model.LineaPedido
import com.tecsup.productos.domain.model.Pedido
import com.tecsup.productos.domain.model.Sesion
import kotlinx.coroutines.flow.Flow

class PedidoSinPermiso(causa: Throwable) : Exception(causa)

class StockInsuficiente(val producto: String) : Exception()

interface PedidoRepository {
    fun observar(sesion: Sesion): Flow<List<Pedido>>
    fun observarUno(id: String): Flow<Pedido?>
    suspend fun crear(sesion: Sesion, solicitudId: String, lineas: List<LineaPedido>): String
    suspend fun avanzar(id: String, estadoActual: EstadoPedido)
}
