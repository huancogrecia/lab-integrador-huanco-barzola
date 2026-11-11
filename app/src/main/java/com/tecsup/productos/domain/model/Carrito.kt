package com.tecsup.productos.domain.model

data class Carrito(val lineas: List<LineaPedido>, val solicitudId: String? = null) {
    val totalCentavos: Long get() = lineas.sumOf { requireNotNull(it.subtotalCentavos) }
}
