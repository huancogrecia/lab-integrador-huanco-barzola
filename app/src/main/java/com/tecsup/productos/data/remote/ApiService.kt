package com.tecsup.productos.data.remote

import com.tecsup.productos.data.remote.dto.ProductoDto
import com.tecsup.productos.data.remote.dto.ProductosResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("products/category/{slug}")
    suspend fun listarPorCategoria(
        @Path("slug") slug: String,
    ): ProductosResponseDto

    @GET("products/{id}")
    suspend fun obtenerProducto(
        @Path("id") id: Int,
    ): ProductoDto
}
