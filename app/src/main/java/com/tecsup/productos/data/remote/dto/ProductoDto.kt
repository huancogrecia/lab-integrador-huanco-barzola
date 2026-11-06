package com.tecsup.productos.data.remote.dto

data class ProductosResponseDto(
    val products: List<ProductoDto> = emptyList(),
    val total: Int = 0,
    val skip: Int = 0,
    val limit: Int = 0,
)

data class ProductoDto(
    val id: Int,
    val title: String,
    val description: String? = null,
    val category: String? = null,
    val price: Double? = null,
    val brand: String? = null,
    val thumbnail: String? = null,
    val images: List<String> = emptyList(),
)
