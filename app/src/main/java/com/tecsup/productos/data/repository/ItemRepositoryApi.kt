package com.tecsup.productos.data.repository

import com.tecsup.productos.data.mapper.toItem
import com.tecsup.productos.data.remote.ApiService
import com.tecsup.productos.domain.model.Item
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

class ItemRepositoryApi(
    private val api: ApiService,
    private val categoria: String,
) : ItemRepository {
    private val cache = MutableStateFlow<List<Item>>(emptyList())

    override val items: Flow<List<Item>> = cache.asStateFlow()
    override val favoritos: Flow<List<Item>> = items.map { lista ->
        lista.filter(Item::esFavorito)
    }

    override suspend fun refrescar(): Result<Int> = try {
        val idsFavoritos = cache.value.filter(Item::esFavorito).map(Item::id).toSet()
        val productos = api.listarPorCategoria(categoria).products.map { producto ->
            producto.toItem(esFavorito = producto.id in idsFavoritos)
        }
        cache.value = productos
        Result.success(productos.size)
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Result.failure(e)
    } catch (e: HttpException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun obtenerDetalle(id: Int): Result<Item> = try {
        val esFavorito = cache.value.firstOrNull { it.id == id }?.esFavorito ?: false
        Result.success(api.obtenerProducto(id).toItem(esFavorito))
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Result.failure(e)
    } catch (e: HttpException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun alternarFavorito(id: Int) {
        cache.value = cache.value.map { item ->
            if (item.id == id) item.copy(esFavorito = !item.esFavorito) else item
        }
    }

    override suspend fun cantidadGuardada(): Int = cache.value.size

    override suspend fun cantidadFavoritos(): Int = cache.value.count(Item::esFavorito)

    override suspend fun limpiarCache() {
        cache.value = emptyList()
    }
}
