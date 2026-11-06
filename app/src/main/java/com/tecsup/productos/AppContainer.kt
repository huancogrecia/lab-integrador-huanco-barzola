package com.tecsup.productos

import com.tecsup.productos.data.remote.RetrofitClient
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.data.repository.ItemRepositoryApi

class AppContainer {
    val itemRepository: ItemRepository = ItemRepositoryApi(
        api = RetrofitClient.apiService,
        categoria = CATEGORIA_PRODUCTOS,
    )

    companion object {
        const val CATEGORIA_PRODUCTOS = "laptops"
    }
}
