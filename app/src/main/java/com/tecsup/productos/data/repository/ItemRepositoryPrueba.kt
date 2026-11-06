package com.tecsup.productos.data.repository

import com.tecsup.productos.domain.model.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class ItemRepositoryPrueba : ItemRepository {
    private val datos = MutableStateFlow(
        listOf(
            Item(1, "Laptop HP 14", "HP · S/ 2,499.00", null),
            Item(2, "Laptop Lenovo IdeaPad", "Lenovo · S/ 2,199.00", null),
            Item(3, "Laptop ASUS VivoBook", "ASUS · S/ 2,799.00", null)
        )
    )

    override val items: Flow<List<Item>> = datos.asStateFlow()
    override val favoritos: Flow<List<Item>> = datos.map { lista -> lista.filter { it.esFavorito } }

    override suspend fun refrescar(): Result<Int> = Result.success(datos.value.size)

    override suspend fun obtenerDetalle(id: Int): Result<Item> {
        val item = datos.value.find { it.id == id }
        return if (item != null) Result.success(item)
        else Result.failure(NoSuchElementException("Producto no encontrado"))
    }

    override suspend fun alternarFavorito(id: Int) {
        datos.value = datos.value.map { item ->
            if (item.id == id) item.copy(esFavorito = !item.esFavorito) else item
        }
    }

    override suspend fun cantidadGuardada(): Int = datos.value.size

    override suspend fun cantidadFavoritos(): Int = datos.value.count { it.esFavorito }

    override suspend fun limpiarCache() {
        datos.value = emptyList()
    }
}
