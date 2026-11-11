package com.tecsup.productos.data.repository

import com.tecsup.productos.data.local.ItemDao
import com.tecsup.productos.data.local.ItemConFavorito
import com.tecsup.productos.data.mapper.toEntity
import com.tecsup.productos.data.mapper.toItem
import com.tecsup.productos.data.mapper.toModelo
import com.tecsup.productos.data.remote.ApiService
import com.tecsup.productos.domain.model.Item
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

class ItemRepositoryImpl(
    private val api: ApiService,
    private val dao: ItemDao,
    private val uid: String,
) : ItemRepository {
    override val items: Flow<List<Item>> = dao.todos(uid).map { lista ->
        lista.map(ItemConFavorito::toModelo)
    }

    override val favoritos: Flow<List<Item>> = dao.favoritos(uid).map { lista ->
        lista.map(ItemConFavorito::toModelo)
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
        Result.success(api.obtenerProducto(id).toItem(esFavorito = dao.esFavorito(uid, id)))
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Result.failure(e)
    } catch (e: HttpException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun alternarFavorito(id: Int) = dao.alternarFavorito(uid, id)

    override suspend fun cantidadGuardada(): Int = dao.contar()

    override suspend fun cantidadFavoritos(): Int = dao.contarFavoritos(uid)

    override suspend fun limpiarCache() = dao.borrarTodo()

    companion object {
        const val CATEGORIA_PRODUCTOS = "laptops"
    }
}
