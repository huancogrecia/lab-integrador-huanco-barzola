package com.tecsup.productos.data.repository

import com.tecsup.productos.data.local.ItemDao
import com.tecsup.productos.data.local.ItemEntity
import com.tecsup.productos.data.mapper.toEntity
import com.tecsup.productos.data.mapper.toItem
import com.tecsup.productos.data.mapper.toModelo
import com.tecsup.productos.data.remote.ApiService
import com.tecsup.productos.domain.model.Item
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException

class ItemRepositoryImpl(
    private val api: ApiService,
    private val dao: ItemDao,
) : ItemRepository {
    override val items: Flow<List<Item>> = dao.todos().map { lista ->
        lista.map(ItemEntity::toModelo)
    }

    override val favoritos: Flow<List<Item>> = dao.favoritos().map { lista ->
        lista.map(ItemEntity::toModelo)
    }

    override suspend fun refrescar(): Result<Int> = try {
        val productos = api.listarPorCategoria(CATEGORIA_PRODUCTOS).products
        dao.sincronizar(productos.map { it.toEntity() })
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
        val idsFavoritos = dao.idsFavoritos().toSet()
        Result.success(api.obtenerProducto(id).toItem(esFavorito = id in idsFavoritos))
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Result.failure(e)
    } catch (e: HttpException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun alternarFavorito(id: Int) = dao.alternarFavorito(id)

    override suspend fun cantidadGuardada(): Int = dao.contar()

    override suspend fun cantidadFavoritos(): Int = dao.contarFavoritos()

    override suspend fun limpiarCache() = dao.borrarTodo()

    companion object {
        const val CATEGORIA_PRODUCTOS = "laptops"
    }
}
