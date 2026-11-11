package com.tecsup.productos.data.local

import androidx.room.Embedded
import androidx.room.Entity

@Entity(tableName = "favoritos", primaryKeys = ["uid", "productoId"])
data class FavoritoEntity(val uid: String, val productoId: Int)

data class ItemConFavorito(
    @Embedded val item: ItemEntity,
    val esFavorito: Boolean,
)
