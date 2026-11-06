package com.tecsup.productos.data.mapper

import com.tecsup.productos.data.remote.dto.ProductoDto
import com.tecsup.productos.domain.model.Item
import java.util.Locale

fun ProductoDto.toItem(esFavorito: Boolean = false): Item {
    val marca = brand?.takeIf(String::isNotBlank)
        ?: category?.takeIf(String::isNotBlank)
        ?: "Producto"
    val precio = price?.let { String.format(Locale.US, "\$%.2f", it) }

    return Item(
        id = id,
        titulo = title,
        subtitulo = listOfNotNull(marca, precio).joinToString(" · "),
        imagenUrl = thumbnail,
        esFavorito = esFavorito,
    )
}
