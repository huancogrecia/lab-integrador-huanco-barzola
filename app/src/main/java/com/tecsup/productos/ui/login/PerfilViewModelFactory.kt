package com.tecsup.productos.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tecsup.productos.AppContainer

class PerfilViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PerfilViewModel::class.java)) {
            return modelClass.cast(
                PerfilViewModel(container.itemRepository, container.preferenciasRepository)
            )
                ?: throw IllegalArgumentException("No se pudo crear PerfilViewModel")
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
