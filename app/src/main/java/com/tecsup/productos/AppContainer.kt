package com.tecsup.productos

import android.content.Context
import com.tecsup.productos.data.local.AppDatabase
import com.tecsup.productos.data.remote.RetrofitClient
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.data.repository.ItemRepositoryImpl

class AppContainer(context: Context) {
    private val itemDao = AppDatabase.obtener(context.applicationContext).itemDao()

    val itemRepository: ItemRepository = ItemRepositoryImpl(
        api = RetrofitClient.apiService,
        dao = itemDao,
    )
}
