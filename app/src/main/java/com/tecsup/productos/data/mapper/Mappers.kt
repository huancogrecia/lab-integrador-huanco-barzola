package com.tecsup.productos.data.mapper

import com.tecsup.productos.data.local.ItemEntity
import com.tecsup.productos.data.remote.dto.ProductoDto
import com.tecsup.productos.domain.model.Item
import java.util.Locale

fun ProductoDto.toItem(esFavorito: Boolean = false): Item = Item(
    id = id,
    titulo = title,
    subtitulo = subtituloProducto(),
    imagenUrl = thumbnail,
    esFavorito = esFavorito,
)

fun ProductoDto.toEntity(): ItemEntity = ItemEntity(
    id = id,
    titulo = title,
    subtitulo = subtituloProducto(),
    imagenUrl = thumbnail,
)

fun ItemEntity.toModelo(): Item = Item(
    id = id,
    titulo = titulo,
    subtitulo = subtitulo,
    imagenUrl = imagenUrl,
    esFavorito = esFavorito,
)

private fun ProductoDto.subtituloProducto(): String {
    val marca = brand?.takeIf(String::isNotBlank)
        ?: category?.takeIf(String::isNotBlank)
        ?: "Producto"
    val precio = price?.let { String.format(Locale.US, "\$%.2f", it) }
    return listOfNotNull(marca, precio).joinToString(" · ")
}
