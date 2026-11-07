package com.tecsup.productos.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tecsup.productos.AppContainer

class AdminViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            return modelClass.cast(AdminViewModel(container.itemRepository, container.notificador))
                ?: throw IllegalArgumentException("No se pudo crear AdminViewModel")
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
