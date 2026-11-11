package com.tecsup.productos.domain.model

enum class EstadoPedido(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    CONFIRMADO("Confirmado"),
    ENTREGADO("Entregado");

    fun siguiente(): EstadoPedido? = when (this) {
        PENDIENTE -> CONFIRMADO
        CONFIRMADO -> ENTREGADO
        ENTREGADO -> null
    }
}

data class LineaPedido(
    val productoId: Int,
    val producto: String,
    val cantidad: Int,
    val precioCentavos: Long?,
) {
    val subtotalCentavos: Long?
        get() = precioCentavos?.times(cantidad)

    fun validar() {
        require(productoId > 0 && producto.isNotBlank() && producto.length <= 200)
        require(cantidad in 1..Pedido.CANTIDAD_MAXIMA)
        require(precioCentavos != null && precioCentavos in 0..Pedido.PRECIO_MAXIMO_CENTAVOS)
    }
}

data class Pedido(
    val id: String,
    val correo: String,
    val lineas: List<LineaPedido>,
    val totalCentavos: Long?,
    val estado: EstadoPedido,
    val fecha: Long,
) {
    val cantidad: Int get() = lineas.sumOf(LineaPedido::cantidad)
    val numero: String get() = id.substringAfterLast(':').takeLast(8).uppercase()

    companion object {
        const val CANTIDAD_MAXIMA = 10
        const val MAX_PRODUCTOS = 5
        const val PRECIO_MAXIMO_CENTAVOS = 100_000_000L
    }
}
