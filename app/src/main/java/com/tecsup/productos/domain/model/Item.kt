package com.tecsup.productos.domain.model

import java.math.BigDecimal

data class Item(
    val id: Int,
    val titulo: String,
    val subtitulo: String,
    val imagenUrl: String?,
    val esFavorito: Boolean = false,
    val precioUnitario: BigDecimal? = null,
)
