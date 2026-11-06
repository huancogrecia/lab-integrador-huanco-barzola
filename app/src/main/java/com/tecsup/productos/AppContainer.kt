package com.tecsup.productos

import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.data.repository.ItemRepositoryPrueba

class AppContainer {
    val itemRepository: ItemRepository = ItemRepositoryPrueba()
}
