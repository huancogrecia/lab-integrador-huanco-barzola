package com.tecsup.productos.data.mapper

import com.tecsup.productos.data.local.ItemConFavorito
import com.tecsup.productos.data.local.ItemEntity
import com.tecsup.productos.data.remote.dto.ProductoDto
import com.tecsup.productos.domain.model.Item
import java.math.RoundingMode
import java.util.Locale

fun ProductoDto.toItem(esFavorito: Boolean = false): Item = Item(
    id = id,
    titulo = title,
    subtitulo = subtituloProducto(),
    imagenUrl = thumbnail,
    esFavorito = esFavorito,
    precioUnitario = price?.takeIf { it.isFinite() && it >= 0 }
        ?.toBigDecimal()?.setScale(2, RoundingMode.HALF_UP),
)

fun ProductoDto.toEntity(): ItemEntity = ItemEntity(
    id = id,
    titulo = title,
    subtitulo = subtituloProducto(),
    imagenUrl = thumbnail,
)

fun ItemConFavorito.toModelo(): Item = Item(
    id = item.id,
    titulo = item.titulo,
    subtitulo = item.subtitulo,
    imagenUrl = item.imagenUrl,
    esFavorito = esFavorito,
)

private fun ProductoDto.subtituloProducto(): String {
    val marca = brand?.takeIf(String::isNotBlank)
        ?: category?.takeIf(String::isNotBlank)
        ?: "Producto"
    val precio = price?.let { String.format(Locale.US, "\$%.2f", it) }
    return listOfNotNull(marca, precio).joinToString(" · ")
}
