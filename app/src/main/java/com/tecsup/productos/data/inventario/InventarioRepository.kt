package com.tecsup.productos.data.inventario

import kotlinx.coroutines.flow.Flow

class StockModificado : Exception()

interface InventarioRepository {
    fun observar(): Flow<Map<Int, Int>>
    suspend fun guardar(productoId: Int, unidades: Int, unidadesEsperadas: Int?)
}
