package com.tecsup.productos.data.local

import androidx.room.Entity
import com.tecsup.productos.domain.model.LineaPedido

@Entity(tableName = "carrito", primaryKeys = ["uid", "productoId"])
data class CarritoEntity(
    val uid: String,
    val productoId: Int,
    val producto: String,
    val cantidad: Int,
    val precioCentavos: Long,
    val solicitudId: String? = null,
) {
    fun aLinea() = LineaPedido(productoId, producto, cantidad, precioCentavos)
}
